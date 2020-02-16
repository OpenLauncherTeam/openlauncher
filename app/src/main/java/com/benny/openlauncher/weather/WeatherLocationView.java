package com.benny.openlauncher.weather;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.util.AppSettings;
import com.benny.openlauncher.util.Tool;

import java.util.ArrayList;
import java.util.List;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;
import static android.support.v7.widget.helper.ItemTouchHelper.LEFT;

public class WeatherLocationView extends RecyclerView {
    private Context _context;
    private static WeatherLocationSelectionAdapter _locationAdapter;
    private LinearLayoutManager _layoutManager;
    private WeatherLocationDecorator _decorator;
    private TextPaint _contextDialogPaint = new TextPaint();

    public int _maximumWidth = 0;

    public WeatherLocationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _context = context;
        _layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        _locationAdapter = new WeatherLocationSelectionAdapter(context);
        _decorator = new WeatherLocationDecorator(context.getResources());
        _contextDialogPaint.setTextSize(Tool.dp2px(14));

        setHasFixedSize(false);
        setLayoutManager(_layoutManager);
        setAdapter(_locationAdapter);
        addItemDecoration(_decorator);
    }

    public static WeatherLocationView build(Context context, AttributeSet attrs) {
        WeatherLocationView instance = new WeatherLocationView(context, attrs);
        instance.onFinishInflate();

        final SwipeController swipeController = new SwipeController(new SwipeControllerActions() {
            @Override
            public void onRightClicked(int position) {
                WeatherLocation loc = _locationAdapter.getObject(position);
                AppSettings.get().removeWeatherLocations(loc);

                instance._decorator.removeDivider(loc);

                _locationAdapter.remove(position);
                _locationAdapter.notifyItemRemoved(position);
                _locationAdapter.notifyItemRangeChanged(position, _locationAdapter.getItemCount());
            }
        });

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
        itemTouchhelper.attachToRecyclerView(instance);

        instance.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                swipeController.onDraw(c);
            }
        });

        return instance;
    }

    public void appendData(List<WeatherLocation> list) {
        _locationAdapter.appendData(list);
        for (WeatherLocation loc: list) {
            _maximumWidth = Math.max(_maximumWidth, (int) _contextDialogPaint.measureText(loc.toString()));
        }

        WeatherLocation last = _locationAdapter.getObject(_locationAdapter.getItemCount()-1);
        _decorator.addDivider(last);
        WeatherService.LOG.debug("adding divider for {} at position {}", last, _locationAdapter.getItemCount()-1);
    }

    public void appendData(WeatherLocation loc) {
        _locationAdapter.appendData(loc);
        _maximumWidth = Math.max(_maximumWidth, (int) _contextDialogPaint.measureText(loc.toString()));

        // Don't use loc as we might not have added it in if it is a duplicate.
        WeatherLocation last = _locationAdapter.getObject(_locationAdapter.getItemCount()-1);
        _decorator.addDivider(last);

        WeatherService.LOG.debug("adding divider for {} at position {}", last, _locationAdapter.getItemCount()-1);
    }

    public void setDialog(Dialog dialog) {
        _locationAdapter.setDialog(dialog);
    }

    public static class WeatherLocationViewHolder extends RecyclerView.ViewHolder {
        public View _parent;
        public TextView _location;

        public WeatherLocationViewHolder(View view) {
            super(view);

            _parent = view;
            _location = (TextView) view.findViewById(R.id.location);
        }
    }

    public static class SwipeController extends Callback {
        private boolean _swipeBack = false;
        private ButtonsState _buttonShowedState = ButtonsState.GONE;
        private RectF _buttonInstance = null;
        private RecyclerView.ViewHolder _currentItemViewHolder = null;
        private SwipeControllerActions _buttonsActions;
        private TextPaint _labelPaint = new TextPaint();
        private Paint _buttonPaint = new Paint();
        private float _buttonWidth = 0f;

        enum ButtonsState {
            GONE,
            LEFT_VISIBLE,
            RIGHT_VISIBLE
        }

        public SwipeController(SwipeControllerActions buttonsActions) {
            _buttonsActions = buttonsActions;

            _labelPaint.setColor(Color.WHITE);
            _labelPaint.setAntiAlias(true);
            _labelPaint.setTextSize(Tool.sp2px(12));

            _buttonPaint.setColor(Color.RED);
        }

        @Override
        public int convertToAbsoluteDirection(int flags, int layoutDirection) {
            if (_swipeBack) {
                _swipeBack = _buttonShowedState != ButtonsState.GONE;
                return 0;
            }
            return super.convertToAbsoluteDirection(flags, layoutDirection);
        }

        private void drawButtons(Canvas c, RecyclerView.ViewHolder viewHolder) {
            if (_buttonWidth == 0) {
                _buttonWidth = _labelPaint.measureText(HomeActivity._launcher.getString(R.string.delete)) * 2;
            }
            float corners = 16;

            View location = viewHolder.itemView;
            View parent = (View) location.getParent();

            RectF rightButton = new RectF(parent.getRight() - _buttonWidth, location.getTop(), parent.getRight(), location.getBottom());
            c.drawRoundRect(rightButton, corners, corners, _buttonPaint);
            drawText(HomeActivity._launcher.getString(R.string.delete), c, rightButton);

            _buttonInstance = null;
            if (_buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                _buttonInstance = rightButton;
            }
        }

        private void drawText(String text, Canvas c, RectF button) {
            float textWidth = _labelPaint.measureText(text);
            c.drawText(text, button.centerX() - (textWidth / 2), button.centerY() + (_labelPaint.getTextSize() / 2), _labelPaint);
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(0, LEFT);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                float dX, float dY, int actionState, boolean isCurrentlyActive) {

            // We should only be able to delete stored locations which have all the details in them.
            WeatherLocation location = WeatherLocation.parse(((WeatherLocationViewHolder) viewHolder)._location.getText().toString());
            ArrayList<WeatherLocation> storedLocations = AppSettings.get().getWeatherLocations();
            if (!storedLocations.contains(location)) {
                return;
            }

            if (actionState == ACTION_STATE_SWIPE) {
                if (_buttonShowedState != ButtonsState.GONE) {
                    if (_buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                        dX = Math.min(dX, - _buttonWidth);
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                else {
                    setTouchListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            if (_buttonShowedState == ButtonsState.GONE) {
                 super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            _currentItemViewHolder = viewHolder;
        }

        public void onDraw(Canvas c) {
            if (_currentItemViewHolder != null) {
                drawButtons(c, _currentItemViewHolder);
            }
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        private void setItemsClickable(RecyclerView recyclerView, boolean isClickable) {
            for (int i = 0; i < recyclerView.getChildCount(); ++i) {
                recyclerView.getChildAt(i).setClickable(isClickable);
            }
        }

        private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                      float dX, float dY, int actionState, boolean isCurrentlyActive) {

            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    _swipeBack = event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP;
                    if (_swipeBack) {
                        if (dX < -_buttonWidth) {
                            _buttonShowedState = ButtonsState.RIGHT_VISIBLE;
                        } else if (dX > _buttonWidth) {
                            _buttonShowedState = ButtonsState.LEFT_VISIBLE;
                        }

                        if (_buttonShowedState != ButtonsState.GONE) {
                            setTouchDownListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                            setItemsClickable(recyclerView, false);
                        }
                    }
                    return false;
                }
            });
        }

        private void setTouchDownListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder,
                                          final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        setTouchUpListener(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                    return false;
                }
            });
        }

        private void setTouchUpListener(final Canvas c, final RecyclerView recyclerView, final RecyclerView.ViewHolder viewHolder,
                                        final float dX, final float dY, final int actionState, final boolean isCurrentlyActive) {
            recyclerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        SwipeController.super.onChildDraw(c, recyclerView, viewHolder, 0F, dY, actionState, isCurrentlyActive);
                        recyclerView.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return false;
                            }
                        });
                        setItemsClickable(recyclerView, true);
                        _swipeBack = false;

                        if (_buttonsActions != null && _buttonInstance != null && _buttonInstance.contains(event.getX(), event.getY())) {
                            if (_buttonShowedState == ButtonsState.LEFT_VISIBLE) {
                                _buttonsActions.onLeftClicked(viewHolder.getAdapterPosition());
                            }
                            else if (_buttonShowedState == ButtonsState.RIGHT_VISIBLE) {
                                _buttonsActions.onRightClicked(viewHolder.getAdapterPosition());
                            }
                        }
                        _buttonShowedState = ButtonsState.GONE;
                        _currentItemViewHolder = null;
                    }
                    return false;
                }
            });
        }
    }

    public static abstract class SwipeControllerActions {

        public void onLeftClicked(int position) {}

        public void onRightClicked(int position) {}

    }
}

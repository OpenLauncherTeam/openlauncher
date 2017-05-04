package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.DialogUtils;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

public class DragOptionView extends CardView {
    private View[] hideViews;
    private LinearLayout dragOptions;

    private TextView editIcon;
    private TextView removeIcon;
    private TextView infoIcon;
    private TextView deleteIcon;

    private Home home;
    private Long animSpeed = 120L;
    private boolean init = false;
    public boolean dragging = false;

    public void setHome(Home home) {
        this.home = home;
    }

    public DragOptionView(Context context) {
        super(context);
        init();
    }

    public DragOptionView(Context context, AttributeSet attr) {
        super(context, attr);
        init();
    }

    public void setAutoHideView(View... v) {
        hideViews = v;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!init) {
            init = true;
            setY(-getHeight() - getCardElevation());
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    private ViewPropertyAnimator hide() {
        return animate().y(-getHeight() - getCardElevation()).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
    }

    private ViewPropertyAnimator show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return animate().y(((ConstraintLayout.LayoutParams) getLayoutParams()).topMargin + Tool.getStatusBarHeight(getContext())).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
        } else {
            return animate().y(((ConstraintLayout.LayoutParams) getLayoutParams()).topMargin).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    private void init() {
        init = false;
        setCardElevation(Tool.dp2px(4, getContext()));
        setRadius(Tool.dp2px(2, getContext()));

        dragOptions = (LinearLayout) ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_drag_options, this, false);
        addView(dragOptions);

        editIcon = (TextView) dragOptions.findViewById(R.id.editIcon);
        editIcon.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        if (((DragAction) dragEvent.getLocalState()).action == DragAction.Action.APP_DRAWER) {
                            return false;
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        DialogUtils.alert(getContext(), "Not implemented", "Not implemented");
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        deleteIcon = (TextView) dragOptions.findViewById(R.id.deleteIcon);
        deleteIcon.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case APP_DRAWER:
                            case APP:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        startDeletePackageDialog(dragEvent);
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        infoIcon = (TextView) dragOptions.findViewById(R.id.infoIcon);
        infoIcon.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case APP_DRAWER:
                            case APP:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Item.class.getClassLoader());
                        Item item = intent.getParcelableExtra("mDragData");
                        if (item.type == Item.Type.APP) {
                            try {
                                getContext().startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + item.appIntent.getComponent().getPackageName())));
                            } catch (Exception e) {
                                Tool.toast(getContext(), R.string.toast_appuninstalled);
                            }
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        removeIcon = (TextView) dragOptions.findViewById(R.id.removeIcon);
        removeIcon.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case GROUP:
                            case APP:
                            case WIDGET:
                            case SHORTCUT:
                            case APP_DRAWER:
                            case ACTION:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Item.class.getClassLoader());
                        Item item = intent.getParcelableExtra("mDragData");

                        // remove all items from the database
                        Home.launcher.db.deleteItem(item);
                        if (item.type == Item.Type.GROUP) {
                            for (Item i : item.items) {
                                Home.launcher.db.deleteItem(i);
                            }
                        }

                        home.desktop.consumeRevert();
                        home.dock.consumeRevert();
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });

        editIcon.setText(removeIcon.getText(), TextView.BufferType.SPANNABLE);
        removeIcon.setText(removeIcon.getText(), TextView.BufferType.SPANNABLE);
        infoIcon.setText(infoIcon.getText(), TextView.BufferType.SPANNABLE);
        deleteIcon.setText(deleteIcon.getText(), TextView.BufferType.SPANNABLE);

        for (int i = 0; i < dragOptions.getChildCount(); i++) {
            dragOptions.getChildAt(i).setVisibility(View.GONE);
        }
    }

    private void startDeletePackageDialog(DragEvent dragEvent) {
        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
        intent.setExtrasClassLoader(Item.class.getClassLoader());
        Item item = intent.getParcelableExtra("mDragData");
        if (item.type == Item.Type.APP) {
            try {
                Uri packageURI = Uri.parse("package:" + item.appIntent.getComponent().getPackageName());
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                getContext().startActivity(uninstallIntent);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent ev) {
        boolean r = super.dispatchDragEvent(ev);
        if (r && (ev.getAction() == DragEvent.ACTION_DRAG_STARTED || ev.getAction() == DragEvent.ACTION_DRAG_ENDED)) {
            // If we got a start or end and the return value is true, our
            // onDragEvent wasn't called by ViewGroup.dispatchDragEvent
            // So we do it here.
            onDragEvent(ev);

            // fix crash on older versions of android
            try {
                super.dispatchDragEvent(ev);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    private void animShowView() {
        if (hideViews != null) {
            for (View view : hideViews) {
                view.animate().alpha(0).setDuration(Math.round(animSpeed / 1.3f)).setInterpolator(new AccelerateDecelerateInterpolator());
            }
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    show();
                }
            }, Math.round(animSpeed / 1.3f));
        }
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                dragging = true;
                switch (((DragAction) event.getLocalState()).action) {
                    case ACTION:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case APP:
                        deleteIcon.setVisibility(View.VISIBLE);
                        if (LauncherSettings.getInstance(getContext()).generalSettings.desktopMode != Desktop.DesktopMode.ShowAllApps)
                            removeIcon.setVisibility(View.VISIBLE);
                        infoIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case APP_DRAWER:
                        deleteIcon.setVisibility(View.VISIBLE);
                        infoIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case WIDGET:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case GROUP:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case SHORTCUT:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                }
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                return true;
            case DragEvent.ACTION_DROP:
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                home.dock.setHideGrid(true);
                for (CellContainer cellContainer : home.desktop.pages) {
                    cellContainer.setHideGrid(true);
                }
                dragging = false;
                hide().withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        removeIcon.setVisibility(View.GONE);
                        infoIcon.setVisibility(View.GONE);
                        deleteIcon.setVisibility(View.GONE);
                        if (hideViews != null) {
                            for (View view : hideViews) {
                                view.animate().alpha(1).setDuration(Math.round(animSpeed / 1.3f)).setInterpolator(new AccelerateDecelerateInterpolator());
                            }
                        }
                    }
                });

                home.dock.revertLastItem();
                home.desktop.revertLastItem();
                return true;
        }
        return false;
    }
}

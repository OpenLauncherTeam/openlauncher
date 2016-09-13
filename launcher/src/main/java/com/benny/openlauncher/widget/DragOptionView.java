package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.Tools;

public class DragOptionView extends CardView{

    private View hideView;
    private LinearLayout horiIconList;
    public boolean dragging = false;

    private TextView removeIcon;
    private TextView infoIcon;
    private TextView deleteIcon;

    final Long animSpeed = 180L;

    public DragOptionView(Context context) {
        super(context);
        init();
    }

    public DragOptionView(Context context, AttributeSet attr) {
        super(context,attr);
        init();
    }

    public void setAutoHideView(View v){
        hideView = v;
    }

    private void init(){
        setCardElevation(Tools.convertDpToPixel(8,getContext()));
        setRadius(Tools.convertDpToPixel(2,getContext()));
        setY(-Tools.convertDpToPixel(68,getContext()));
        horiIconList = (LinearLayout)((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_dragoption_horiiconlist, this, false);
        addView(horiIconList);

        deleteIcon = (TextView) horiIconList.findViewById(R.id.deleteIcon);
        deleteIcon.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch(((DragAction)dragEvent.getLocalState()).action){
                            case ACTION_APP_DRAWER:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
                        Desktop.Item item = intent.getParcelableExtra("mDragData");
                        if(item.type == Desktop.Item.Type.APP) {
                            try {
                                Uri packageURI = Uri.parse("package:" + item.actions[0].getComponent().getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
                                getContext().startActivity(uninstallIntent);
                            }
                            catch (Exception e) {

                            }
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        infoIcon = (TextView) horiIconList.findViewById(R.id.infoIcon);
        infoIcon.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch(((DragAction)dragEvent.getLocalState()).action){
                            case ACTION_APP:
                            case ACTION_APP_DRAWER:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        Intent intent = dragEvent.getClipData().getItemAt(0).getIntent();
                        intent.setExtrasClassLoader(Desktop.Item.class.getClassLoader());
                        Desktop.Item item = intent.getParcelableExtra("mDragData");
                        if(item.type == Desktop.Item.Type.APP) {
                            try {
                               getContext().startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + item.actions[0].getComponent().getPackageName())));
                            }
                            catch (Exception e) {
                                Tools.toast(getContext(),R.string.toast_appuninstalled);
                            }
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
        removeIcon = (TextView) horiIconList.findViewById(R.id.removeIcon);
        removeIcon.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch(((DragAction)dragEvent.getLocalState()).action){
                            case ACTION_GROUP:
                            case ACTION_APP:
                            case ACTION_WIDGET:
                                return true;
                        }
                    case DragEvent.ACTION_DRAG_ENTERED:
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        return true;
                    case DragEvent.ACTION_DROP:
                        Home.desktop.consumeRevert();
                        Home.dock.consumeRevert();
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });

        removeIcon.setText(removeIcon.getText(),TextView.BufferType.SPANNABLE);
        infoIcon.setText(infoIcon.getText(),TextView.BufferType.SPANNABLE);
        deleteIcon.setText(deleteIcon.getText(),TextView.BufferType.SPANNABLE);

        for (int i = 0 ; i < horiIconList.getChildCount() ; i ++){
            horiIconList.getChildAt(i).setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchDragEvent(DragEvent ev){
        boolean r = super.dispatchDragEvent(ev);
        if (r && (ev.getAction() == DragEvent.ACTION_DRAG_STARTED
                || ev.getAction() == DragEvent.ACTION_DRAG_ENDED)){
            // If we got a start or end and the return value is true, our
            // onDragEvent wasn't called by ViewGroup.dispatchDragEvent
            // So we do it here.
            onDragEvent(ev);
            super.dispatchDragEvent(ev);
        }
        return r;
    }

    private void animShowView(){
        if (hideView != null)
            hideView.animate().alpha(0).setDuration(Math.round(animSpeed/1.3f)).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    animate().y(0).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());

                }
            });
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch(event.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                dragging = true;
                switch(((DragAction)event.getLocalState()).action){
                    case ACTION_APP:
                        removeIcon.setVisibility(View.VISIBLE);
                        infoIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        Home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : Home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case ACTION_APP_DRAWER:
                        deleteIcon.setVisibility(View.VISIBLE);
                        infoIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        Home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : Home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case ACTION_WIDGET:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        for (CellContainer cellContainer : Home.desktop.pages)
                            cellContainer.setHideGrid(false);
                        return true;
                    case ACTION_GROUP:
                        removeIcon.setVisibility(View.VISIBLE);
                        animShowView();

                        Home.dock.setHideGrid(false);
                        for (CellContainer cellContainer : Home.desktop.pages)
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
                Home.dock.setHideGrid(true);
                for (CellContainer cellContainer : Home.desktop.pages)
                    cellContainer.setHideGrid(true);

                dragging = false;
                if (hideView != null){
                    hideView.setAlpha(0);
                }
                animate().y(-Tools.convertDpToPixel(68,getContext())).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        removeIcon.setVisibility(View.GONE);
                        infoIcon.setVisibility(View.GONE);
                        deleteIcon.setVisibility(View.GONE);

                        if (hideView != null)
                        hideView.animate().alpha(1).setDuration(Math.round(animSpeed/1.3f)).setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                });
                return true;
        }
        return false;
    }
}

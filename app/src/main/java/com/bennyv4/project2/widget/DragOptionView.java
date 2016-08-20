package com.bennyv4.project2.widget;

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

import com.bennyv4.project2.R;
import com.bennyv4.project2.util.DragAction;
import com.bennyv4.project2.util.Tools;

public class DragOptionView extends CardView{

    private View hideView;
    private LinearLayout horiIconList;
    public boolean dragging = false;

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

        horiIconList.findViewById(R.id.deleteIcon).setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch((DragAction)dragEvent.getLocalState()){
                            case ACTION_APP:
                                return true;
                        }
                        return false;
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
        horiIconList.findViewById(R.id.infoIcon).setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch(dragEvent.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch((DragAction)dragEvent.getLocalState()){
                            case ACTION_APP:
                                return true;
                        }
                        return false;
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
                                Tools.toast(getContext(),R.string.headsup_appuninstalled);
                            }
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        return true;
                }
                return false;
            }
        });
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
        }
        return r;
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        final Long animSpeed = 200L;
        final Long animDelay = 200L;
        switch(event.getAction()){
            case DragEvent.ACTION_DRAG_STARTED:
                dragging = true;
                switch((DragAction)event.getLocalState()){
                    case ACTION_APP:
                        removeCallbacks(null);
                        if (hideView != null)
                            hideView.animate().alpha(0).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());

                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animate().y(0).setStartDelay(animDelay).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
                            }
                        },animDelay/2);
                        return true;
                }
                return false;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                dragging = false;
                if (hideView != null){
                    hideView.setAlpha(0);
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            hideView.animate().alpha(1).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
                        }
                    },animDelay*2);
                }
                animate().y(-Tools.convertDpToPixel(68,getContext())).setDuration(animSpeed).setInterpolator(new AccelerateDecelerateInterpolator());
                return true;
        }
        return false;
    }
}

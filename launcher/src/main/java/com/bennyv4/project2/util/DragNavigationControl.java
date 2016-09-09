package com.bennyv4.project2.util;

import android.os.Handler;
import android.view.DragEvent;
import android.view.View;

import com.bennyv4.project2.activity.Home;

public class DragNavigationControl {

    Handler l;
    Runnable left, right;
    boolean leftok = true, rightok = true;

    View rightView,leftView;

    public DragNavigationControl(View left, View right) {
        rightView = right;
        leftView = left;
        init();
    }

    private void init() {
        l = new Handler();
        right = new Runnable() {
            @Override
            public void run() {
                if (Home.desktop.getCurrentItem() < Home.desktop.pageCount-1)
                    Home.desktop.setCurrentItem(Home.desktop.getCurrentItem() + 1);
                else if (Home.desktop.getCurrentItem() == Home.desktop.pageCount-1)
                    Home.desktop.addPageRight();
                l.postDelayed(this, 1000);
            }
        };
        left = new Runnable() {
            @Override
            public void run() {
                if (Home.desktop.getCurrentItem() > 0)
                    Home.desktop.setCurrentItem(Home.desktop.getCurrentItem() - 1);
                else if (Home.desktop.getCurrentItem() == 0)
                    Home.desktop.addPageLeft();
                l.postDelayed(this, 1000);
            }
        };

        leftView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction)dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_WIDGET:
                            case ACTION_APP_DRAWER:
                            case ACTION_GROUP:
                                leftView.animate().alpha(1);
                                return true;
                        }
                        return false;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (leftok) {
                            leftok = false;
                            l.post(left);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        l.removeCallbacksAndMessages(null);
                        rightok = true;
                        leftok = true;
                        return true;
                    case DragEvent.ACTION_DROP:
                        return false;
                    case DragEvent.ACTION_DRAG_ENDED:
                        l.removeCallbacksAndMessages(null);
                        rightok = true;
                        leftok = true;
                        leftView.animate().alpha(0);
                        return true;
                }
                return false;
            }
        });
        rightView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction)dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_WIDGET:
                            case ACTION_APP_DRAWER:
                            case ACTION_GROUP:
                                rightView.animate().alpha(1);
                                return true;
                        }
                        return false;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        return true;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        if (rightok) {
                            rightok = false;
                            l.post(right);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        l.removeCallbacksAndMessages(null);
                        rightok = true;
                        leftok = true;
                        return true;
                    case DragEvent.ACTION_DROP:
                        return false;
                    case DragEvent.ACTION_DRAG_ENDED:
                        l.removeCallbacksAndMessages(null);
                        rightok = true;
                        leftok = true;
                        rightView.animate().alpha(0);
                        return true;
                }
                return false;
            }
        });
    }
}

package com.benny.openlauncher.viewutil;

import android.os.Handler;
import android.view.DragEvent;
import android.view.View;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.util.DragAction;

public class DragNavigationControl {

    private static boolean leftok = true, rightok = true;

    public static void init(final Home home, final View leftView, final View rightView) {
        final Handler l = new Handler();
        final Runnable right = new Runnable() {
            @Override
            public void run() {
                if (home.desktop.getCurrentItem() < home.desktop.pageCount - 1)
                    home.desktop.setCurrentItem(home.desktop.getCurrentItem() + 1);
                else if (home.desktop.getCurrentItem() == home.desktop.pageCount - 1)
                    home.desktop.addPageRight();
                l.postDelayed(this, 1000);
            }
        };
        final Runnable left = new Runnable() {
            @Override
            public void run() {
                if (home.desktop.getCurrentItem() > 0)
                    home.desktop.setCurrentItem(home.desktop.getCurrentItem() - 1);
                else if (home.desktop.getCurrentItem() == 0)
                    home.desktop.addPageLeft();
                l.postDelayed(this, 1000);
            }
        };

        leftView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_WIDGET:
                            case ACTION_APP_DRAWER:
                            case ACTION_GROUP:
                            case ACTION_SHORTCUT:
                            case ACTION_LAUNCHER:
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
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case ACTION_APP:
                            case ACTION_WIDGET:
                            case ACTION_APP_DRAWER:
                            case ACTION_GROUP:
                            case ACTION_SHORTCUT:
                            case ACTION_LAUNCHER:
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

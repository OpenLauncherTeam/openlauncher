package com.benny.openlauncher.core.viewutil;

import android.os.Handler;
import android.view.DragEvent;
import android.view.View;

import com.benny.openlauncher.core.activity.CoreHome;
import com.benny.openlauncher.core.util.DragAction;

public class DragNavigationControl {

    private static boolean leftok = true, rightok = true;

    public static void init(final CoreHome home, final View leftView, final View rightView) {
        final Handler l = new Handler();
        final Runnable right = new Runnable() {
            @Override
            public void run() {
                if (home.getDesktop().getCurrentItem() < home.getDesktop().getPageCount() - 1)
                    home.getDesktop().setCurrentItem(home.getDesktop().getCurrentItem() + 1);
                else if (home.getDesktop().getCurrentItem() == home.getDesktop().getPageCount() - 1)
                    home.getDesktop().addPageRight(true);
                l.postDelayed(this, 1000);
            }
        };
        final Runnable left = new Runnable() {
            @Override
            public void run() {
                if (home.getDesktop().getCurrentItem() > 0)
                    home.getDesktop().setCurrentItem(home.getDesktop().getCurrentItem() - 1);
                else if (home.getDesktop().getCurrentItem() == 0)
                    home.getDesktop().addPageLeft(true);
                l.postDelayed(this, 1000);
            }
        };

        leftView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                switch (dragEvent.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        switch (((DragAction) dragEvent.getLocalState()).action) {
                            case APP:
                            case WIDGET:
                            case APP_DRAWER:
                            case GROUP:
                            case SHORTCUT:
                            case ACTION:
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
                            case APP:
                            case WIDGET:
                            case APP_DRAWER:
                            case GROUP:
                            case SHORTCUT:
                            case ACTION:
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

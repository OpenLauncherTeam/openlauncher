package com.bennyv4.project2.widget;

import android.animation.Animator;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bennyv4.project2.Home;
import com.bennyv4.project2.R;
import com.bennyv4.project2.util.AppManager;
import com.bennyv4.project2.util.GroupIconDrawable;
import com.bennyv4.project2.util.LauncherSettings;
import com.bennyv4.project2.util.Tools;

public class GroupPopupView extends FrameLayout {

    CellContainer cc;
    CardView popup;

    PopupWindow p;

    public GroupPopupView(Context context) {
        super(context);
        init();
    }

    public GroupPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bringToFront();
        popup = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_grouppopup, null, false);
        popup.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
        cc = (CellContainer) popup.getChildAt(0);

        setVisibility(View.INVISIBLE);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.INVISIBLE);
                p.dismiss();
            }
        });
    }

    public void showWindowV(final Desktop.Item item, final View view, final boolean fromDock) {
        setVisibility(View.VISIBLE);
        final Context c = view.getContext();

        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.actions.length);
        cc.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tools.convertDpToPixel(LauncherSettings.getInstance(c).generalSettings.iconSize, c);

        popup.getLayoutParams().width = (iconSize + iconSize / 3) * cellSize[0];
        popup.getLayoutParams().height = (iconSize + iconSize / 2 + iconSize / 4) * cellSize[1];

        cc.removeAllViews();
        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.actions.length - 1) continue;
                final AppManager.App app = AppManager.getInstance(c).findApp(item.actions[y2 * cellSize[0] + x2].getComponent().getPackageName(), item.actions[y2 * cellSize[0] + x2].getComponent().getClassName());

                if (app == null)continue;

                FrameLayout itemView = new FrameLayout(getContext());
                final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, itemView, false);
                itemView.addView(item_layout);

                TextView tv = (TextView) item_layout.findViewById(R.id.tv);
                ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

                iv.getLayoutParams().width = iconSize;
                iv.getLayoutParams().height = iconSize;

                tv.setText(app.appName);
                iv.setImageDrawable(app.icon);

                final Intent act = item.actions[y2 * cellSize[0] + x2];
                itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.remove(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).remove(item);
                        item.removeActions(act);
                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.add(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).add(item);

                        p.dismiss();


                        AppManager.App[] apps = new AppManager.App[item.actions.length];
                        for (int i = 0; i < item.actions.length; i++) {
                            apps[i] = AppManager.getInstance(getContext()).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
                            if (apps[i] == null)
                                return true;
                        }
                        final Bitmap[] icons = new Bitmap[4];
                        for (int i = 0; i < 4; i++) {
                            if (i < apps.length)
                                icons[i] = Tools.drawableToBitmap(apps[i].icon);
                            else
                                icons[i] = Tools.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
                        }
                        if (((GroupIconDrawable) ((ImageView) view.findViewById(R.id.iv)).getDrawable()).v == null)
                            ((ImageView) view.findViewById(R.id.iv)).setImageDrawable(new GroupIconDrawable(icons, Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext())));
                        else
                            ((ImageView) view.findViewById(R.id.iv)).setImageDrawable(new GroupIconDrawable(icons, Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext()), view));
                        view.performClick();
                        return true;
                    }
                });
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Tools.createScaleInScaleOutAnim(view, new Runnable() {
                            @Override
                            public void run() {
                                p.dismiss();
                                setVisibility(View.INVISIBLE);
                                Tools.startApp(c, app);
                            }
                        });
                    }
                });
                cc.addViewToGrid(itemView, x2, y2, 1, 1);
            }
        }

        p = new PopupWindow(popup, popup.getLayoutParams().width, popup.getLayoutParams().height);
        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (view.getScaleX() != 1)
                    view.animate().setDuration(200).scaleX(1f).scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator());
                else
                    view.findViewById(R.id.iv).animate().setDuration(200).scaleX(1f).scaleY(1f).setInterpolator(new AccelerateDecelerateInterpolator());

                ((GroupIconDrawable) ((ImageView) view.findViewById(R.id.iv)).getDrawable()).popBack();
            }
        });
        p.showAsDropDown(view);
    }

    //region Can be reuse
//    public void showWindow(Desktop.Item item,float x,float y){
//        setVisibility(View.VISIBLE);
//
//        int[] cellSize = GroupDef.getCellSize(item.actions.length);
//        cc.setGridSize(cellSize[0],cellSize[1]);
//
//        int iconSize = Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize,getContext());
//
//        popup.getLayoutParams().width = iconSize * cellSize[0] + iconSize/2;
//        popup.getLayoutParams().height = iconSize * cellSize[1] + iconSize/2;
//
//        x = x-iconSize;
//        y = y-iconSize;
//        popup.setTranslationX(x);
//        popup.setTranslationY(y);
//
//        cc.removeAllViews();
//        for (int x2 = 0; x2 < cellSize[0]; x2++) {
//            for (int y2 = 0; y2 < cellSize[1]; y2++) {
//                if (y2 * cellSize[0] + x2 > item.actions.length-1)return;
//                final AppManager.App app = AppManager.getInstance(getContext()).findApp(item.actions[y2 * cellSize[0] + x2].getComponent().getPackageName(), item.actions[y2 * cellSize[0] + x2].getComponent().getClassName());
//                ImageView v = new ImageView(getContext());
//                v.setImageDrawable(app.icon);
//                LayoutParams lp = new FrameLayout.LayoutParams(iconSize, iconSize);
//                lp.gravity = Gravity.CENTER;
//                v.setLayoutParams(lp);
//
//                final FrameLayout l = new FrameLayout(getContext());
//                l.addView(v);
//
//                l.setOnClickListener(new OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        Tools.createScaleInScaleOutAnim(view, new Runnable() {
//                            @Override
//                            public void run() {
//                                Tools.startApp(getContext(),app);
//                            }
//                        });
//                    }
//                });
//                if (app != null)
//                    cc.addViewToGrid(l,x2,y2,1,1);
//            }
//        }
//
//        animateViewShow(popup,x,y);
//    }
//
//    private void animateViewHide(View view,float x,float y){
//        int finalRadius = Math.max(view.getWidth(),view.getHeight());
//        Animator appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(view
//                ,(int)x,(int)y,finalRadius
//                ,Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize,getContext()));
//        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        appDrawerAnimator.setDuration(200);
//        appDrawerAnimator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator p1) {}
//
//            @Override
//            public void onAnimationEnd(Animator p1) {
//                setVisibility(View.INVISIBLE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator p1) {}
//
//            @Override
//            public void onAnimationRepeat(Animator p1) {}
//        });
//        appDrawerAnimator.start();
//    }
//
//    private void animateViewShow(View view,float x,float y){
//        curX=x;
//        curY=y;
//        int finalRadius = Math.max(view.getWidth(),view.getHeight());
//        Animator appDrawerAnimator = io.codetail.animation.ViewAnimationUtils.createCircularReveal(view
//                ,(int)x,(int)y
//                ,Tools.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize,getContext())
//                ,finalRadius);
//        appDrawerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//        appDrawerAnimator.setDuration(200);
//        appDrawerAnimator.start();
//

    public static class GroupDef {
        public static int maxItem = 12;

        public static int[] getCellSize(int count) {
            if (count <= 1)
                return new int[]{1, 1};
            if (count <= 2)
                return new int[]{2, 1};
            if (count <= 4)
                return new int[]{2, 2};
            if (count <= 6)
                return new int[]{3, 2};
            if (count <= 9)
                return new int[]{3, 3};
            if (count <= 12)
                return new int[]{4, 3};

            return new int[]{0, 0};
        }
    }
}

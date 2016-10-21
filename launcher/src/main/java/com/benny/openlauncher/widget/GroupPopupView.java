package com.benny.openlauncher.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.ViewUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.DragAction;
import com.benny.openlauncher.util.GoodDragShadowBuilder;
import com.benny.openlauncher.util.GroupIconDrawable;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tool;

public class GroupPopupView extends FrameLayout {

//    CellContainer cc;
//    TextView tv;
//    CardView popup;
//
//    PopupWindow p;

    CardView popupParent;
    CellContainer cellContainer;
    TextView title;

    boolean init = false;

    PopupWindow.OnDismissListener dismissListener;

    public GroupPopupView(Context context) {
        super(context);
        init();
    }

    public GroupPopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (isInEditMode())return;
        init = false;

        bringToFront();
        popupParent = (CardView) LayoutInflater.from(getContext()).inflate(R.layout.view_grouppopup, this, false);
        cellContainer = (CellContainer) popupParent.findViewById(R.id.cc);
        title = (TextView) popupParent.findViewById(R.id.tv);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dismissListener != null)
                    dismissListener.onDismiss();
                setVisibility(View.INVISIBLE);
                dismissPopup();
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!init){
            init = true;
            setVisibility(View.INVISIBLE);
        }
    }

    public void dismissPopup(){
        removeAllViews();
        cellContainer.removeAllViews();
        setVisibility(View.INVISIBLE);
    }

    public boolean showWindowV(final Desktop.Item item, final View view, final boolean fromDock) {
        if (getVisibility() == View.VISIBLE)return false;

        setVisibility(View.VISIBLE);
        popupParent.setVisibility(View.VISIBLE);
        final Context c = view.getContext();

        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.actions.length);
        cellContainer.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tool.convertDpToPixel(LauncherSettings.getInstance(c).generalSettings.iconSize, c);
        int textHeight = Tool.convertDpToPixel(22,c);

        int contentPadding = Tool.convertDpToPixel(5,c);

        for (int x2 = 0; x2 < cellSize[0]; x2++) {
            for (int y2 = 0; y2 < cellSize[1]; y2++) {
                if (y2 * cellSize[0] + x2 > item.actions.length - 1) continue;
                final AppManager.App app = AppManager.getInstance(c).findApp(item.actions[y2 * cellSize[0] + x2].getComponent().getPackageName(), item.actions[y2 * cellSize[0] + x2].getComponent().getClassName());

                if (app == null)continue;

                final FrameLayout itemView = new FrameLayout(getContext());
                final ViewGroup item_layout = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.item_app, itemView, false);
                itemView.addView(item_layout);

                TextView tv = (TextView) item_layout.findViewById(R.id.tv);
                ImageView iv = (ImageView) item_layout.findViewById(R.id.iv);

                iv.getLayoutParams().width = iconSize;
                iv.getLayoutParams().height = iconSize;

                tv.getLayoutParams().height = textHeight;

                tv.setText(app.appName);
                iv.setImageDrawable(app.icon);

                final Intent act = item.actions[y2 * cellSize[0] + x2];
                itemView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view2) {
//                        Home.desktop.previousPage = Home.desktop.getCurrentItem();
//                        Home.desktop.previousItemView = view;
//                        Home.desktop.previousItem = Desktop.Item.fromGroupItem(item);

                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.remove(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).remove(item);
                        item.removeActions(act);
                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.add(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).add(item);

                        AppManager.App[] apps = new AppManager.App[item.actions.length];
                        for (int i = 0; i < item.actions.length; i++) {
                            apps[i] = AppManager.getInstance(getContext()).findApp(item.actions[i].getComponent().getPackageName(), item.actions[i].getComponent().getClassName());
                            if (apps[i] == null)
                                return true;
                        }
                        final Bitmap[] icons = new Bitmap[4];
                        for (int i = 0; i < 4; i++) {
                            if (i < apps.length)
                                icons[i] = Tool.drawableToBitmap(apps[i].icon);
                            else
                                icons[i] = Tool.drawableToBitmap(new ColorDrawable(Color.TRANSPARENT));
                        }
                        if (((GroupIconDrawable) ((ImageView) view.findViewById(R.id.iv)).getDrawable()).v == null)
                            ((ImageView) view.findViewById(R.id.iv)).setImageDrawable(new GroupIconDrawable(icons, Tool.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext())));
                        else
                            ((ImageView) view.findViewById(R.id.iv)).setImageDrawable(new GroupIconDrawable(icons, Tool.convertDpToPixel(LauncherSettings.getInstance(getContext()).generalSettings.iconSize, getContext()), view));

                        AppManager.App dapps = AppManager.getInstance(getContext()).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                        if (dapps == null)
                            return true;

                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                        Intent i = new Intent();
                        i.putExtra("mDragData",Desktop.Item.newAppItem(dapps));
                        ClipData data = ClipData.newIntent("mDragIntent", i);
                        itemView.startDrag(data, new GoodDragShadowBuilder(itemView),new DragAction(DragAction.Action.ACTION_APP,0), 0);

                        dismissPopup();
                        return true;
                    }
                });
                itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Tool.createScaleInScaleOutAnim(view, new Runnable() {
                            @Override
                            public void run() {
                                dismissPopup();
                                setVisibility(View.INVISIBLE);
                                Tool.startApp(c, app);
                            }
                        });
                    }
                });
                cellContainer.addViewToGrid(itemView, x2, y2, 1, 1);
            }
        }
        title.setText(item.name);
        title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tool.askForText("Rename", item.name, getContext(), new Tool.OnTextGotListener() {
                    @Override
                    public void hereIsTheText(String str) {
                        if (str.isEmpty())return;
                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.remove(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).remove(item);
                        item.name = str;
                        if (fromDock)
                            LauncherSettings.getInstance(getContext()).dockData.add(item);
                        else
                            LauncherSettings.getInstance(getContext()).desktopData.get(Home.desktop.getCurrentItem()).add(item);
                        title.setText(str);
                    }
                });
            }
        });

        dismissListener = new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                TextView otv = (TextView) view.findViewById(R.id.tv);
                if (!otv.getText().toString().isEmpty())
                    otv.setText(title.getText());
                ((GroupIconDrawable) ((ImageView) view.findViewById(R.id.iv)).getDrawable()).popBack();
            }
        };

        int popupWidth = contentPadding*4 + popupParent.getContentPaddingLeft() + popupParent.getContentPaddingRight() + (iconSize) * cellSize[0];
        popupParent.getLayoutParams().width = popupWidth;
        int popupHeight = contentPadding*2 + popupParent.getContentPaddingTop() + popupParent.getContentPaddingBottom() + Tool.convertDpToPixel(30, c)
                + (iconSize + textHeight) * cellSize[1];
        popupParent.getLayoutParams().height = popupHeight;

        int[] coord = new int[2];
        view.getLocationInWindow(coord);

        coord[0] += view.getWidth()/2;
        coord[1] += view.getHeight()/2;

        coord[0] -= popupWidth/2;
        coord[1] -= popupHeight/2;

        int width = getWidth();
        int height = getHeight();

        if (coord[0]+popupWidth > width){
            coord[0] += width - (coord[0]+popupWidth);
        }
        if (coord[1]+popupHeight > height){
            coord[1] += height - (coord[1]+popupHeight);
        }
        if (coord[0]< 0){
            coord[0] -= view.getWidth()/2;
            coord[0] += popupWidth/2;
        }
        if (coord[1] < 0){
            coord[1] -= view.getHeight()/2;
            coord[1] += popupHeight/2;
        }

        popupParent.setPivotX(0);
        popupParent.setPivotX(0);
        popupParent.setX(coord[0]);
        popupParent.setY(coord[1]);

        addView(popupParent);
        return true;
    }

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

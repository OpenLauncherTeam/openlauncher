package com.benny.openlauncher.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.R;
import com.benny.openlauncher.util.AppManager;
import com.benny.openlauncher.util.GroupIconDrawable;
import com.benny.openlauncher.util.LauncherSettings;
import com.benny.openlauncher.util.Tools;

public class GroupPopupView extends FrameLayout {

    CellContainer cc;
    TextView tv;
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
        cc = (CellContainer) popup.findViewById(R.id.cc);
        tv = (TextView) popup.findViewById(R.id.tv);

        setVisibility(View.INVISIBLE);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.INVISIBLE);
                p.dismiss();
            }
        });
    }

    public boolean showWindowV(final Desktop.Item item, final View view, final boolean fromDock) {
        if (getVisibility() == View.VISIBLE)return false;

        setVisibility(View.VISIBLE);
        final Context c = view.getContext();

        int[] cellSize = GroupPopupView.GroupDef.getCellSize(item.actions.length);
        cc.setGridSize(cellSize[0], cellSize[1]);

        int iconSize = Tools.convertDpToPixel(LauncherSettings.getInstance(c).generalSettings.iconSize, c);

        popup.getLayoutParams().width = (int)(iconSize + iconSize / 2.5f) * cellSize[0];
        popup.getLayoutParams().height = (iconSize*2) * cellSize[1]+ popup.getPaddingBottom();

        cc.removeAllViews();
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

                        AppManager.App dapps = AppManager.getInstance(getContext()).findApp(act.getComponent().getPackageName(), act.getComponent().getClassName());
                        if (dapps == null)
                            return true;

                        p.dismiss();

                        setVisibility(View.INVISIBLE);
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
        tv.setText(item.name);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Tools.askForText("Rename", item.name, getContext(), new Tools.OnTextGotListener() {
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
                        tv.setText(str);
                    }
                });
            }
        });

        p = new PopupWindow(popup, popup.getLayoutParams().width, popup.getLayoutParams().height);
        p.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                TextView otv = (TextView) view.findViewById(R.id.tv);
                if (!otv.getText().toString().isEmpty())
                    otv.setText(tv.getText());
                ((GroupIconDrawable) ((ImageView) view.findViewById(R.id.iv)).getDrawable()).popBack();
            }
        });
        p.showAsDropDown(view,0,-view.getHeight());
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

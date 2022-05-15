package com.benny.openlauncher.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.model.Item;

public class WidgetContainer extends FrameLayout {
    View ve;
    View he;
    View vl;
    View hl;

    final Runnable action = new Runnable() {
        @Override
        public void run() {
            ve.animate().scaleY(0).scaleX(0);
            he.animate().scaleY(0).scaleX(0);
            vl.animate().scaleY(0).scaleX(0);
            hl.animate().scaleY(0).scaleX(0);
        }
    };

    public WidgetContainer(Context context, WidgetView widgetView, Item item) {
        super(context);

        addView(widgetView);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_widget_container, this);

        ve = findViewById(R.id.vertexpand);
        he = findViewById(R.id.horiexpand);
        vl = findViewById(R.id.vertless);
        hl = findViewById(R.id.horiless);

        final WidgetContainer widgetContainer = this;
        ve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanY(item.getSpanY() + 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });

        he.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanX(item.getSpanX() + 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });

        vl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanY(item.getSpanY() - 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });

        hl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getScaleX() < 1) return;
                item.setSpanX(item.getSpanX() - 1);
                scaleWidget(widgetContainer, item);
                widgetContainer.removeCallbacks(action);
                widgetContainer.postDelayed(action, 2000);
            }
        });
    }

    public void showResize() {
        ve.animate().scaleY(1).scaleX(1);
        he.animate().scaleY(1).scaleX(1);
        vl.animate().scaleY(1).scaleX(1);
        hl.animate().scaleY(1).scaleX(1);

        postDelayed(action, 2000);
    }

    public void scaleWidget(View view, Item item) {
        item.setSpanX(Math.min(item.getSpanX(), HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanH()));
        item.setSpanX(Math.max(item.getSpanX(), 1));
        item.setSpanY(Math.min(item.getSpanY(), HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellSpanV()));
        item.setSpanY(Math.max(item.getSpanY(), 1));

        HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(false, (CellContainer.LayoutParams) view.getLayoutParams());

        if (!HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().checkOccupied(new Point(item.getX(), item.getY()), item.getSpanX(), item.getSpanY())) {
            CellContainer.LayoutParams newWidgetLayoutParams = new CellContainer.LayoutParams(CellContainer.LayoutParams.WRAP_CONTENT, CellContainer.LayoutParams.WRAP_CONTENT, item.getX(), item.getY(), item.getSpanX(), item.getSpanY());

            // update occupied array
            HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, newWidgetLayoutParams);

            // update the view
            view.setLayoutParams(newWidgetLayoutParams);
            updateWidgetOption(item);

            // update the widget size in the database
            HomeActivity._db.saveItem(item);
        } else {
            Toast.makeText(HomeActivity.Companion.getLauncher().getDesktop().getContext(), R.string.toast_not_enough_space, Toast.LENGTH_SHORT).show();

            // add the old layout params to the occupied array
            HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().setOccupied(true, (CellContainer.LayoutParams) view.getLayoutParams());
        }
    }

    public void updateWidgetOption(Item item) {
        int cellWidth = HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellWidth();
        int cellHeight = HomeActivity.Companion.getLauncher().getDesktop().getCurrentPage().getCellHeight();

        if (cellWidth < 1 || cellHeight < 1) {
            // desktop isn't laid out
            return;
        }

        Bundle newOps = new Bundle();
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, item.getSpanX() * cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, item.getSpanX() * cellWidth);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, item.getSpanY() * cellHeight);
        newOps.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, item.getSpanY() * cellHeight);
        HomeActivity._appWidgetManager.updateAppWidgetOptions(item.getWidgetValue(), newOps);
    }
}

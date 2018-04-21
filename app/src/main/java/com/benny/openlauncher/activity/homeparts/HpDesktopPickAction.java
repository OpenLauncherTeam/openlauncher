package com.benny.openlauncher.activity.homeparts;

import android.graphics.Point;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.interfaces.DialogListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Tool;

public class HpDesktopPickAction implements DialogListener.OnAddAppDrawerItemListener {
    private Home _home;

    public HpDesktopPickAction(Home home) {
        _home = home;
    }

    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(_home, this);
    }

    @Override
    public void onAdd(int type) {
        Point pos = _home.getDesktop().getCurrentPage().findFreeSpace();
        if (pos != null) {
            _home.getDesktop().addItemToCell(Item.newActionItem(type), pos.x, pos.y);
        } else {
            Tool.toast(_home, R.string.toast_not_enough_space);
        }
    }
}

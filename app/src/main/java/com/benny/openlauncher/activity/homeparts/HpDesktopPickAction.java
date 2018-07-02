package com.benny.openlauncher.activity.homeparts;

import android.graphics.Point;

import com.benny.openlauncher.R;
import com.benny.openlauncher.activity.HomeActivity;
import com.benny.openlauncher.interfaces.DialogListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.util.Tool;

public class HpDesktopPickAction implements DialogListener.OnActionDialogListener {
    private HomeActivity _homeActivity;

    public HpDesktopPickAction(HomeActivity homeActivity) {
        _homeActivity = homeActivity;
    }

    public void onPickDesktopAction() {
        Setup.eventHandler().showPickAction(_homeActivity, this);
    }

    @Override
    public void onAdd(int type) {
        Point pos = _homeActivity.getDesktop().getCurrentPage().findFreeSpace();
        if (pos != null) {
            _homeActivity.getDesktop().addItemToCell(Item.newActionItem(type), pos.x, pos.y);
        } else {
            Tool.toast(_homeActivity, R.string.toast_not_enough_space);
        }
    }
}

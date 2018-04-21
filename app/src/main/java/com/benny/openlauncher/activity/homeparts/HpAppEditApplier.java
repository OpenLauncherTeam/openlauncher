package com.benny.openlauncher.activity.homeparts;

import android.graphics.Point;

import com.benny.openlauncher.activity.Home;
import com.benny.openlauncher.interfaces.DialogListener;
import com.benny.openlauncher.manager.Setup;
import com.benny.openlauncher.model.Item;
import com.benny.openlauncher.widget.Desktop;
import com.benny.openlauncher.widget.Dock;

public class HpAppEditApplier implements DialogListener.OnEditDialogListener {
    private Home _home;
    private Item _item;

    public HpAppEditApplier(Home home) {
        _home = home;
    }

    public void onEditItem(final Item item) {
        _item = item;
        Setup.eventHandler().showEditDialog(_home, item, this);
    }

    @Override
    public void onRename(String name) {
        _item.setLabel(name);
        Setup.dataManager().saveItem(_item);
        Point point = new Point(_item.x, _item.y);

        switch (_item.locationInLauncher) {
            case Item.LOCATION_DESKTOP: {
                Desktop desktop = _home.getDesktop();
                desktop.removeItem(desktop.getCurrentPage().coordinateToChildView(point), false);
                desktop.addItemToCell(_item, _item.x, _item.y);
                break;
            }
            case Item.LOCATION_DOCK: {
                Dock dock = _home.getDock();
                _home.getDock().removeItem(dock.coordinateToChildView(point), false);
                dock.addItemToCell(_item, _item.x, _item.y);
                break;
            }
        }
    }
}

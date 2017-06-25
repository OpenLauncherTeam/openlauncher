package com.benny.openlauncher.core.interfaces;

import com.benny.openlauncher.core.activity.Home;
import com.benny.openlauncher.core.widget.Desktop;
import com.benny.openlauncher.core.widget.Dock;

/**
 * Created by Michael on 25.06.2017.
 */

public interface IAppDeleteListener<T extends IApp> {
    void onAppDeleted(T app);
}

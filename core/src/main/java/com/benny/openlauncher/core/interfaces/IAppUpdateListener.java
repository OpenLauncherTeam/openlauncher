package com.benny.openlauncher.core.interfaces;

import java.util.List;

/**
 * Created by Michael on 25.06.2017.
 */

public interface IAppUpdateListener<T extends IApp> {
    void onAppUpdated(List<T> apps);
}

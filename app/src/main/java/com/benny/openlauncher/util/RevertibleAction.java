package com.benny.openlauncher.util;

/**
 * Created by BennyKok on 11/3/2016.
 */

public interface RevertibleAction {
    void revertLastItem();

    void consumeRevert();

    void setLastItem(Object... args);
}

package com.benny.openlauncher.interfaces;

public interface RevertibleAction {
    void revertLastItem();

    void consumeRevert();

    void setLastItem(Object... args);
}

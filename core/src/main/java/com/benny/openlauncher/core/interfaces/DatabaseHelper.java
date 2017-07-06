package com.benny.openlauncher.core.interfaces;

import java.util.List;

public interface DatabaseHelper<ID extends Number, T extends Item<?, ID>> {
    void deleteItem(T item);

    void setItem(T item, int page, int desktop);

    void updateItem(T item, int state);

    void updateItem(T item);

    T getItem(ID id);

    List<List<T>> getDesktop();

    List<T> getDock();
}

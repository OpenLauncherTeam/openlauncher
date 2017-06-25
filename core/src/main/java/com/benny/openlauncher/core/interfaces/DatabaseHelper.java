package com.benny.openlauncher.core.interfaces;

import java.util.List;

/**
 * Created by Michael on 25.06.2017.
 */

public interface DatabaseHelper<T extends Item> {
    void deleteItem(T item);
    void setItem(T item, int page, int desktop);
    void updateItem(T item, int state);
    void updateItem(T item);
    T getItem(int id);

    List<List<T>> getDesktop();
    List<T> getDock();
}

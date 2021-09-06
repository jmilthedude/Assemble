package net.thedudemc.schedulebot.models;

import java.util.List;

public interface DataAccessObject<T> {

    void createTable(String name);

    T select(int id);

    List<T> selectAll();

    int insert(T data);

    void update(T data);

    void delete(int id);

}

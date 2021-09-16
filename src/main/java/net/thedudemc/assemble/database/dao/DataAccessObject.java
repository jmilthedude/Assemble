package net.thedudemc.assemble.database.dao;

import java.util.List;

public interface DataAccessObject<T> {

    void createTable(String name);

    T select(int id);

    List<T> selectAll();

    int insert(T data);

    int update(T data);

    boolean delete(int id);

}

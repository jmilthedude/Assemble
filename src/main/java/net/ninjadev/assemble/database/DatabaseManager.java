package net.ninjadev.assemble.database;

import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.database.dao.ScheduleMessageDAO;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static Connection connection;

    private static ScheduleMessageDAO messageDao;

    private DatabaseManager() {
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            File path = new File("./db/");
            path.mkdirs();
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:db/assemble.sqlite");
            }
        } catch (SQLException | SecurityException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }

        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
    }

    public ScheduleMessageDAO getMessageDao() {
        if (messageDao == null) {
            messageDao = new ScheduleMessageDAO();
            messageDao.createTable("Messages");
            messageDao.addColumnIfMissing();
        }

        return messageDao;
    }
}

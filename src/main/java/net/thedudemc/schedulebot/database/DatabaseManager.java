package net.thedudemc.schedulebot.database;

import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.database.dao.ScheduleMessageDAO;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseManager instance;
    private static Connection connection;

    private final ScheduleMessageDAO messageDao = new ScheduleMessageDAO();

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
                connection = DriverManager.getConnection("jdbc:sqlite:db/schedulebot.sqlite");
            }
        } catch (SQLException | SecurityException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }

        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
    }

    public ScheduleMessageDAO getMessageDao() {
        return this.messageDao;
    }
}

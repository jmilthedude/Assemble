package net.thedudemc.schedulebot.database.dao;

import net.thedudemc.schedulebot.ScheduleBot;
import net.thedudemc.schedulebot.database.DatabaseManager;
import net.thedudemc.schedulebot.init.BotConfigs;
import net.thedudemc.schedulebot.models.ScheduledMessage;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ScheduleMessageDAO implements DataAccessObject<ScheduledMessage> {

    private static final String ID = "ID";
    private static final String TITLE = "Title";
    private static final String CONTENT = "Content";
    private static final String CHANNEL_ID = "ChannelID";
    private static final String OWNER_ID = "OwnerID";
    private static final String EXECUTION_DATE = "ExecutionDate";
    private static final String RECURRING = "Recurring";
    private static final String INTERVAL = "Interval";
    private static final String TIME_UNIT = "TimeUnit";
    private static final String IMAGE = "ImageFileName";

    @Override
    public void createTable(String name) {
        String query = "CREATE TABLE IF NOT EXISTS \"Messages\" (" +
                "\"" + ID + "\" INTEGER NOT NULL UNIQUE," +
                "\"" + TITLE + "\" TEXT NOT NULL," +
                "\"" + CONTENT + "\" TEXT NOT NULL," +
                "\"" + CHANNEL_ID + "\" TEXT NOT NULL," +
                "\"" + OWNER_ID + "\" TEXT NOT NULL," +
                "\"" + EXECUTION_DATE + "\" INTEGER NOT NULL," +
                "\"" + RECURRING + "\" INTEGER NOT NULL," +
                "\"" + INTERVAL + "\" INTEGER," +
                "\"" + TIME_UNIT + "\" TEXT," +
                "\"" + IMAGE + "\" TEXT," +
                "PRIMARY KEY(" + "\"" + ID + "\" AUTOINCREMENT)" +
                ");";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute(query);
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
    }

    @Override
    public ScheduledMessage select(int id) {
        this.createTable("Messages");
        String query = "SELECT * FROM Messages WHERE " + ID + " = ?";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);

                ResultSet result = statement.executeQuery();
                if (result.next()) {
                    String title = result.getString(TITLE);
                    String content = result.getString(CONTENT);
                    long channelId = result.getLong(CHANNEL_ID);
                    long ownerId = result.getLong(OWNER_ID);
                    LocalDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone()).toLocalDateTime();
                    boolean recurring = result.getBoolean(RECURRING);
                    ScheduledMessage.Recurrence recurrence = null;
                    if (recurring) {
                        int interval = result.getInt(INTERVAL);
                        TimeUnit timeUnit = TimeUnit.valueOf(result.getString(TIME_UNIT));
                        recurrence = new ScheduledMessage.Recurrence(interval, timeUnit);
                    }
                    String imageFileName = result.getString(IMAGE);

                    return new ScheduledMessage(id, title, content,
                            channelId, ownerId, executionDate, recurring,
                            recurrence, imageFileName, ScheduledMessage.SetupState.READY
                    );
                }
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return null;
    }

    @Override
    public List<ScheduledMessage> selectAll() {
        this.createTable("Messages");
        List<ScheduledMessage> messages = new ArrayList<>();
        String query = "SELECT * FROM Messages";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = connection.createStatement()) {

                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    int id = result.getInt(ID);
                    String title = result.getString(TITLE);
                    String content = result.getString(CONTENT);
                    long channelId = result.getLong(CHANNEL_ID);
                    long ownerId = result.getLong(OWNER_ID);
                    LocalDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone()).toLocalDateTime();
                    boolean recurring = result.getBoolean(RECURRING);
                    ScheduledMessage.Recurrence recurrence = null;
                    if (recurring) {
                        int interval = result.getInt(INTERVAL);
                        TimeUnit timeUnit = TimeUnit.valueOf(result.getString(TIME_UNIT));
                        recurrence = new ScheduledMessage.Recurrence(interval, timeUnit);
                    }
                    String imageFileName = result.getString(IMAGE);

                    messages.add(new ScheduledMessage(id, title, content,
                            channelId, ownerId, executionDate, recurring,
                            recurrence, imageFileName, ScheduledMessage.SetupState.READY
                    ));
                }
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return messages;
    }

    @Override
    public int insert(ScheduledMessage data) {
        this.createTable("Messages");
        String query = "INSERT INTO Messages " +
                "(" +
                TITLE + ", " +
                CONTENT + ", " +
                CHANNEL_ID + ", " +
                OWNER_ID + ", " +
                EXECUTION_DATE + ", " +
                RECURRING + ", " +
                INTERVAL + ", " +
                TIME_UNIT + ", " +
                IMAGE + ") " +
                "VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, data.getTitle());
                statement.setString(2, data.getContent());
                statement.setLong(3, data.getChannelId());
                statement.setLong(4, data.getOwnerId());
                statement.setLong(5, data.getExecutionDate().atZone(BotConfigs.CONFIG.getTimeZone()).toInstant().toEpochMilli());
                statement.setBoolean(6, data.isRecurring());
                statement.setNull(7, Types.INTEGER);
                statement.setString(8, "");
                if (data.isRecurring()) {
                    statement.setInt(7, data.getRecurrence().getInterval());
                    statement.setString(8, data.getRecurrence().getUnit().toString());
                }
                statement.setString(9, data.getImageFileName() == null ? "" : data.getImageFileName());

                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating message failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating message failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return -1;
    }

    @Override
    public int update(ScheduledMessage data) {

        this.createTable("Messages");
        String query = "UPDATE Messages " +
                "SET " + TITLE + " = ?," +
                " " + CONTENT + " = ?," +
                " " + CHANNEL_ID + " = ?," +
                " " + OWNER_ID + " = ?," +
                " " + EXECUTION_DATE + " = ?," +
                " " + RECURRING + " = ?," +
                " " + INTERVAL + " = ?," +
                " " + TIME_UNIT + " = ?," +
                " " + IMAGE + " = ? WHERE ID = ?";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, data.getTitle());
                statement.setString(2, data.getContent());
                statement.setLong(3, data.getChannelId());
                statement.setLong(4, data.getOwnerId());
                statement.setLong(5, data.getExecutionDate().atZone(BotConfigs.CONFIG.getTimeZone()).toInstant().toEpochMilli());
                statement.setBoolean(6, data.isRecurring());
                statement.setNull(7, Types.INTEGER);
                statement.setString(8, "");
                if (data.isRecurring()) {
                    statement.setInt(7, data.getRecurrence().getInterval());
                    statement.setString(8, data.getRecurrence().getUnit().toString());
                }
                statement.setString(9, data.getImageFileName() == null ? "" : data.getImageFileName());
                statement.setInt(10, data.getId());

                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Updating message failed, no rows affected.");
                }

                return affectedRows;

            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return -1;
    }

    @Override
    public boolean delete(int id) {
        this.createTable("Messages");
        String query = "DELETE FROM Messages WHERE " + ID + " = ?";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public List<Integer> shouldExecute() {
        List<Integer> messageIds = new ArrayList<>();
        String query = "SELECT " + ID + ", " + EXECUTION_DATE + " FROM Messages";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = connection.createStatement()) {

                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    int id = result.getInt(ID);
                    LocalDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone()).toLocalDateTime();
                    if (LocalDateTime.now(BotConfigs.CONFIG.getTimeZone()).isAfter(executionDate)) {
                        messageIds.add(id);
                    }
                }
            }
        } catch (SQLException exception) {
            ScheduleBot.getLogger().error(exception.getMessage());
        }
        return messageIds;
    }

    public List<ScheduledMessage> getMessagesToExecute(List<Integer> messageIds) {
        List<ScheduledMessage> allMessages = selectAll();
        return allMessages.stream().filter(message -> messageIds.contains(message.getId())).collect(Collectors.toList());
    }

}

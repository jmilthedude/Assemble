package net.ninjadev.assemble.database.dao;

import net.ninjadev.assemble.models.ScheduledMessage;
import net.ninjadev.assemble.Assemble;
import net.ninjadev.assemble.database.DatabaseManager;
import net.ninjadev.assemble.init.BotConfigs;

import java.sql.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
    private static final String LAST_DAY = "LastDay";

    public void addColumnIfMissing() {
        String query = "ALTER TABLE \"Messages\" ADD " + LAST_DAY + " INTEGER;";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute(query);
            }
        } catch (SQLException exception) {
            if (exception.getMessage().contains("duplicate")) return;
            Assemble.getLogger().error(exception.getMessage());
        }
    }

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
                "\"" + LAST_DAY + "\" INTEGER," +
                "PRIMARY KEY(" + "\"" + ID + "\" AUTOINCREMENT)" +
                ");";

        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = conn.createStatement()) {
                statement.execute(query);
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
    }

    @Override
    public ScheduledMessage select(int id) {
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
                    ZonedDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone());
                    boolean recurring = result.getBoolean(RECURRING);
                    ScheduledMessage.Recurrence recurrence = null;
                    if (recurring) {
                        int interval = result.getInt(INTERVAL);
                        ChronoUnit timeUnit = ChronoUnit.valueOf(result.getString(TIME_UNIT).toUpperCase());
                        recurrence = new ScheduledMessage.Recurrence(interval, timeUnit);
                    }
                    String imageFileName = result.getString(IMAGE);
                    boolean isLastDay = result.getBoolean(LAST_DAY);

                    return new ScheduledMessage(id, title, content,
                            channelId, ownerId, executionDate, recurring,
                            recurrence, imageFileName, ScheduledMessage.SetupState.READY, isLastDay
                    );
                }
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
        return null;
    }

    @Override
    public List<ScheduledMessage> selectAll() {
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
                    ZonedDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone());
                    boolean recurring = result.getBoolean(RECURRING);
                    ScheduledMessage.Recurrence recurrence = null;
                    if (recurring) {
                        int interval = result.getInt(INTERVAL);
                        ChronoUnit timeUnit = ChronoUnit.valueOf(result.getString(TIME_UNIT).toUpperCase());
                        recurrence = new ScheduledMessage.Recurrence(interval, timeUnit);
                    }
                    String imageFileName = result.getString(IMAGE);
                    boolean isLastDay = result.getBoolean(LAST_DAY);

                    messages.add(new ScheduledMessage(id, title, content,
                            channelId, ownerId, executionDate, recurring,
                            recurrence, imageFileName, ScheduledMessage.SetupState.READY, isLastDay
                    ));
                }
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
        return messages;
    }

    @Override
    public int insert(ScheduledMessage data) {
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
                IMAGE + ", " +
                LAST_DAY + ") " +
                "VALUES(?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, data.getTitle());
                statement.setString(2, data.getContent());
                statement.setLong(3, data.getChannelId());
                statement.setLong(4, data.getOwnerId());
                statement.setLong(5, data.getExecutionDate().toInstant().toEpochMilli());
                statement.setBoolean(6, data.isRecurring());
                statement.setNull(7, Types.INTEGER);
                statement.setString(8, "");
                if (data.isRecurring()) {
                    ScheduledMessage.Recurrence recurrence = data.getRecurrence();
                    if (recurrence != null) {
                        statement.setInt(7, recurrence.getInterval());
                        statement.setString(8, recurrence.getUnit().toString());
                    }
                }
                statement.setString(9, data.getImageFileName() == null ? "" : data.getImageFileName());
                statement.setBoolean(10, data.isLastDay());

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
            Assemble.getLogger().error(exception.getMessage());
        }
        return -1;
    }

    @Override
    public int update(ScheduledMessage data) {
        String query = "UPDATE Messages " +
                "SET " + TITLE + " = ?," +
                " " + CONTENT + " = ?," +
                " " + CHANNEL_ID + " = ?," +
                " " + OWNER_ID + " = ?," +
                " " + EXECUTION_DATE + " = ?," +
                " " + RECURRING + " = ?," +
                " " + INTERVAL + " = ?," +
                " " + TIME_UNIT + " = ?," +
                " " + IMAGE + " = ?," +
                " " + LAST_DAY + " = ? WHERE ID = ?";

        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, data.getTitle());
                statement.setString(2, data.getContent());
                statement.setLong(3, data.getChannelId());
                statement.setLong(4, data.getOwnerId());
                statement.setLong(5, data.getExecutionDate().toInstant().toEpochMilli());
                statement.setBoolean(6, data.isRecurring());
                statement.setNull(7, Types.INTEGER);
                statement.setString(8, "");
                if (data.isRecurring()) {
                    ScheduledMessage.Recurrence recurrence = data.getRecurrence();
                    if (recurrence != null) {
                        statement.setInt(7, recurrence.getInterval());
                        statement.setString(8, recurrence.getUnit().toString());
                    }
                }
                statement.setString(9, data.getImageFileName() == null ? "" : data.getImageFileName());
                statement.setBoolean(10, data.isLastDay());
                statement.setInt(11, data.getId());

                int affectedRows = statement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Updating message failed, no rows affected.");
                }

                return affectedRows;

            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
        return -1;
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM Messages WHERE " + ID + " = ?";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, id);

                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
        return false;
    }

    public List<Integer> getIdsReadyToExecute() {
        List<Integer> messageIds = new ArrayList<>();
        String query = "SELECT " + ID + ", " + EXECUTION_DATE + " FROM Messages";
        try (Connection connection = DatabaseManager.getInstance().getConnection()) {
            try (Statement statement = connection.createStatement()) {

                ResultSet result = statement.executeQuery(query);
                while (result.next()) {
                    int id = result.getInt(ID);
                    ZonedDateTime now = ZonedDateTime.now(BotConfigs.CONFIG.getTimeZone());
                    ZonedDateTime executionDate = Instant.ofEpochMilli(result.getLong(EXECUTION_DATE)).atZone(BotConfigs.CONFIG.getTimeZone());
                    if (executionDate.isBefore(now)) {
                        messageIds.add(id);
                    }
                }
            }
        } catch (SQLException exception) {
            Assemble.getLogger().error(exception.getMessage());
        }
        return messageIds;
    }

    public List<ScheduledMessage> getMessagesToExecute(List<Integer> messageIds) {
        List<ScheduledMessage> allMessages = selectAll();
        return allMessages.stream().filter(message -> messageIds.contains(message.getId())).collect(Collectors.toList());
    }

}

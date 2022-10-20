package org.sdst.dao;

import org.sdst.enums.GoldSource;
import org.sdst.models.Clan;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClanDAO {
    private static ClanDAO instance;
    private static Connection connection;

    private static final String URL = "jdbc:h2:mem:~/test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USERNAME = "";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("org.h2.Driver");

            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);

            String SQLClanTable = "CREATE TABLE IF NOT EXISTS Clan (" +
                    "id INT primary key NOT NULL," +
                    "name VARCHAR(100) NOT NULL," +
                    "balance INT NOT NULL," +
                    "atomicBalance INT NOT NULL)";

            connection.prepareStatement(SQLClanTable).executeUpdate();

            String SQLClanLogTable = "CREATE TABLE IF NOT EXISTS Clan_log (" +
                    "    transaction_id IDENTITY NOT NULL PRIMARY KEY," +
                    "    clan_id INT," +
                    "    FOREIGN KEY(clan_id) REFERENCES Clan(id)," +
                    "    source VARCHAR," +
                    "    gold_previous INT," +
                    "    gold_current INT," +
                    "    gold_difference INT," +
                    "    event_time TIMESTAMP" +
                    ")";
            connection.prepareStatement(SQLClanLogTable).executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void log(Clan clan, GoldSource source, int goldAdded) throws SQLException {
        String SQL = "INSERT INTO Clan_log(clan_id, source, gold_previous, gold_current, gold_difference, event_time)" +
                "VALUES(?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setInt(1, clan.getId());
        statement.setString(2, source.toString());
        statement.setInt(3, clan.getBalance());
        statement.setInt(4, clan.getBalance() + goldAdded);
        statement.setInt(5, goldAdded);
        statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
//        statement.setLong(7, System.currentTimeMillis());

        statement.executeUpdate();
    }

    public static List<String> getLogs(Clan clan) throws SQLException {
        List<String> logs = new ArrayList<>();

        String SQL = "SELECT * FROM Clan_log WHERE clan_id = ?";

        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setInt(1, clan.getId());

        ResultSet resultSet = statement.executeQuery();

        while(resultSet.next()) {
            String sb = "clan id: " + resultSet.getInt("clan_id") +
                    "; timestamp: " + resultSet.getTimestamp("event_time") +
                    "; gold source: " + resultSet.getString("source") +
                    "; previous gold: " + resultSet.getInt("gold_previous") +
                    "; current gold: " + resultSet.getInt("gold_current") +
                    "; gold difference: " + resultSet.getInt("gold_difference");

            logs.add(sb);
        }

        return logs;
    }

    public static List<Clan> get() throws SQLException {
        Statement statement = connection.createStatement();
        String SQL = "SELECT * FROM Clan";

        List<Clan> items = new ArrayList<>();

        ResultSet resultSet = statement.executeQuery(SQL);

        while (resultSet.next()) {
            Clan clan = new Clan();

            clan.setId(resultSet.getInt("id"));
            clan.setName(resultSet.getString("name"));
            clan.setBalance(resultSet.getInt("balance"));
            clan.setAtomicBalance(resultSet.getInt("atomicBalance"));

            items.add(clan);
        }

        return items;
    }

    public static void save(Clan clan) throws SQLException {
        String SQL = "INSERT INTO Clan(id, name, balance, atomicBalance) VALUES(?,?,?,?)";

        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setInt(1, clan.getId());
        statement.setString(2, clan.getName());
        statement.setInt(3, clan.getBalance());
        statement.setInt(4, clan.getAtomicBalance().intValue());

        statement.executeUpdate();
    }

    public static void update(Clan clan) throws SQLException {
        String SQL = "UPDATE Clan SET name=?, balance=?, atomicBalance=? WHERE id=?";

        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setString(1, clan.getName());
        statement.setInt(2, clan.getBalance());
        statement.setInt(3, clan.getAtomicBalance().intValue());
        statement.setInt(4, clan.getId());

        statement.executeUpdate();
    }

    public static Optional<Clan> getItem(int id) throws SQLException {
        Clan clan = null;

        String SQL = "SELECT * FROM Clan WHERE id=?";

        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setInt(1, id);

        ResultSet resultSet = statement.executeQuery();

        if(resultSet.next()) {
            clan.setId(resultSet.getInt("id"));
            clan.setName(resultSet.getString("name"));
            clan.setBalance(resultSet.getInt("balance"));
            clan.setAtomicBalance(resultSet.getInt("atomicBalance"));
        }

        return Optional.ofNullable(clan);
    }

    public static void deleteItem(int id) throws SQLException {
        String SQL = "DELETE * FROM Clan WHERE id=?";

        PreparedStatement statement = connection.prepareStatement(SQL);

        statement.setInt(1, id);

        statement.executeUpdate();
    }
}

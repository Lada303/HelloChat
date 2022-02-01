package Lada303.server;

import java.sql.*;

public class DbServerWork {
    private static DbServerWork db;
    private Connection connection;
    private PreparedStatement prStmtGetNick;
    private PreparedStatement prStmtRegQuery;
    private PreparedStatement prStmtRegUpdate;
    private PreparedStatement prStmtNickUpdate;

    private DbServerWork() {
    }

    protected static DbServerWork getDb(){
        if (db == null) {
            db = new DbServerWork();
        }
        try {
            db.setConnection();
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("DB not connected: " + e.getMessage());
        }
        return db;
    }

    protected void setConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:users.sqlite");
        System.out.println("DB connected");
        prStmtGetNick = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
        System.out.println("PreparedStatement getNick - ok");
        prStmtRegQuery = connection.prepareStatement("SELECT login, nick FROM users WHERE login = ? OR nick = ?;");
        System.out.println("PreparedStatement regQuery - ok");
        prStmtRegUpdate = connection.prepareStatement(
                "INSERT INTO users (login, password, nick) VALUES (?, ?, ?);");
        System.out.println("PreparedStatement regUpdate - ok");
        prStmtNickUpdate = connection.prepareStatement(
                "UPDATE users SET nick = ? WHERE login = ?;");
        System.out.println("PreparedStatement nickUpdate - ok");
    }

    protected void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
            if (prStmtGetNick != null) {
                prStmtGetNick.close();
            }
            if (prStmtRegQuery != null) {
                prStmtRegQuery.close();
            }
            if (prStmtRegUpdate != null) {
                prStmtRegUpdate.close();
            }
            if (prStmtNickUpdate != null) {
                prStmtNickUpdate.close();
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Exc: " + e.getMessage());
        }
    }

    protected boolean isNoConnected() throws SQLException {
        return connection == null || connection.isClosed();
    }

    protected String getNick(String login, String password) throws SQLException {
        prStmtGetNick.setString(1, login);
        prStmtGetNick.setString(2, password);
        ResultSet rs = prStmtGetNick.executeQuery();
        if (rs.next()) {
            System.out.println("return nick = " + rs.getString("nick"));
            return rs.getString("nick");
        }
        System.out.println("User not founded = " + login + " " + password);
        return null;
    }

    protected boolean registration(String login, String password, String nickname) throws SQLException {
        prStmtRegQuery.setString(1, login);
        prStmtRegQuery.setString(2, nickname);
        ResultSet rs = prStmtRegQuery.executeQuery();
        if (rs.next()) {
            System.out.println("login or nick already exist");
            return false;
        }
        prStmtRegUpdate.setString(1, login);
        prStmtRegUpdate.setString(2, password);
        prStmtRegUpdate.setString(3, nickname);
        System.out.println("Insert into row - " + prStmtRegUpdate.executeUpdate());
        return true;
    }

    protected boolean changeNick(String login, String password, String nickname) throws SQLException {
        prStmtGetNick.setString(1, login);
        prStmtGetNick.setString(2, password);
        ResultSet rs = prStmtGetNick.executeQuery();
        if (!rs.next()) {
            System.out.println("login or password not exist");
            return false;
        }
        prStmtNickUpdate.setString(1, nickname);
        prStmtNickUpdate.setString(2, login);
        System.out.println("Update nick into row - " + prStmtNickUpdate.executeUpdate());
        return true;
    }
}

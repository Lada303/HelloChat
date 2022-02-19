package Lada303.server;

import java.sql.*;
import java.util.logging.Logger;

public class DbServerWork {
    private static final Logger LOGGER = Logger.getLogger(DbServerWork.class.getName());
    private static DbServerWork db;
    private Connection connection;
    private PreparedStatement prStmtGetNick;
    private PreparedStatement prStmtRegUpdate;
    private PreparedStatement prStmtNickUpdate;

    private DbServerWork() {
    }

    protected static DbServerWork getDb(){
        if (db == null) {
            db = new DbServerWork();
        }
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LOGGER.warning("DB not connected or error in prepareStatement: " + e.getMessage());
        }
        return db;
    }

    protected void setConnection() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:users.sqlite");
        LOGGER.info("DB connected");
        prStmtGetNick = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
        LOGGER.info("PreparedStatement getNick - ok");
        prStmtRegUpdate = connection.prepareStatement(
                "INSERT INTO users (login, password, nick) VALUES (?, ?, ?);");
        LOGGER.info("PreparedStatement regUpdate - ok");
        prStmtNickUpdate = connection.prepareStatement(
                "UPDATE users SET nick = ? WHERE login = ?;");
        LOGGER.info("PreparedStatement nickUpdate - ok");
    }

    protected void disconnect() {
        try {
            if (prStmtGetNick != null) {
                prStmtGetNick.close();
            }
            if (prStmtRegUpdate != null) {
                prStmtRegUpdate.close();
            }
            if (prStmtNickUpdate != null) {
                prStmtNickUpdate.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            //e.printStackTrace();
            LOGGER.warning("Exc: " + e.getMessage());
        }
    }

    protected boolean isNoConnected() throws SQLException {
        return connection == null || connection.isClosed();
    }

    protected String getNick(String login, String password) {
        String answer = null;
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            prStmtGetNick.setString(1, login);
            prStmtGetNick.setString(2, password);
            ResultSet rs = prStmtGetNick.executeQuery();
            if (rs.next()) {
                answer = rs.getString("nick");
                LOGGER.fine("return nick = " + answer);
            } else {
                LOGGER.fine("User not founded = " + login + " " + password);
            }
            rs.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            LOGGER.warning("Exc: " + e.getMessage());
        }
        return answer;
    }

    protected boolean registration(String login, String password, String nickname) {
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            prStmtRegUpdate.setString(1, login);
            prStmtRegUpdate.setString(2, password);
            prStmtRegUpdate.setString(3, nickname);
            LOGGER.fine("Client reg : insert into row - " + prStmtRegUpdate.executeUpdate());
            return true;
        } catch (SQLException e) {
            //e.printStackTrace();
            LOGGER.warning("Exc: " + e.getMessage());
            return false;
        }
    }

    protected boolean changeNick(String login, String password, String nickname) {
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            prStmtGetNick.setString(1, login);
            prStmtGetNick.setString(2, password);
            ResultSet rs = prStmtGetNick.executeQuery();
            if (rs.next()) {
                prStmtNickUpdate.setString(1, nickname);
                prStmtNickUpdate.setString(2, login);
                LOGGER.fine("Update nick into row - " + prStmtNickUpdate.executeUpdate());
                rs.close();
                return true;
            }
            rs.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            LOGGER.warning("Exc: " + e.getMessage());
        }
        return false;
    }
}

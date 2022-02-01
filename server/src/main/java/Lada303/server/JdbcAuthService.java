package Lada303.server;

import java.sql.*;

public class JdbcAuthService implements AuthService{
    private static JdbcAuthService jdbcAuthService;
    private DbServerWork db;

    public JdbcAuthService(DbServerWork db) {
        this.db = db;
    }

    protected static JdbcAuthService getJdbcAuthService(DbServerWork db){
        if (jdbcAuthService == null) {
            jdbcAuthService = new JdbcAuthService(db);
        }
        return jdbcAuthService;
    }

    @Override
    public String getNick(String login, String password) {
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            return db.getNick(login, password);
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Exc: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            return db.registration(login, password, nickname);
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Exc: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean changeNick(String login, String password, String nickname) {
        try {
            if (db.isNoConnected()) {
                db.setConnection();
            }
            return db.changeNick(login, password, nickname);
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Exc: " + e.getMessage());
            return false;
        }
    }
}

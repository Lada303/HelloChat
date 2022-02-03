package Lada303.server;

public class JdbcAuthService implements AuthService{
    private static JdbcAuthService jdbcAuthService;
    private DbServerWork db;

    private JdbcAuthService(DbServerWork db) {
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
        return db.getNick(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return db.registration(login, password, nickname);
    }

    @Override
    public boolean changeNick(String login, String password, String nickname) {
        return db.changeNick(login, password, nickname);
    }
}

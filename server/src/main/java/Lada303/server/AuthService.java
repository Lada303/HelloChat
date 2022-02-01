package Lada303.server;

public interface AuthService {
    String getNick(String login, String password);
    boolean registration(String login, String password, String nickname);
    boolean changeNick(String login, String password, String nickname);
}

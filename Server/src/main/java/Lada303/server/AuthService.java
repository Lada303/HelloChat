package Lada303.server;

public interface AuthService {
    String getNick(String login, String password);
    public boolean registration(String login, String password, String nickname);
}

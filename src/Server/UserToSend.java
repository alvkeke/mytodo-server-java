package Server;

public class UserToSend {
    private int id;
    private String username;
    private String password;
    private boolean isAdmin;

    public UserToSend(int id, String username, String password, boolean isAdmin){
        this.id = id;
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public String getUsername() {
        return username;
    }

    public int getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }
}

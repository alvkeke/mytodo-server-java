package Server.DataBase;

import Server.UserToSend;

import java.sql.*;
import java.util.ArrayList;

import static Server.DataBase.Constants.*;

public class UsersOperator {

    private Connection conn;
    private Statement stmt;

    public UsersOperator(){

        try {
            Class.forName(CLASS_NAME);
            conn = DriverManager.getConnection(CONNECT_URL, DB_USER, DB_PASS);

            stmt = conn.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUser(String username ,String password){
        try {
            if(!isExist(username)) {
                stmt.execute("insert into `users`(username, password) values('" + username + "','" + password + "')");
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void delUser(String username){
        try {
            if(isExist(username)) {
                stmt.execute("delete from `users` where username='" + username + "';");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int findUser(String username, String password){
        try {
            ResultSet rs = stmt.executeQuery("select id from `users` where username='" + username +"' and password='"+ password+"'");
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean adminLogin(String username, String password){

        String sqlcmd = String.format("select isAdmin from `users` where username='%s' and password='%s'", username, password);
        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            if(rs.getBoolean(1)){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void editUser(String username, String newPassword, boolean isAdmin){
        String sqlcmd = String.format("update `users` set password='%s', isAdmin=%b where username='%s'",
                newPassword, username, isAdmin
        );
        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<UserToSend> getAllUsers(){
        ArrayList<UserToSend> users = new ArrayList<>();

        try {
            ResultSet rs = stmt.executeQuery("select id, username, password, isAdmin from `users`");
            while (rs.next()){
                int id = rs.getInt(1);
                String username = rs.getString(2);
                String password = rs.getString(3);
                boolean isAdmin = rs.getBoolean(4);
                UserToSend user = new UserToSend(id, username, password, isAdmin);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private boolean isExist(String username){

        try {
            ResultSet rs = stmt.executeQuery("select id from `users` where username='"+username+"'");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void close(){
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

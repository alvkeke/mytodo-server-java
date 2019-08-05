package Server.DataBase;

import java.sql.*;

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

    public void changePassword(String username, String newPassword){

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

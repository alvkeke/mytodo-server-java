package Server.DataBase;

import Server.DataStruct.Project;

import java.sql.*;
import java.util.ArrayList;

import static Server.DataBase.Constants.*;
import static Server.DataBase.Constants.DB_PASS;

public class ProjectOperator {

    private Connection conn;
    private Statement stmt;

    public ProjectOperator(){
        try {
            Class.forName(CLASS_NAME);
            conn = DriverManager.getConnection(CONNECT_URL, DB_USER, DB_PASS);

            stmt = conn.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public Project getProject(String username, long proId){
        String sqlcmd = String.format(
                "select * from `projects` proName, proColor, lastModifyTime where username='%s' and proId=%d",
                username, proId);
        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            Project p;
            if(rs.next()){
                p = new Project(proId, rs.getString(1), rs.getInt(2));
                p.setLastModifyTime(rs.getLong(3));
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public ArrayList<Project> getAllProjects(String username){

        ArrayList<Project> projects = new ArrayList<>();

        String sqlcmd = String.format(
                "select proId, proName, proColor, lastModifyTime from `projects` where username='%s'",
                username);
        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            while (rs.next()){
                Project p = new Project(rs.getLong(1), rs.getString(2), rs.getInt(3));
                p.setLastModifyTime(rs.getLong(4));
                projects.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return projects;
    }

    public void addProject(String username, long proId, String proName, int color, long lastModifyTime){
        try {
            if(!isExist(username, proId)) {
                String sqlcmd = String.format(
                        "insert into `projects` (username, proId, proName, proColor, lastModifyTime) values('%s',%d,'%s',%d,%d)",
                        username, proId, proName, color, lastModifyTime);
                stmt.execute(sqlcmd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delProject(String username, long proId){
        try {
            stmt.execute("delete from `projects` where proId="+ proId +" and username='" + username +"'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyProject(String username, long proId, String newName, int newColor, long lastModifyTime){
        String sqlcmd = String.format(
                "update `projects` set proName='%s', proColor=%d,lastModifyTime=%d where username='%s' and proId=%d",
                newName, newColor, lastModifyTime, username, proId);
        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isExist(String username, long proId){
        try {
            ResultSet set = stmt.executeQuery("select * from projects where proId=" + proId +" and username='" + username +"'");
            return set.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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

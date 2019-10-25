package Server.DataBase;

import Server.DataStruct.TaskItem;

import java.sql.*;
import java.util.ArrayList;

import static Server.DataBase.Constants.*;
import static Server.DataBase.Constants.DB_PASS;

public class TasksOperator {

    private Connection conn;
    private Statement stmt;

    public TasksOperator(){
        try {
            Class.forName(CLASS_NAME);
            conn = DriverManager.getConnection(CONNECT_URL, DB_USER, DB_PASS);

            stmt = conn.createStatement();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isExist(String username, long taskId){

        String sqlcmd = String.format("select * from tasks where username='%s' and taskId=%d", username, taskId);
        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList<TaskItem> getAllTasks(String username){
        ArrayList<TaskItem> taskItems = new ArrayList<>();
        String sqlcmd = String.format(
                "select taskId, proId, taskContent, taskTime, taskLevel, isFinished, lastModifyTime from `tasks` " +
                        "where username='%s'",username
        );

        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            while (rs.next()){
                long taskId = rs.getLong(1);
                long proId = rs.getLong(2);
                String todo = rs.getString(3);
                long time = rs.getLong(4);
                int level = rs.getInt(5);
                boolean isFinished = rs.getBoolean(6);
                long lastModifyTime = rs.getLong(7);
                TaskItem t = new TaskItem(proId, taskId, todo, time, level);
                t.setFinished(isFinished);
                t.setLastModifyTime(lastModifyTime);
                taskItems.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskItems;
    }

    public ArrayList<TaskItem> getTasksInProject(String username, long proId){

        ArrayList<TaskItem> taskItems = new ArrayList<>();
        String sqlcmd = String.format(
                "select taskId, taskContent, taskTime, taskLevel, isFinished, lastModifyTime from `tasks` " +
                        "where username='%s' and proId=%d",username, proId
        );

        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            while (rs.next()){
                long taskId = rs.getLong(1);
                String todo = rs.getString(2);
                long time = rs.getLong(3);
                int level = rs.getInt(4);
                boolean isFinished = rs.getBoolean(5);
                long lastModifyTime = rs.getLong(6);
                TaskItem t = new TaskItem(proId, taskId, todo, time, level);
                t.setFinished(isFinished);
                t.setLastModifyTime(lastModifyTime);
                taskItems.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return taskItems;
    }

    public TaskItem getTask(String username, long taskId){
        TaskItem t;
        String sqlcmd = String.format(
                "select proId, taskContent, taskTime, taskLevel, isFinished, lastModifyTime from `tasks` where username='%s' and taskId=%d",
                username, taskId);

        try {
            ResultSet rs = stmt.executeQuery(sqlcmd);
            if(rs.next()){
                long proId = rs.getLong(1);
                String content = rs.getString(2);
                long time = rs.getLong(3);
                int taskLevel = rs.getInt(4);
                boolean isFinished = rs.getBoolean(5);
                long lastModifyTime = rs.getLong(6);
                t = new TaskItem(proId, taskId, content, time, taskLevel);
                t.setFinished(isFinished);
                t.setLastModifyTime(lastModifyTime);
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void addTask(String username, long taskId, long proId, String taskContent, long taskTime,
                        int taskLevel, boolean isFinished, long lastModifyTime){

        if(isExist(username, taskId)){
            return;
        }

        String sqlcmd = String.format(
                "insert into `tasks` (username, taskId, proId, taskContent, taskTime, taskLevel, isFinished, lastModifyTime) " +
                        "value('%s',%d,%d,'%s',%d,%d,%b,%d)",
                username, taskId, proId, taskContent, taskTime, taskLevel, isFinished, lastModifyTime);

        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delTask(String username, long taskId){
        String sqlcmd = String.format("delete from tasks where username='%s' and taskId=%d",username, taskId);

        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delTaskInProject(String username, long proId){
        String sqlcmd = String.format("delete from tasks where username='%s' and proId=%d",username, proId);

        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void modifyTask(String username, long taskId, long newProId, String todo,
                           long time, int level, boolean isFinished, long lastModifyTime){

        String sqlcmd = String.format(
                "update tasks set " +
                        "proId=%d,taskContent='%s',taskTime=%d,taskLevel=%d,isFinished=%b,lastModifyTime=%d " +
                        "where username='%s' and taskId=%d",
                newProId, todo, time, level, isFinished, lastModifyTime, username, taskId
        );
        try {
            stmt.execute(sqlcmd);
        } catch (SQLException e) {
            e.printStackTrace();
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

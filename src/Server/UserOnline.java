package Server;

import Server.DataStruct.Functions;
import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;

import java.util.ArrayList;

public class UserOnline {
    private int userId;
    private String username;
    private int netkey;

    private long lastHeartTime;

    private ArrayList<Project> projsInTmp;
    private ArrayList<TaskItem> tasksInTmp;

    private UserData data;

    UserOnline(int netkey, int userId, String username){

        this.netkey = netkey;
        this.userId = userId;
        this.username = username;

        projsInTmp = new ArrayList<>();
        tasksInTmp = new ArrayList<>();
    }

    public void setLastHeartTime(long lastHeartTime) {
        this.lastHeartTime = lastHeartTime;
    }

    public long getLastHeartTime() {
        return lastHeartTime;
    }

    public String getUsername() {
        return username;
    }

    public int getNetkey() {
        return netkey;
    }

    public int getUserId() {
        return userId;
    }

    public void setData(UserData data) {
        this.data = data;
    }

    public UserData getData() {
        return data;
    }

    void addTmpProject(Project p){
        projsInTmp.add(p);
    }

    void addTmpTask(TaskItem t){
        tasksInTmp.add(t);
    }

    void mergeData(){
        if(projsInTmp == null) return;

        Functions.mergeProjectList(data.getProjects(), projsInTmp);

        ArrayList<TaskItem> tasksOld = new ArrayList<>();

        for(Project p : data.getProjects()){
            tasksOld.addAll(p.getTaskList());
            p.getTaskList().clear();
        }

        Functions.mergeTaskList(tasksOld, tasksInTmp);
        Functions.autoMoveTaskToProject(data.getProjects(), tasksOld);

        projsInTmp.clear();
        tasksInTmp.clear();
    }
}

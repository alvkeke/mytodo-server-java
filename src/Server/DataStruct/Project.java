package Server.DataStruct;


import java.util.ArrayList;
import java.util.Date;

public class Project {

    private String name;
    private ArrayList<TaskItem> tasks;
    private int color;
    private long Id;

    private long lastModifyTime;

    public Project(long projectId, String name, int color){
        this.name = name;
        this.tasks = new ArrayList<>();
        this.color = color;
        this.Id = projectId;
    }

    public void changeName(String name){
        this.name = name;
    }

    public void changeColor(int Color){this.color = Color;}

    public long getId() {
        return Id;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    public void addTask(TaskItem e){
        tasks.add(e);
    }

//    public TaskItem getTask(int pos){
//        return tasks.get(pos);
//    }

    public TaskItem findTask(long taskId){
        for(int i = 0; i<tasks.size(); i++){
            if(tasks.get(i).getId() == taskId){
                return tasks.get(i);
            }
        }
        return null;
    }

    public long getLastModifyTime() {
        return lastModifyTime;
    }

    public void updataLastModifyTime(){
        lastModifyTime = new Date().getTime();
    }

    public void setLastModifyTime(long time){
        lastModifyTime = time;
    }

    public ArrayList<TaskItem> getTaskList(){
        return tasks;
    }

}

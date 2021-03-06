package Server;

import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;

import java.util.ArrayList;

public class UserData {

	private boolean isBusy;

    private int userId;
	private String username;
	private int currentOperatorNetkey;

	private ArrayList<Project> projects;
//	private ArrayList<Project> projsInTmp;
//	private ArrayList<TaskItem> tasksInTmp;

//	private long lastHeartTime;

    public UserData(int userId, String username){
        isBusy = false;

	    projects = new ArrayList<>();
//	    projsInTmp = new ArrayList<>();
//	    tasksInTmp = new ArrayList<>();

		this.currentOperatorNetkey = 0;
		this.userId = userId;
	    this.username = username;
    }

	void setBusy(int netkey) {
    	currentOperatorNetkey = netkey;
		isBusy = true;
	}

	void setFree() {
    	isBusy = false;
	}

	boolean isBusy() {
		return isBusy;
	}

	boolean isOperating(int netkey){
    	return netkey == currentOperatorNetkey;
	}

	void addProject(Project p){
    	projects.add(p);
	}

	void delProject(long proId){
    	for(Project p : projects){
    		if(p.getId() == proId){
    			projects.remove(p);
    			return;
			}
		}
	}

	void addTask(long proId, TaskItem t){
    	for(Project p: projects){
    		if(p.getId() == proId){
    			p.addTask(t);
    			return;
			}
		}
	}

	void deleteTask(long taskId, long proId){
    	for(Project p: projects){
    		if(p.getId() == proId){
    			for(TaskItem e : p.getTaskList()){
					p.getTaskList().remove(e);
					return;
				}
			}
		}
	}

	void modifyProject(long proId, String name, int color, long lastModifyTime){
    	for(Project p : projects){
    		if(p.getId() == proId){
    			p.setLastModifyTime(lastModifyTime);
    			p.changeColor(color);
    			p.changeName(name);
    			return;
			}
		}
	}

	void modifyTask(long taskId, long oldProId, long newProId, String content, long time, int level,
					boolean isFinished, long lastModifyTime){
    	for(Project p : projects){

    		if(p.getId() == oldProId){
    			for(TaskItem t : p.getTaskList()){

    				if(t.getId() == taskId){
    					t.setFinished(isFinished);
    					t.setLastModifyTime(lastModifyTime);
    					t.setLevel(level);
    					t.setTime(time);
    					t.setContent(content);

    					if(oldProId != newProId){
    						for(Project pNew : projects){

    							if(pNew.getId() == newProId){
    								pNew.addTask(t);
    								p.getTaskList().remove(t);
									t.setProId(newProId);

									return;
								}
							}
						}

    					return;
					}
				}
			}
		}
	}

//	void addTmpProject(Project p){
//    	projsInTmp.add(p);
//	}
//
//	void addTmpTask(TaskItem t){
//    	tasksInTmp.add(t);
//	}
//
//	void mergeData(){
//    	if(projsInTmp == null) return;
//
//    	Functions.mergeProjectList(projects, projsInTmp);
//
//		ArrayList<TaskItem> tasksOld = new ArrayList<>();
//
//		for(Project p : projects){
//			tasksOld.addAll(p.getTaskList());
//			p.getTaskList().clear();
//		}
//
//		Functions.mergeTaskList(tasksOld, tasksInTmp);
//		Functions.autoMoveTaskToProject(projects, tasksOld);
//
//		projsInTmp.clear();
//		tasksInTmp.clear();
//	}

	public int getCurrentOperatorNetkey() {
		return currentOperatorNetkey;
	}

	public String getUsername() {
		return username;
	}

    public int getUserId() {
        return userId;
    }

    public ArrayList<Project> getProjects() {
		return projects;
    }
}

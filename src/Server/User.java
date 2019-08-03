package Server;

import Server.DataStruct.Functions;
import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;

import java.util.ArrayList;
import java.util.HashMap;

class User {

	private String username;
	private int netkey;

	private ArrayList<Project> projects;
	private ArrayList<Project> tmpProjs;
	private ArrayList<TaskItem> tmpTasks;
//	private HashMap<Integer, String> confirmList;

	private long lastHeartTime;

    User(int netkey, String username){
	    projects = new ArrayList<>();
	    tmpProjs = new ArrayList<>();
	    tmpTasks = new ArrayList<>();
//		confirmList = new HashMap<>();

		this.netkey = netkey;
	    this.username = username;
    }

    public void setLastHeartTime(long lastHeartTime) {
        this.lastHeartTime = lastHeartTime;
    }

    void initConfirmList(){
//        confirmList.clear();
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

	void addTmpProject(Project p){
    	tmpProjs.add(p);
	}

	void addTmpTask(TaskItem t){
    	tmpTasks.add(t);
	}

	void mergeData(){
    	if(tmpProjs == null) return;

    	Functions.mergeProjectList(projects, tmpProjs);

		ArrayList<TaskItem> tmp = new ArrayList<>();

		for(Project p : projects){
			tmp.addAll(p.getTaskList());
			p.getTaskList().clear();
		}

		Functions.mergeTaskList(tmp, tmpTasks);
		Functions.autoMoveTaskToProject(projects, tmp);

		tmpProjs.clear();
		tmpTasks.clear();
	}

	public int getNetkey() {
		return netkey;
	}

	public String getUsername() {
		return username;
	}

	public ArrayList<Project> getProjects() {
		return projects;
    }
}

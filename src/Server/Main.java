package Server;

import Server.DataBase.ProjectOperator;
import Server.DataBase.TasksOperator;
import Server.DataBase.UsersOperator;
import Server.DataHandler.*;
import Server.DataStruct.Functions;
import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static Server.Constants.*;

public class Main implements HandlerCallback {

	private DatagramSocket socket;
	private boolean stop;

	private HashMap<Integer, User> userList;
	private HashMap<Integer, ArrayList<Integer>> confirmLists;

	private Main(){

	    userList = new HashMap<>();
	    confirmLists = new HashMap<>();

		stop = false;
		try {
			socket = new DatagramSocket(SERVER_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	private void startRecvData(){
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);

		while (!stop){
			Arrays.fill(buf, (byte)0);
			packet.setData(buf);
			try {
				socket.receive(packet);
				byte cmd = packet.getData()[0];
				String data = new String(packet.getData()).substring(1).trim();


				if(cmd == COMMAND_LOGIN){

					String[] userInfo = data.split("\\|");
					if(userInfo.length < 2){
						continue;
					}
					new LoginHandler(this, socket, packet.getSocketAddress()).handle(userInfo[0], userInfo[1]);

				}else {
				    String[] splitMsg = data.split("\\|");

				    if(splitMsg.length < 1){
						System.out.println("<<<<<<<<<<<<<<got error data>>>>>>>>>>>>>>>");
				    	continue;
					}
					int netkey = Integer.parseInt(splitMsg[0]);

					if(userList.get(netkey) == null){
						System.out.println(">unknown user:\n" + (int)cmd +":"+ data);
						String s = COMMAND_YOU_ARE_OFFLINE + "";
						packet.setData(s.getBytes());
						socket.send(packet);
					    continue;
					}

					if (cmd == COMMAND_LOGOUT) {
						new LogoutHandler(this, socket, packet.getSocketAddress()).handle(netkey);
					} else if (cmd == COMMAND_SEND_DATA_BEGIN || cmd == COMMAND_SEND_DATA_PROJS ||
							cmd == COMMAND_SEND_DATA_TASKS || cmd == COMMAND_SEND_DATA_END) {

						new DataRecvHandler(this, socket, packet.getSocketAddress()).handle(cmd, splitMsg);
					} else if (cmd == COMMAND_GET_DATA) {
						ArrayList<Integer> confirmList = new ArrayList<>();
						this.confirmLists.put(netkey, confirmList);
						ArrayList<Project> userProjs = userList.get(netkey).getProjects();
						new DataSender(this, socket, packet.getSocketAddress()).send(netkey, userProjs, confirmList);
					} else if (cmd == COMMAND_CONFIRM_SEND_BEGIN) {

					} else if (cmd == COMMAND_CONFIRM_DATA) {
						int dataId = Integer.parseInt(splitMsg[1]);
						ArrayList<Integer> confirmList = confirmLists.get(netkey);
						confirmList.remove(Integer.valueOf(dataId));
					} else if (cmd == COMMAND_CONFIRM_SEND_END) {
						ArrayList<Integer> confirmList = confirmLists.get(netkey);
						String s;
						if(confirmList.isEmpty()){
							s = COMMAND_RESEND_NO_NEED + String.valueOf(netkey);
						}else{
							s = COMMAND_RESEND_NEED + String.valueOf(netkey);
						}
						packet.setData(s.getBytes());
						socket.send(packet);

					} else if (cmd >= COMMAND_ADD_PROJECT && cmd <= COMMAND_EDIT_TASK) {
						new DataModifier(this, socket, packet.getSocketAddress()).handle(cmd, data);
					} else if (cmd == COMMAND_HEART_BEAT){
						User user = userList.get(netkey);
						user.setLastHeartTime(new Date().getTime());

						printOnlineUser();
					} else {
						System.out.println(">unknown msg:\n" + (int)cmd +":"+ data);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

			ArrayList<Integer> delNetkeys = new ArrayList<>();
			long currentTime = new Date().getTime();
			for(int key: userList.keySet()){
				if(userList.get(key).getLastHeartTime() + 90000 < currentTime){
					delNetkeys.add(key);
				}
			}
			for(int k : delNetkeys){
				userList.remove(k);
			}
		}
	}

	private void stop(){stop = true;}

	public static void main(String[] args){
		Main mc = new Main();

		mc.startRecvData();

	}

	private void printOnlineUser(){
		System.out.println("-------------------------------------------------");
		long currentTime = new Date().getTime();
		for(int netkey : userList.keySet()){
			User u = userList.get(netkey);

			System.out.println(u.getUsername() +"["+ u.getNetkey() +"]: " + (currentTime - u.getLastHeartTime()));
		}
		System.out.println("-------------------------------------------------");
	}

	private void printUserData(int netkey){
		System.out.println(">>>>>>>start print user data:");
		System.out.println("user info: " + userList.get(netkey).getUsername() + "[" + netkey +"]" );
		ArrayList<Project> projects = userList.get(netkey).getProjects();
		printProjectList(projects);
		System.out.println(">>>>>>>end print.\n");
	}

	private void printProjectList(ArrayList<Project> projects){
		for(Project p : projects){
			System.out.println("p: " + p.getId() +":"+ p.getName() +":"+ p.getColor() +":"+ p.getLastModifyTime());
			for(TaskItem e: p.getTaskList()){
				System.out.println("\tt: " + e.getId() +":"+ e.getProId() +":"+ e.getTaskContent() +":"+
						e.getTime() +":"+ e.getLevel() +":"+ e.isFinished() +":"+ e.getLastModifyTime());
			}
		}
	}

	@Override
	public void gotUserLogin(int netkey, int userId, String username) {
		User user = new User(netkey, userId, username);
		user.setLastHeartTime(new Date().getTime());

		userList.put(netkey, user);
		//todo:load user projects and tasks data from the database
        ProjectOperator po = new ProjectOperator();
        TasksOperator to = new TasksOperator();

        ArrayList<Project> projects = po.getAllProjects(username);
        ArrayList<TaskItem> taskItems = to.getAllTasks(username);

        to.close();
        po.close();

		Functions.autoMoveTaskToProject(projects, taskItems);
		user.getProjects().addAll(projects);

		printUserData(netkey);

		printOnlineUser();
	}

	@Override
	public void gotUserLogout(int netkey){
		//todo:save user projects and tasks data to the database
	    userList.remove(netkey);
		printOnlineUser();
	}

	@Override
	public void beginRecvData(int netkey) {
		User user = userList.get(netkey);
		user.initConfirmList();
	}

	@Override
	public void recvProject(int netkey, long proId, String proName, int proColor, long lastModifyTime) {
		User user = userList.get(netkey);

		Project p = new Project(proId, proName, proColor);
		p.setLastModifyTime(lastModifyTime);

        user.addTmpProject(p);
	}

	@Override
	public void recvTask(int netkey, long taskId, long proId, String taskContent, long time, int level, boolean isFinished, long lastModifyTime) {
		User user = userList.get(netkey);

		TaskItem t = new TaskItem(proId, taskId, taskContent, time, level);
		t.setFinished(isFinished);
		t.setLastModifyTime(lastModifyTime);

        user.addTmpTask(t);
	}

	@Override
	public void endRecvData(int netkey) {
	    User s = userList.get(netkey);

		s.mergeData();
		String username = s.getUsername();
		ProjectOperator po = new ProjectOperator();
		TasksOperator to = new TasksOperator();

		for(Project p : s.getProjects()){
			if(po.isExist(username, p.getId())){
				po.modifyProject(username, p.getId(), p.getName(), p.getColor(), p.getLastModifyTime());
			}else{
				po.addProject(username, p.getId(), p.getName(), p.getColor(), p.getLastModifyTime());
			}
			for(TaskItem t : p.getTaskList()){
				if(to.isExist(username, t.getId())){
					to.modifyTask(username, t.getId(), t.getProId(), t.getTaskContent(),
							t.getTime(), t.getLevel(), t.isFinished(), t.getLastModifyTime());
				}else{
					to.addTask(username, t.getId(), t.getProId(), t.getTaskContent(),
							t.getTime(), t.getLevel(), t.isFinished(), t.getLastModifyTime());
				}
			}
		}

		po.close();
		to.close();

		printUserData(netkey);
	}

	@Override
	public void createProject(int netkey, long proId, String proName, int proColor, long lastModifyTime) {
	    User user = userList.get(netkey);

		Project p = new Project(proId, proName, proColor);
		p.setLastModifyTime(lastModifyTime);

        user.addProject(p);
        ProjectOperator po = new ProjectOperator();
        po.addProject(user.getUsername(), proId, proName, proColor, lastModifyTime);
        po.close();

		printUserData(netkey);
	}

	@Override
	public void deleteProject(int netkey, long proId) {

		User user = userList.get(netkey);
		user.delProject(proId);

		ProjectOperator po = new ProjectOperator();
		TasksOperator to = new TasksOperator();
		po.delProject(user.getUsername(), proId);
		to.delTaskInProject(user.getUsername(), proId);
		to.close();
		po.close();

		printUserData(netkey);
	}

	@Override
	public void createTask(int netkey, long taskId, long proId, String content,
						   long time, int level, boolean isFinished, long lastModifyTime) {

        User user = userList.get(netkey);

		TaskItem t = new TaskItem(proId, taskId,content, time, level);
		user.addTask(proId, t);
		t.setLastModifyTime(lastModifyTime);

		TasksOperator to = new TasksOperator();
		to.addTask(user.getUsername(), taskId, proId, content, time, level, isFinished, lastModifyTime);
		to.close();

        printUserData(netkey);
	}

	@Override
	public void deleteTask(int netkey, long taskId, long proId) {

        userList.get(netkey).deleteTask(taskId, proId);

        TasksOperator to = new TasksOperator();
        to.delTask(userList.get(netkey).getUsername(), taskId);
        to.close();

        printUserData(netkey);
	}

	@Override
	public void modifyProject(int netkey, long proId, String proName, int proColor, long lastModifyTime) {

		User user = userList.get(netkey);
		user.modifyProject(proId, proName, proColor, lastModifyTime);

		ProjectOperator po = new ProjectOperator();
		po.modifyProject(user.getUsername(), proId, proName, proColor, lastModifyTime);
		po.close();

		po.close();


		printUserData(netkey);
	}

	@Override
	public void modifyTask(int netkey, long taskId, long oldProId, long newProId, String content,
						   long time, int level, boolean isFinished, long lastModifyTime) {
		User user = userList.get(netkey);

        user.modifyTask(taskId, oldProId, newProId, content, taskId, level, isFinished, lastModifyTime);

        TasksOperator to = new TasksOperator();
        to.modifyTask(user.getUsername(), taskId, newProId, content, time, level, isFinished, lastModifyTime);
        to.close();

		printUserData(netkey);
	}

}

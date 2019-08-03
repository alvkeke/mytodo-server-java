package Server;

import Server.DataHandler.*;
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
;

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

					} else {
						System.out.println(">unknown msg:\n" + (int)cmd +":"+ data);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
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
		for(int netkey : userList.keySet()){
			User u = userList.get(netkey);

			System.out.println(u.getUsername() +"["+ u.getNetkey() +"]");
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
	public void gotUserLogin(int netkey, String username) {
		User user = new User(netkey, username);
		user.setLastHeartTime(new Date().getTime());

		userList.put(netkey, user);
		printOnlineUser();
	}

	@Override
	public void gotUserLogout(int netkey){
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

		printUserData(netkey);
	}

	@Override
	public void createProject(int netkey, long proId, String proName, int proColor, long lastModifyTime) {
	    User user = userList.get(netkey);

//	    ArrayList<Project> userProjs = userData.get(netkey);
		Project p = new Project(proId, proName, proColor);
		p.setLastModifyTime(lastModifyTime);

//		userProjs.add(p);

        user.addProject(p);

		printUserData(netkey);
	}

	@Override
	public void deleteProject(int netkey, long proId) {

//	    ArrayList<Project> userProjs = userData.get(netkey);
//		Project p = Functions.findProjectInProjectList(userProjs, proId);
//		if(p != null){
//			userProjs.remove(p);
//		}

		User user = userList.get(netkey);
		user.delProject(proId);

		printUserData(netkey);
	}

	@Override
	public void createTask(int netkey, long taskId, long proId, String content, long time, int level, boolean isFinished, long lastModifyTime) {

//		ArrayList<Project> projects = userData.get(netkey);
//
//		if(projects == null) return;
//
//		Project p = Functions.findProjectInProjectList(projects, proId);
//		if(p == null){
//			return;
//		}
        User user = userList.get(netkey);

		TaskItem t = new TaskItem(proId, taskId,content, time, level);
		user.addTask(proId, t);
		t.setLastModifyTime(lastModifyTime);

        printUserData(netkey);
	}

	@Override
	public void deleteTask(int netkey, long taskId, long proId) {
//	    ArrayList<Project> projects = userData.get(netkey);
//		Project p = Functions.findProjectInProjectList(projects, proId);
//		if(p == null){
//			return;
//		}
//		TaskItem t = p.findTask(taskId);
//		p.getTaskList().remove(t);

        userList.get(netkey).deleteTask(taskId, proId);

        printUserData(netkey);
	}

	@Override
	public void modifyProject(int netkey, long proId, String proName, int proColor, long lastModifyTime) {
//	    ArrayList<Project> projects = userData.get(netkey);
//		Project p = Functions.findProjectInProjectList(projects, proId);
//		if(p!=null){
//			p.changeName(proName);
//			p.changeColor(proColor);
//			p.setLastModifyTime(lastModifyTime);
//		}

		userList.get(netkey).modifyProject(proId, proName, proColor, lastModifyTime);

		printUserData(netkey);
	}

	@Override
	public void modifyTask(int netkey, long taskId, long oldProId, long newProId, String content, long time, int level, boolean isFinished, long lastModifyTime) {
//		ArrayList<Project> projects = userData.get(netkey);
//		Project p = Functions.findProjectInProjectList(projects, oldProId);
//		if(p == null){
//			return;
//		}
//		TaskItem t = p.findTask(taskId);
//		if(t == null){
//			return;
//		}
//		t.setContent(content);
//		t.setTime(time);
//		t.setLevel(level);
//		t.setFinished(isFinished);
//		t.setLastModifyTime(lastModifyTime);
//
//		if(oldProId != newProId) {
//			Project pn = Functions.findProjectInProjectList(projects, newProId);
//			if (pn == null) {
//				return;
//			}
//			pn.addTask(t);
//			p.getTaskList().remove(t);
//		}

        userList.get(netkey).modifyTask(taskId, oldProId, newProId, content, taskId, level, isFinished, lastModifyTime);

		printUserData(netkey);
	}

}

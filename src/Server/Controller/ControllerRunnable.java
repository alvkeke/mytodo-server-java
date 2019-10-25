package Server.Controller;

import Server.DataBase.ProjectOperator;
import Server.DataBase.TasksOperator;
import Server.DataBase.UsersOperator;
import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;
import Server.UserOnline;
import Server.UserToSend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.*;

import static Server.Controller.Constant.*;

public class ControllerRunnable implements Runnable{

    private static final int CONTROLLER_PORT = 9997;

    private int currentAdminId;
    private long lastCheckTime;

    private HashMap<Integer, UserOnline> userList;

    private DatagramSocket socket;

    private boolean inLoop;

    public ControllerRunnable(HashMap<Integer, UserOnline> userList){
        this.userList = userList;

        currentAdminId = 0;
        inLoop= true;

        try {
            socket = new DatagramSocket(CONTROLLER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void stop(){
        inLoop = false;
    }

    private int generateNetkey(){
        Random random = new Random();
        return random.nextInt(90000000) + 10000000;
    }

    @Override
    public void run() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while (inLoop) {

            Arrays.fill(buf, (byte)0);

            try {
                socket.receive(packet);
                byte cmd = packet.getData()[0];
                String msg = new String(packet.getData()).substring(1).trim();
                String[] splitMsg = msg.split("\\|");
                int adminIdRecv = Integer.parseInt(splitMsg[0]);

                if (new Date().getTime() - lastCheckTime > 100000) {
                    //上一个管理员下线,以便新的管理员登录
                    currentAdminId = 0;
                }

                if (cmd == CONTROLLER_LOGIN) {
                    //CONTROLLER_LOGIN + username | password
                    if (currentAdminId != 0) {
                        //同一时间只能用一个管理员在线
                        String s = CONTROLLER_LOGIN_DENIED + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);

                        continue;
                    }
                    //判断管理员登录
                    if (splitMsg.length < 2) continue;
                    String username = splitMsg[0];
                    String password = splitMsg[1];

                    UsersOperator uo = new UsersOperator();
                    if (uo.adminLogin(username, password)) {
                        int genAdminId = generateNetkey();
                        String s = CONTROLLER_LOGIN_SUCCESS + String.valueOf(genAdminId);
                        packet.setData(s.getBytes());
                        socket.send(packet);
                        currentAdminId = genAdminId;
                        lastCheckTime = new Date().getTime();
                    }

                } else {
                    //CONTROLLER_CMD + adminId | data
                    int recvAdminId = Integer.parseInt(splitMsg[0]);
                    if (recvAdminId != currentAdminId) continue;

                    if (cmd == CONTROLLER_LOGOUT) {
                        currentAdminId = 0;
                    } else if (cmd == CONTROLLER_HEART_BEAT) {
                        lastCheckTime = new Date().getTime();
                    } else if (cmd == CONTROLLER_GET_ONLINE_USERS) {
                        String s = CONTROLLER_SEND_USER_BEGIN + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);

                        for (int key : userList.keySet()) {
                            UserOnline user = userList.get(key);
                            s = CONTROLLER_SEND_ONLINE_USER + user.getUsername() + "|" + user.getNetkey();
                            packet.setData(s.getBytes());
                            socket.send(packet);
                        }
                        s = CONTROLLER_SEND_USER_END + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);
                    } else if (cmd == CONTROLLER_GET_ALL_USERS) {

                        UsersOperator uo = new UsersOperator();
                        ArrayList<UserToSend> users = uo.getAllUsers();

                        String s = CONTROLLER_SEND_USER_BEGIN + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);

                        for (UserToSend user : users) {
                            s = CONTROLLER_SEND_DATABASE_USER + user.getUsername() + "|" + user.getPassword() + "|" + user.isAdmin();
                            packet.setData(s.getBytes());
                            socket.send(packet);
                        }
                        s = CONTROLLER_SEND_USER_END + "";
                        packet.setData(s.getBytes());
                        uo.close();
                        socket.send(packet);
                    } else if (cmd == CONTROLLER_GET_DATA) {
                        //CONTROLLER_GET_DATA + adminId | username
                        //获取某个用户的所有数据
                        if (splitMsg.length < 2) continue;
                        String username = splitMsg[1];

                        String s = CONTROLLER_SEND_DATA_BEGIN + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);

                        ProjectOperator po = new ProjectOperator();
                        TasksOperator to = new TasksOperator();

                        ArrayList<Project> projects = po.getAllProjects(username);
                        ArrayList<TaskItem> tasks = to.getAllTasks(username);

                        for (Project p : projects) {
                            s = String.format("%c%d|%s|%d|%d", CONTROLLER_SEND_DATA_PROJS,
                                    p.getId(), p.getName(), p.getColor(), p.getLastModifyTime()
                            );
                            packet.setData(s.getBytes());
                            socket.send(packet);
                        }

                        for (TaskItem t : tasks) {
                            s = String.format("%c%d|%d|%s|%d%d|%b|%d", CONTROLLER_SEND_DATA_TASKS,
                                    t.getId(), t.getProId(), t.getTaskContent(), t.getTime(), t.getLevel(),
                                    t.isFinished(), t.getLastModifyTime()
                            );
                            packet.setData(s.getBytes());
                            socket.send(packet);
                        }

                        to.close();
                        po.close();
                        s = CONTROLLER_SEND_DATA_END + "";
                        packet.setData(s.getBytes());
                        socket.send(packet);
                    } else if (cmd == CONTROLLER_ADD_USER) {
                        //CONTROLLER_ADD_USER + adminId | username | password
                        if (splitMsg.length < 3) continue;
                        String username = splitMsg[1];
                        String password = splitMsg[2];
                        UsersOperator uo = new UsersOperator();
                        uo.addUser(username, password);

                        uo.close();
                    } else if (cmd == CONTROLLER_DEL_USER) {
                        //CONTROLLER_DEL_USER + adminId | username
                        if (splitMsg.length < 2) continue;
                        String username = splitMsg[1];
                        UsersOperator uo = new UsersOperator();
                        uo.delUser(username);
                        uo.close();
                    } else if (cmd == CONTROLLER_EDIT_USER) {
                        //CONTROLLER_EDIT_USER + adminId | username | password | isAdmin
                        if (splitMsg.length < 4) continue;
                        String username, password;
                        boolean isAdmin;

                        username = splitMsg[1];
                        password = splitMsg[2];
                        isAdmin = Boolean.parseBoolean(splitMsg[3]);
                        UsersOperator uo = new UsersOperator();
                        uo.editUser(username, password, isAdmin);
                        uo.close();

                    } else if (cmd == CONTROLLER_KICK_USER_OUT) {
                        //CONTROLLER_KICK_USER_OUT + adminId | userNetkey
                        if (splitMsg.length < 2) continue;
                        int netkey = Integer.parseInt(splitMsg[1]);
                        String username = userList.get(netkey).getUsername();
                        userList.remove(netkey);
                        //todo:让Main类把无用数据列表移除
                    } else if (cmd == CONTROLLER_ADD_PROJECT) {
                        //CONTROLLER_ADD_PROJECT + adminId | username | proId | proName | proColor
                        //lastModifyTime will be set to 0
                        if (splitMsg.length < 5) continue;
                        String username = splitMsg[1];
                        long proId = Long.parseLong(splitMsg[2]);
                        String proName = splitMsg[3];
                        int proColor = Integer.parseInt(splitMsg[4]);
                        ProjectOperator po = new ProjectOperator();
                        po.addProject(username, proId, proName, proColor, 0);
                        po.close();
                    } else if (cmd == CONTROLLER_DEL_PROJECT) {
                        //CONTROLLER_DEL_PROJECT + adminId | username | proId
                        if (splitMsg.length < 3) continue;
                        String username = splitMsg[1];
                        long proId = Long.parseLong(splitMsg[2]);
                        ProjectOperator po = new ProjectOperator();
                        po.delProject(username, proId);
                        po.close();
                    } else if (cmd == CONTROLLER_EDIT_PROJECT) {
                        //CMD + adminId | username | proId | newName | newColor
                        //lastModifyTime + 1
                        if (splitMsg.length < 5) continue;
                        String username = splitMsg[1];
                        long proId = Long.parseLong(splitMsg[2]);
                        String name = splitMsg[3];
                        int color = Integer.parseInt(splitMsg[4]);
                        ProjectOperator po = new ProjectOperator();
                        Project p = po.getProject(username, proId);
                        po.modifyProject(username, proId, name, color, p.getLastModifyTime() + 1);
                        po.close();
                    } else if (cmd == CONTROLLER_ADD_TASK) {
                        //CONTROLLER_ADD_TASK + adminId | username | taskId | proId | content | time | level
                        if (splitMsg.length < 7) continue;
                        String username = splitMsg[1];
                        long taskId = Long.parseLong(splitMsg[2]);
                        long proId = Long.parseLong(splitMsg[3]);
                        String content = splitMsg[4];
                        long time = Long.parseLong(splitMsg[5]);
                        int level = Integer.parseInt(splitMsg[6]);
                        TasksOperator to = new TasksOperator();
                        to.addTask(username, taskId, proId, content, time, level, false, 0);
                        to.close();
                    } else if (cmd == CONTROLLER_DEL_TASK) {
                        //CMD + adminId | username | taskId
                        if (splitMsg.length < 3) continue;
                        String username = splitMsg[1];
                        long taskId = Long.parseLong(splitMsg[2]);
                        TasksOperator to = new TasksOperator();
                        to.delTask(username, taskId);
                        to.close();
                    } else if (cmd == CONTROLLER_EDIT_TASK) {
                        //CMD + adminId | username | taskId | newProId | newContent | newTime | newLevel | isFinished
                        //lastModifyTime not change
                        if (splitMsg.length < 8) continue;
                        String username = splitMsg[1];
                        long taskId = Long.parseLong(splitMsg[2]);
                        long newProId = Long.parseLong(splitMsg[3]);
                        String content = splitMsg[4];
                        long time = Long.parseLong(splitMsg[5]);
                        int level = Integer.parseInt(splitMsg[6]);
                        boolean isFinished = Boolean.parseBoolean(splitMsg[7]);
                        TasksOperator to = new TasksOperator();
                        TaskItem t = to.getTask(username, taskId);
                        to.modifyTask(username, taskId, newProId, content, time, level, isFinished, t.getLastModifyTime() + 1);
                        to.close();
                    }
                }

            } catch (NumberFormatException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}

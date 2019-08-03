package Server.DataHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import static Server.Constants.*;

public class DataModifier {

    private HandlerCallback callback;
    private DatagramSocket socket;
    private SocketAddress address;

    public DataModifier(HandlerCallback callback, DatagramSocket socket, SocketAddress address){
        this.callback = callback;
        this.socket = socket;
        this.address = address;
    }

    public void handle(byte cmd, String data){
        String[] msg = data.split("\\|");
        if(cmd == COMMAND_ADD_PROJECT){
            if(msg.length<5){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long proId = Long.parseLong(msg[1]);
            String proName = msg[2];
            int proColor = Integer.parseInt(msg[3]);
            long lastModifyTime = Long.parseLong(msg[4]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.createProject(netkey, proId, proName, proColor, lastModifyTime);
            } catch (IOException e) {
                e.printStackTrace();
                new DataModifier(callback, socket, address).handle(cmd, data);
            }
        }else if(cmd == COMMAND_DEL_PROJECT){
            if(msg.length<2){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long proId = Long.parseLong(msg[1]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.deleteProject(netkey, proId);
            } catch (IOException e){
                e.printStackTrace();
                new DataModifier(callback,socket,address).handle(cmd, data);
            }
        }else if(cmd == COMMAND_ADD_TASK){
            if(msg.length < 8){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long taskId = Long.parseLong(msg[1]);
            long proId = Long.parseLong(msg[2]);
            String todo = msg[3];
            long time = Long.parseLong(msg[4]);
            int level = Integer.parseInt(msg[5]);
            boolean isFinished = Boolean.parseBoolean(msg[6]);
            long lastModifyTime = Long.parseLong(msg[7]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.createTask(netkey, taskId, proId, todo, time, level, isFinished, lastModifyTime);
            } catch (IOException e) {
                e.printStackTrace();
                new DataModifier(callback,socket,address).handle(cmd, data);
            }
        }else if(cmd == COMMAND_DEL_TASK){
            if(msg.length < 3){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long taskId = Long.parseLong(msg[1]);
            long proId = Long.parseLong(msg[2]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.deleteTask(netkey, taskId, proId);
            } catch (IOException e) {
                e.printStackTrace();
                new DataModifier(callback,socket,address).handle(cmd, data);
            }
        }else if (cmd == COMMAND_EDIT_PROJECT){
            if(msg.length < 5){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long proId = Long.parseLong(msg[1]);
            String proName = msg[2];
            int proColor = Integer.parseInt(msg[3]);
            long lastModifyTime = Long.parseLong(msg[4]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.modifyProject(netkey, proId, proName, proColor, lastModifyTime);
            } catch (IOException e) {
                e.printStackTrace();
                new DataModifier(callback,socket,address).handle(cmd, data);
            }
        }else if(cmd == COMMAND_EDIT_TASK){
            if(msg.length < 9){
                return;
            }
            String s = COMMAND_OPERATE_SUCCESS + msg[0];
            int netkey = Integer.parseInt(msg[0]);
            long taskId = Long.parseLong(msg[1]);
            long oldProId = Long.parseLong(msg[2]);
            long newProId = Long.parseLong(msg[3]);
            String todo = msg[4];
            long time = Long.parseLong(msg[5]);
            int level = Integer.parseInt(msg[6]);
            boolean isFinished = Boolean.parseBoolean(msg[7]);
            long lastModifyTime = Long.parseLong(msg[8]);

            DatagramPacket p = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(p);
                callback.modifyTask(netkey, taskId, oldProId, newProId, todo, time, level, isFinished, lastModifyTime);
            } catch (IOException e) {
                e.printStackTrace();
                new DataModifier(callback,socket,address).handle(cmd, data);
            }

        }
    }

}

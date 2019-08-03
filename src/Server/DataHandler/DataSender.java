package Server.DataHandler;

import Server.DataStruct.Project;
import Server.DataStruct.TaskItem;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;

import static Server.Constants.*;

public class DataSender {
    private HandlerCallback callback;
    private DatagramSocket socket;
    private SocketAddress address;


    public DataSender(HandlerCallback callback, DatagramSocket socket, SocketAddress address){

        this.callback = callback;
        this.socket = socket;
        this.address = address;

    }

    public void send(int netkey, ArrayList<Project> projects, ArrayList<Integer> confirmList){
        String s = COMMAND_SEND_DATA_BEGIN + String.valueOf(netkey);
        DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
        try {
            socket. send(packet);
            int dataId = 0;
            for(Project p : projects){
                s = COMMAND_SEND_DATA_PROJS + String.valueOf(netkey) +"|"+ dataId +"|"+ p.getId() +"|"+ p.getName() +"|"+
                        p.getColor() +"|"+ p.getLastModifyTime();
                packet.setData(s.getBytes());
                socket.send(packet);
                confirmList.add(dataId++);

                for(TaskItem e: p.getTaskList()){
                    s = COMMAND_SEND_DATA_TASKS + String.valueOf(netkey) +"|"+ dataId +"|"+ e.getId() +"|"+ e.getProId() +"|"+
                            e.getTaskContent() +"|"+ e.getTime() +"|"+ e.getLevel() +"|"+ e.isFinished() +"|"+ e.getLastModifyTime();
                    packet.setData(s.getBytes());
                    socket.send(packet);
                    confirmList.add(dataId++);
                }

            }

            s = COMMAND_SEND_DATA_END + String.valueOf(netkey);
            packet.setData(s.getBytes());
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

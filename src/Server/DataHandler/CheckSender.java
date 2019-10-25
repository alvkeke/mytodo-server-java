package Server.DataHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

public class CheckSender {

    private DatagramSocket socket;
    private SocketAddress address;

    public CheckSender(DatagramSocket socket, SocketAddress address){
        this.socket = socket;
        this.address = address;
    }

    public void send(char cmd, int netkey){
        String s = cmd + String.valueOf(netkey);
        DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

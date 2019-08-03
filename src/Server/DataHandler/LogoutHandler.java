package Server.DataHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import static Server.Constants.COMMAND_LOGOUT_SUCCESS;

public class LogoutHandler {

    private HandlerCallback callback;
    private DatagramSocket socket;
    private SocketAddress address;

    public LogoutHandler(HandlerCallback callback, DatagramSocket socket, SocketAddress address){
        this.callback = callback;
        this.socket = socket;
        this.address = address;
    }

    public void handle(int netkey){
        String s = COMMAND_LOGOUT_SUCCESS + "";
        DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);

        try {
            socket.send(packet);
            callback.gotUserLogout(netkey);
        } catch (IOException e) {
            e.printStackTrace();
            new LogoutHandler(callback, socket, address).handle(netkey);
        }
    }
}

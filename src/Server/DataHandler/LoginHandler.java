package Server.DataHandler;

import Server.DataBase.UsersOperator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Random;

import static Server.Constants.*;
public class LoginHandler {

    private HandlerCallback callback;
    private DatagramSocket socket;
    private SocketAddress address;

    public LoginHandler(HandlerCallback callback, DatagramSocket socket, SocketAddress address){
        this.callback = callback;
        this.socket = socket;
        this.address = address;
    }

    public void handle(String username, String password){
        UsersOperator uo = new UsersOperator();
        int userId = uo.findUser(username, password);
        uo.close();
        if(userId > 0){
            int netkey = generateNetkey();
            String s = COMMAND_LOGIN_SUCCESS + String.valueOf(netkey);
            DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(packet);
                callback.gotUserLogin(netkey, userId, username);
            } catch (IOException e){
                e.printStackTrace();
                new LoginHandler(callback, socket, address).handle(username, password);
            }
        }else{
            String s = COMMAND_LOGIN_FAILED +"";
            DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
            try {
                socket.send(packet);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private int generateNetkey(){
        Random random = new Random();
        return random.nextInt(90000000) + 10000000;
    }
}

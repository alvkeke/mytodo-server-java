package Server.DataHandler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import static Server.Constants.*;

public class DataRecvHandler {

    private HandlerCallback callback;
    private DatagramSocket socket;
    private SocketAddress address;

    public DataRecvHandler(HandlerCallback callback, DatagramSocket socket, SocketAddress address){
        this.address = address;
        this.callback = callback;
        this.socket = socket;

    }

    public void handle(byte cmd, String[] msgs){
        String s;
        if(cmd == COMMAND_SEND_DATA_BEGIN){
            s = COMMAND_CONFIRM_SEND_BEGIN + msgs[0];   //netkey
        }else if(cmd == COMMAND_SEND_DATA_END){
            s = COMMAND_CONFIRM_SEND_END + msgs[0]; //netkey
        }else{
            if(msgs.length<2) return;
            s = COMMAND_CONFIRM_DATA + msgs[1];     //dataId
        }

        int netkey;
        try {
            netkey = Integer.parseInt(msgs[0]);
        } catch (NumberFormatException e){
            return;
        }

        DatagramPacket packet = new DatagramPacket(s.getBytes(), s.getBytes().length, address);
        try {

//            socket.setSoTimeout(6000);
            socket.send(packet);

            switch (cmd) {
                case COMMAND_SEND_DATA_BEGIN:   //2
                    callback.beginRecvData(netkey);
                    break;
                case COMMAND_SEND_DATA_END: //2
                    callback.endRecvData(netkey);
                    break;
                case COMMAND_SEND_DATA_PROJS:   //6
                    if (msgs.length < 6) return;
                    callback.recvProject(netkey, Long.parseLong(msgs[2]), msgs[3], Integer.parseInt(msgs[4]), Long.parseLong(msgs[5]));
                    break;
                case COMMAND_SEND_DATA_TASKS:   //9
                    if (msgs.length < 9) return;
                    callback.recvTask(netkey, Long.parseLong(msgs[2]), Long.parseLong(msgs[3]), msgs[4],
                            Long.parseLong(msgs[5]), Integer.parseInt(msgs[6]), Boolean.parseBoolean(msgs[7]),
                            Long.parseLong(msgs[8]));
                    break;
            }
        } catch (NumberFormatException e){
            e.printStackTrace();
            return;
        } catch (IOException e){
            e.printStackTrace();
            new DataRecvHandler(callback, socket, address).handle(cmd, msgs);
        }

    }

}

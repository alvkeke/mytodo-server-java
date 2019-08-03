package Server;

public class Constants {
//    public final static String SERVER_IP = "192.168.1.111";
    public final static String SERVER_IP = "192.168.1.9";
    public final static int SERVER_PORT = 9999;


    public final static char COMMAND_LOGIN = 0;
    //COMMAND_LOGIN + username + ":" + password
    public final static char COMMAND_LOGIN_SUCCESS = 1;
    //COMMAND_LOGIN_SUCCESS + netkey
    public final static char COMMAND_LOGIN_FAILED = 2;
    //COMMAND_LOGIN_FAILED

    public final static char COMMAND_LOGOUT = 3;
    //COMMAND_LOGOUT + netkey
    public final static char COMMAND_LOGOUT_SUCCESS = 4;
    //COMMAND_LOGOUT_SUCCESS + netkey

    /**Sender send**/
    public final static char COMMAND_SEND_DATA_BEGIN = 5;
    //COMMAND_SEND_DATA_BEGIN + netkey
    public final static char COMMAND_SEND_DATA_PROJS = 6;
    public final static char COMMAND_SEND_DATA_TASKS = 7;
    //COMMAND_SEND_DATA_PROJS/TASKS + netkey + data(split with :)
    public final static char COMMAND_SEND_DATA_END = 8;

    /**receiver return**/
    public final static char COMMAND_CONFIRM_SEND_BEGIN = 9;
    public final static char COMMAND_CONFIRM_DATA = 10;
    //COMMAND_CONFIRM_DATA + id;
    public final static char COMMAND_CONFIRM_SEND_END = 11;
    //COMMAND_SEND_DATA_END + netkey

    /**resend the data which is not be confirmed**/
//    public final static char COMMAND_RESEND_DATA_BEGIN = 12;
//    public final static char COMMAND_RESEND_DATA_END = 13;
    public final static char COMMAND_RESEND_NEED = 12;
    public final static char COMMAND_RESEND_NO_NEED = 13;

    /**receiver send**/
    public final static char COMMAND_GET_DATA = 14;
    //COMMAND_GET_DATA + netkey
    /**
     * Sender will send the COMMAND_SEND_DATA_BEGIN to the receiver
     * receiver will send the confirm command to the sender
     * the first command must be COMMAND_SEND_DATA_BEGIN
     */

    public final static char COMMAND_ADD_PROJECT = 15;
    public final static char COMMAND_DEL_PROJECT = 16;

    public final static char COMMAND_ADD_TASK = 17;
    public final static char COMMAND_DEL_TASK = 18;

    public final static char COMMAND_EDIT_PROJECT = 19;
    public final static char COMMAND_EDIT_TASK = 20;

    public final static char COMMAND_OPERATE_SUCCESS = 21;
    public final static char COMMAND_OPERATE_FAILED = 22;

    public final static char COMMAND_HEART_BEAT = 127;
    //COMMAND_HEART_BEAT + netkey

}

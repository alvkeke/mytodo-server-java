package Server.Controller;

public class Constant {


    static final char CONTROLLER_LOGIN = 0;
    static final char CONTROLLER_LOGIN_SUCCESS = 1;
    static final char CONTROLLER_LOGIN_DENIED = 2;

    static final char CONTROLLER_LOGOUT = 3;

    static final char CONTROLLER_GET_ONLINE_USERS = 4;
    static final char CONTROLLER_GET_ALL_USERS = 5;

    static final char CONTROLLER_SEND_USER_BEGIN = 6;
    static final char CONTROLLER_SEND_DATABASE_USER = 7;
    static final char CONTROLLER_SEND_ONLINE_USER = 8;
    static final char CONTROLLER_SEND_USER_END = 9;

    static final char CONTROLLER_GET_DATA = 10;

    static final char CONTROLLER_SEND_DATA_BEGIN = 11;
    static final char CONTROLLER_SEND_DATA_TASKS = 12;
    static final char CONTROLLER_SEND_DATA_PROJS = 13;
    static final char CONTROLLER_SEND_DATA_END = 14;

    static final char CONTROLLER_ADD_USER = 15;
    static final char CONTROLLER_DEL_USER = 16;
    static final char CONTROLLER_EDIT_USER = 17;
    static final char CONTROLLER_KICK_USER_OUT = 18;

    static final char CONTROLLER_ADD_PROJECT = 19;
    static final char CONTROLLER_DEL_PROJECT = 20;
    static final char CONTROLLER_EDIT_PROJECT = 21;

    static final char CONTROLLER_ADD_TASK = 22;
    static final char CONTROLLER_DEL_TASK = 23;
    static final char CONTROLLER_EDIT_TASK = 24;

    static final char CONTROLLER_HEART_BEAT = 127;

}

package Server.DataHandler;

public interface HandlerCallback {

    void gotUserLogin(int netkey, String username);
    void gotUserLogout(int netkey);

    void beginRecvData(int netkey);
    void recvProject(int netkey, long proId, String proName, int proColor, long lastModifyTime);
    void recvTask(int netkey, long taskId, long proId, String taskContent, long time, int level, boolean isFinished, long lastModifyTime);
    void endRecvData(int netkey);

    void createProject(int netkey, long proId, String proName, int proColor, long lastModifyTime);
    void deleteProject(int netkey, long proId);

    void createTask(int netkey, long taskId, long proId, String content, long time, int level, boolean isFinished, long lastModifyTime);
    void deleteTask(int netkey, long taskId, long proId);

    void modifyProject(int netkey, long proId, String proName, int proColor, long lastModifyTime);
    void modifyTask(int netkey, long taskId, long oldProId, long newProId, String content, long time, int level, boolean isFinished, long lastModifyTime);

}

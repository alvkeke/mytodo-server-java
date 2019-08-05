package Server;

import javax.swing.*;

class UI extends JFrame {

    JList<String> userList;


    UI(){
        this.setSize(500,500);
        this.setLocation(100,100);

        userList = new JList<>();

        userList.setSize(100,500);
        userList.setLocation(0,0);
        userList.setListData(new String[]{"123", "456", "789"});


        userList.setVisible(true);
        add(userList);
    }

}

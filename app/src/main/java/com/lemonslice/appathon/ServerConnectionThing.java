package com.lemonslice.appathon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by alexander on 3/8/15.
 */
public class ServerConnectionThing {
    ServerSocket serverSocket;
    Socket connectionSocket;
    public ServerConnectionThing() {
        try {
            serverSocket = new ServerSocket(9000);
            connectionSocket = new Socket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEmoji(InetAddress address) {

    }

    public void receiveEmoji() {

    }

    interface EmojiReceiver {
        void receive();
    }

}

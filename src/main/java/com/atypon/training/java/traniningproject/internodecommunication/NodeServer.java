package com.atypon.training.java.traniningproject.internodecommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class NodeServer extends Thread {

    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());
    private static volatile NodeServer INSTANCE = null;
    private int nodeServerPort = Node.getSharedInstance().getPort();
    private ServerSocket server = null;

    public static NodeServer getSharedInstance() {
        if (INSTANCE == null) { // Check 1
            synchronized (NodeServer.class) {
                if (INSTANCE == null) { // Check 2
                    INSTANCE = new NodeServer();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(nodeServerPort);
            while (true) {
                Socket skt = server.accept();
                BufferedReader inputFromPeer = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                Thread serverThread = new Thread(new NodeServerThread(skt, inputFromPeer));
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

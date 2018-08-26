package com.atypon.training.java.traniningproject.internodecommunication;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeServer {

    private int nodeServerPort;
    private ServerSocket server = null;


    public NodeServer(int nodeServerPort) {
        this.nodeServerPort = nodeServerPort;
    }

    public void start() {
        try {
            server = new ServerSocket(nodeServerPort);
            while (true) {
                Socket skt = new ServerSocket().accept();
                Thread serverThread = new Thread(new NodeServerThread(skt));
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.atypon.training.java.atycoin.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class NodeServer extends Thread {

    private static final NodeServer INSTANCE = new NodeServer();
    private int nodeServerPort = Node.getSharedInstance().getPort();
    private ServerSocket server = null;

    public static NodeServer getSharedInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(nodeServerPort);
            while (true) {
                Socket skt = server.accept();
                Thread serverThread = new Thread(new NodeServerThread(skt));
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.atypon.training.java.traniningproject.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public final class NodeServer extends Thread {

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

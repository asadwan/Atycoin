package com.atypon.training.java.traniningproject.internodecommunication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Logger;

public class NodeServer extends Thread {

    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());

    private int nodeServerPort;
    private ServerSocket server = null;
    private Set<Integer> peersAddresses;


    public NodeServer(int nodeServerPort, Set<Integer> peersAddresses) {
        this.nodeServerPort = nodeServerPort;
        this.peersAddresses = peersAddresses;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(nodeServerPort);
            while (true) {
                Socket skt = server.accept();
                BufferedReader inputFromPeer = new BufferedReader(new InputStreamReader(skt.getInputStream()));
                String nodeAddress = inputFromPeer.readLine();
                LOGGER.info("A connection with peer '" + nodeAddress + "' has been established");
                Thread serverThread = new Thread(new NodeServerThread(skt, inputFromPeer, nodeAddress, peersAddresses));
                serverThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

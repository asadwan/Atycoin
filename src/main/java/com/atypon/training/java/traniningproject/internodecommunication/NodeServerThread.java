package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Block;
import com.atypon.training.java.traniningproject.Blockchain;
import com.atypon.training.java.traniningproject.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class NodeServerThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());
    private Set<Integer> peersAddresses;
    private Socket skt;
    private BufferedReader inputStream;
    private String nodeAddress; // The address of the peer this thread is handling communication with
    private Blockchain blockchain;

    public NodeServerThread(Socket skt, BufferedReader inputStream, String nodeAddress,
                            Set<Integer> peersAddresses) {
        this.skt = skt;
        this.inputStream = inputStream;
        this.nodeAddress = nodeAddress;
        this.peersAddresses = peersAddresses;
        //this.blockchain = blockchain;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Handling communications with peer " + nodeAddress);
            Gson gson = new Gson();
            String message;
            while ((message = inputStream.readLine()) != null) {
                Map messageMap = gson.fromJson(message, Map.class);
                if (messageMap.keySet().contains("node")) {
                    Type stringNodeMapType = new TypeToken<Map<String, Node>>() {
                    }.getType();
                    Map<String, Node> nodeMap = gson.fromJson(message, stringNodeMapType);
                    Node node = nodeMap.get("node");
                    new Thread(() -> {
                        System.out.println(node.getPort());
                        System.out.println("Received new node info");
                    }).start();

                } else if (messageMap.keySet().contains("transaction")) {
                    Type stringTransactionMapType = new TypeToken<Map<String, Transaction>>() {
                    }.getType();
                    Map<String, Transaction> transactionMap = gson.fromJson(message, stringTransactionMapType);
                    Transaction transaction = transactionMap.get("transaction");
                    new Thread(() -> System.out.println("Received transaction")).start();

                } else if (messageMap.keySet().contains("block")) {
                    Type stringBlockMapType = new TypeToken<Map<String, Block>>() {
                    }.getType();
                    Map<String, Block> blockMapMap = gson.fromJson(message, stringBlockMapType);
                    Block block = blockMapMap.get("block");
                    new Thread(() -> {
                        System.out.println("Received new block from peer " + nodeAddress);
                    });

                } else if (messageMap.keySet().contains("peers")) {
                    Type stringSetTypeType = new TypeToken<Map<String, Set<Node>>>() {
                    }.getType();
                    Map<String, Set<Node>> peersMap = gson.fromJson(message, stringSetTypeType);
                    Set<Node> peers = peersMap.get("peers");
                    new Thread(() -> {

                    });
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Bye from serverThread handling " + skt.getPort());
        }

    }
}

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

    private static final Logger LOGGER = Logger.getLogger(NodeServerThread.class.getName());

    private Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
    private Socket skt;
    private BufferedReader inputStream;
    private Integer nodeAddress; // The address of the peer this thread is handling communication with
    private Blockchain blockchain = Blockchain.getSharedInstance();
    private NodeClient nodeClient = NodeClient.getSharedInstance();

    public NodeServerThread(Socket skt, BufferedReader inputStream) {
        this.skt = skt;
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            Gson gson = new Gson();
            String message;
            while ((message = inputStream.readLine()) != null) {
                Map messageMap = gson.fromJson(message, Map.class);
                if (messageMap.keySet().contains("peer")) {
                    Type stringNodeMapType = new TypeToken<Map<String, Integer>>() {
                    }.getType();
                    Map<String, Integer> nodeMap = gson.fromJson(message, stringNodeMapType);
                    Integer newPeerAddress = nodeMap.get("peer");
                    nodeAddress = newPeerAddress;
                    LOGGER.info("An incoming connection from peer '" + newPeerAddress + "' has been established");
                    if (!peersAddresses.contains(newPeerAddress)) {
                        peersAddresses.add(newPeerAddress);
                        nodeClient.connectToPeer(newPeerAddress);
                        nodeClient.sendMyAddressToPeer(newPeerAddress); //Connect back to the peer
                        //nodeClient.broadcastNewPeerAddress(newPeerAddress);
                        nodeClient.sendConnectedPeersAddressesToPeer(newPeerAddress);
                    }
                } else if (messageMap.keySet().contains("transaction")) {
                    Type stringTransactionMapType = new TypeToken<Map<String, Transaction>>() {
                    }.getType();
                    Map<String, Transaction> transactionMap = gson.fromJson(message, stringTransactionMapType);
                    Transaction transaction = transactionMap.get("transaction");
                    LOGGER.info("A new transaction has been received from  " + nodeAddress);

                } else if (messageMap.keySet().contains("block")) {
                    Type stringBlockMapType = new TypeToken<Map<String, Block>>() {
                    }.getType();
                    Map<String, Block> blockMapMap = gson.fromJson(message, stringBlockMapType);
                    Block block = blockMapMap.get("block");
                    new Thread(() -> {
                        Blockchain blockchain = Blockchain.getSharedInstance();
                        blockchain.addBlock(block);
                        LOGGER.info("A new block has been received from  " + nodeAddress);
                    }).start();

                } else if (messageMap.keySet().contains("peers")) {
                    LOGGER.info("Received peer " + nodeAddress + " connected peers list");
                    Type stringSetTypeType = new TypeToken<Map<String, Set<Integer>>>() {
                    }.getType();
                    Map<String, Set<Integer>> peersMap = gson.fromJson(message, stringSetTypeType);
                    Set<Integer> newPeersAddresses = peersMap.get("peers");
                    newPeersAddresses.removeAll(peersAddresses);
                    for (Integer newPeerAddress : newPeersAddresses) {
                        peersAddresses.add(newPeerAddress);
                        nodeClient.connectToPeer(newPeerAddress);
                        nodeClient.sendMyAddressToPeer(newPeerAddress);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LOGGER.info("Bye from ServerThread handling communication with peer " + nodeAddress);
        }

    }
}

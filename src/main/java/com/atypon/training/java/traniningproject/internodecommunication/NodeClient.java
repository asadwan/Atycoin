package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Block;
import com.atypon.training.java.traniningproject.Transaction;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NodeClient {

    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());


    public final int NODE_ADDRESS;
    private final String INITIAL_NODES_FILE = "/Users/asadwan/IntellijIDEAProjects/TrainingProject/src/main/resources/initial-nodes.txt";
    public ArrayList<Socket> nodesSkts = new ArrayList<>();
    public Set<Integer> peersAddresses;

    public Map<String, PrintWriter> outputStreams = new HashMap<>();

    public Map<Integer, Socket> peersConnections = new HashMap<>();

    public NodeClient(int NODE_ADDRESS, Set<Integer> peersAddresses) {
        this.NODE_ADDRESS = NODE_ADDRESS;
        this.peersAddresses = peersAddresses;
    }

    public void start() {
        loadSavedNodesAddresses();
        connectToPeers();
    }

    public void connectToPeers() {
        LOGGER.info(String.valueOf("Connecting to peers..."));
        int nodePort;
        final String nodeHost = "localhost";
        for (Integer peerAddress : peersAddresses) {
            if (peerAddress.equals(NODE_ADDRESS)) continue; // If this address is same as current node address skip
            nodePort = peerAddress;
            try {
                Socket skt = new Socket(nodeHost, nodePort);
                PrintWriter pw = new PrintWriter(skt.getOutputStream());
                nodesSkts.add(skt);
                peersConnections.put(nodePort, skt);
                outputStreams.put(String.valueOf(nodePort), pw);
                pw.println(NODE_ADDRESS); // Replace this with this peer address (port num)
                LOGGER.info("A connection was established with peer " + nodePort);
            } catch (UnknownHostException e) {
                LOGGER.log(Level.WARNING, "An IOException has occurred", e);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "An IOException has occurred", e);
            }
        }
    }

    public void loadSavedNodesAddresses() {
        try (BufferedReader br = new BufferedReader(new FileReader(INITIAL_NODES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                int port = Integer.parseInt(line);
                peersAddresses.add(port);
            }
            LOGGER.info("Loaded peers addresses from file 'initial-nodes.txt'");
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Initial nodes addresses file was not found", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An IOException has occurred", e);
        }
    }

//    public void broadcastMyAddress(Node node) {
//        Map<String, Node> myNodeMessage = new HashMap<>();
//        myNodeMessage.put("node", node);
//        Gson gson = new Gson();
//        String messageJson = gson.toJson(myNodeMessage);
//        for (Map.Entry<String, PrintWriter> entry: outputStreams.entrySet()) {
//            PrintWriter outToPeer = entry.getValue();
//            String peerAddress = entry.getKey();
//            outToPeer.println(messageJson);
//            outToPeer.flush();
//            LOGGER.info("This peer address has been shared with peer " + peerAddress);
//        }
//    }

    public void broadcastNewTransaction(Transaction transaction) {
        Map<String, Transaction> newTransactionMessage = new HashMap<>();
        newTransactionMessage.put("transaction", transaction);
        Gson gson = new Gson();
        String messageJson;
        messageJson = gson.toJson(newTransactionMessage);
        for (Map.Entry<String, PrintWriter> entry : outputStreams.entrySet()) {
            PrintWriter outToPeer = entry.getValue();
            String peerAddress = entry.getKey();
            outToPeer.println(messageJson);
            outToPeer.flush();
            LOGGER.info("A new transaction has been shared with peer" + peerAddress);
        }
    }

    public void broadcastNewBlock(Block block) {
        Map<String, Block> newBlockMessage = new HashMap<>();
        newBlockMessage.put("block", block);
        Gson gson = new Gson();
        String messageJson = gson.toJson(newBlockMessage);
        for (Map.Entry<String, PrintWriter> entry : outputStreams.entrySet()) {
            PrintWriter outToPeer = entry.getValue();
            String peerAddress = entry.getKey();
            outToPeer.println(messageJson);
            outToPeer.flush();
            LOGGER.info("A new block has been shared with peer " + peerAddress);
        }
    }

    public void broadcastPeersList() {
        Map<String, Set<Integer>> peersListMessage = new HashMap<>();
        peersListMessage.put("peers", peersAddresses);
        Gson gson = new Gson();
        String messageJson = gson.toJson(peersListMessage);
        for (Map.Entry<String, PrintWriter> entry : outputStreams.entrySet()) {
            PrintWriter outToPeer = entry.getValue();
            String peerAddress = entry.getKey();
            outToPeer.println(messageJson);
            outToPeer.flush();
            LOGGER.info("This peer's stored peers addresses list has been sent to " + peerAddress);
        }
    }

}

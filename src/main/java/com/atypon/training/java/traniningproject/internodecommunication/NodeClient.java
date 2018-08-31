package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Block;
import com.atypon.training.java.traniningproject.Transaction;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class NodeClient {

    private static final Integer PEER_ADDRESS = Node.getSharedInstance().getPort();

    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());
    private static volatile NodeClient INSTANCE = new NodeClient();
    private final String INITIAL_NODES_FILE = "/Users/asadwan/IntellijIDEAProjects/TrainingProject/" +
            "src/main/resources/initial-nodes.txt";
    public Map<Integer, PrintWriter> outputStreams = new HashMap<>();

    public List<Connection> peersConnections = new ArrayList<>();

    public static NodeClient getSharedInstance() {
        if (INSTANCE == null) { // Check 1
            synchronized (NodeServer.class) {
                if (INSTANCE == null) { // Check 2
                    INSTANCE = new NodeClient();
                }
            }
        }
        return INSTANCE;
    }

    public void start() {
        loadSavedNodesAddresses();
        connectToPeers();
        broadcastMyAddress();
    }

    public void loadSavedNodesAddresses() {
        try (BufferedReader br = new BufferedReader(new FileReader(INITIAL_NODES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                Integer port = Integer.parseInt(line.trim());
                if (port.equals(Node.getSharedInstance().getPort())) break;
                Node.getSharedInstance().getPeersAddresses().add(port);
            }
            LOGGER.info("Loaded peers addresses from file 'initial-nodes.txt'");
        } catch (FileNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Initial nodes addresses file was not found", e);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An IOException has occurred", e);
        }
    }

    public void connectToPeers() {
        LOGGER.info("Connecting to peers...");
        int nodePort;
        final String nodeHost = "localhost";
        Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
        for (Integer peerAddress : peersAddresses) {
            if (peerAddress.equals(Node.getSharedInstance().getPort()))
                continue; // If this address is same as current node address skip
            connectToPeer(peerAddress);
        }
    }

    public void connectToPeer(Integer peerPort) {
        if (peersConnections.stream().anyMatch(connection -> connection.equals(peerPort))) return;
        if (peerPort.equals(Node.getSharedInstance().getPort())) return;
        try {
            Socket socket = new Socket("localhost", peerPort);
            PrintWriter pw = new PrintWriter(socket.getOutputStream());
            Connection connection = new Connection(socket, peerPort, pw);
            peersConnections.add(connection);
            LOGGER.info("An outgoing connection to peer '" + peerPort + "' has been established");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An IOException has occurred", e);
        }
    }

    public void broadcastMyAddress() {
        for (Connection connection : peersConnections) {
            sendMyAddressToPeer(connection.getPeerAddress());
        }
    }

    public void sendMyAddressToPeer(Integer peerAddress) {
        Integer myAddress = Node.getSharedInstance().getPort();
        Map<String, Integer> myNodeMessage = new HashMap<>();
        myNodeMessage.put("peer", myAddress);
        Gson gson = new Gson();
        String messageJson = gson.toJson(myNodeMessage);
        if (peerAddress.equals(Node.getSharedInstance().getPort())) return;
        PrintWriter outToPeer = peersConnections.stream().filter(connection ->
                connection.getPeerAddress().equals(peerAddress)).findFirst().get().getOutToPeer();
        outToPeer.println(messageJson);
        outToPeer.flush();
        LOGGER.info("This peer address has been sent to peer " + peerAddress);
    }

    public void broadcastNewTransaction(Transaction transaction) {
        Map<String, Transaction> newTransactionMessage = new HashMap<>();
        newTransactionMessage.put("transaction", transaction);
        Gson gson = new Gson();
        String messageJson;
        messageJson = gson.toJson(newTransactionMessage);
        for (Map.Entry<Integer, PrintWriter> entry : outputStreams.entrySet()) {
            PrintWriter outToPeer = entry.getValue();
            Integer peerAddress = entry.getKey();
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
        for (Connection connection : peersConnections) {
            PrintWriter outToPeer = connection.getOutToPeer();
            Integer peerAddress = connection.getPeerAddress();
            outToPeer.println(messageJson);
            outToPeer.flush();
            LOGGER.info("A new block has been shared with peer " + peerAddress);
        }
    }

    public void broadcastConncectedPeersAddresses() {
        for (Connection connection : peersConnections) {
            sendConnectedPeersAddressesToPeer(connection.getPeerAddress());
        }
    }

    public void sendConnectedPeersAddressesToPeer(Integer peerAddress) {
        Map<String, Set<Integer>> peersListMessage = new HashMap<>();
        Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
        peersListMessage.put("peers", peersAddresses);
        Gson gson = new Gson();
        String messageJson = gson.toJson(peersListMessage);
        if (peerAddress.equals(PEER_ADDRESS)) return;
        PrintWriter outToPeer = peersConnections.stream().filter(connection ->
                connection.getPeerAddress().equals(peerAddress)).findFirst().get().getOutToPeer();
        outToPeer.println(messageJson);
        outToPeer.flush();
        LOGGER.info("This peer's stored peers addresses list has been sent to " + peerAddress);
    }

    public void broadcastNewPeerAddress(Integer newPeerAddress) {
        Integer toPeerAddress;
        for (Connection connection : peersConnections) {
            toPeerAddress = connection.getPeerAddress();
            if (toPeerAddress.equals(PEER_ADDRESS)) continue;
            if (toPeerAddress.equals(newPeerAddress)) continue;
            Map<String, Integer> newPeerMessage = new HashMap<>();
            newPeerMessage.put("peer", newPeerAddress);
            Gson gson = new Gson();
            String messageJson = gson.toJson(newPeerAddress);
            PrintWriter outToPeer = peersConnections.stream().filter(conn -> conn.getPeerAddress().
                    equals(conn.getPeerAddress())).findFirst().get().getOutToPeer();
            outToPeer.println(messageJson);
            outToPeer.flush();
            LOGGER.info("Peer address '" + newPeerAddress + "' has been sent to peer " + toPeerAddress);
        }
    }

    //private Boolean checkIfPeersSe
}

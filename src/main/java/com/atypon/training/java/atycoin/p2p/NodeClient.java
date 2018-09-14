package com.atypon.training.java.atycoin.p2p;

import com.atypon.training.java.atycoin.blockchain_core.Block;
import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.atypon.training.java.atycoin.transactions_system.Transaction;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class NodeClient {

    private static final NodeClient INSTANCE = new NodeClient();
    private static final Integer PEER_ADDRESS = Node.getSharedInstance().getPort();
    private static final Logger LOGGER = Logger.getLogger(NodeClient.class.getName());
    private static Gson gson = new Gson();

    public List<Connection> peersConnections = new ArrayList<>();

    public static NodeClient getSharedInstance() {
        return INSTANCE;
    }

    public void start() {
        loadSavedPeerAddresses();
        connectToPeers();
        broadcastMyAddress();
    }

    private void loadSavedPeerAddresses() {
        String INITIAL_NODES_FILE = "src/main/resources/initial-nodes.txt";
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

    private void connectToPeers() {
        LOGGER.info("Connecting to peers...");
        Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
        for (Integer peerAddress : peersAddresses) {
            if (peerAddress.equals(Node.getSharedInstance().getPort()))
                continue; // If this address is same as current node address skip
            connectToPeer(peerAddress);
        }
    }

    void connectToPeer(Integer peerAddress) {
        if (peersConnections.stream().anyMatch(connection -> connection.equals(peerAddress))) return;
        if (peerAddress.equals(Node.getSharedInstance().getPort())) return;
        try {
            Socket socket = new Socket("localhost", peerAddress);
            Connection connection = new Connection(socket, peerAddress);
            peersConnections.add(connection);
            LOGGER.info("An outgoing connection to peer '" + peerAddress + "' has been established");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "An IOException has occurred", e);
        }
    }

    private void broadcastMyAddress() {
        for (Connection connection : peersConnections) {
            sendMyAddressToPeer(connection.getPeerAddress());
        }
    }

    void sendMyAddressToPeer(Integer peerAddress) {
        Integer myAddress = Node.getSharedInstance().getPort();
        Map<String, Integer> myNodeMessage = new HashMap<>();
        myNodeMessage.put("peer", myAddress);
        Gson gson = new Gson();
        String messageJson = gson.toJson(myNodeMessage);
        if (peerAddress.equals(PEER_ADDRESS)) return;
        PrintWriter outToPeer = peersConnections.stream().filter(connection ->
                connection.getPeerAddress().equals(peerAddress)).findFirst().get().getOutToPeerStream();
        outToPeer.println(messageJson);
        outToPeer.flush();
        LOGGER.info("This peer address has been sent to peer " + peerAddress);
    }

    public void broadcastNewTransaction(Transaction transaction) {
        Map<String, Transaction> newTransactionMessage = new HashMap<>();
        newTransactionMessage.put("transaction", transaction);
        String transactionJson = gson.toJson(newTransactionMessage);
        for (Connection peerConnection : peersConnections) {
            sendNewTransactionToPeer(transactionJson, peerConnection);
        }
    }

    private void sendNewTransactionToPeer(String transactionJson, Connection peerConnection) {
        PrintWriter outToPeer = peerConnection.getOutToPeerStream();
        Integer peerAddress = peerConnection.getPeerAddress();
        outToPeer.println(transactionJson);
        outToPeer.flush();
        LOGGER.info("A new transaction has been shared with peer" + peerAddress);
    }

    public void broadcastNewBlock(Block block) {
        Map<String, Block> newBlockMessage = new HashMap<>();
        newBlockMessage.put("block", block);
        String newBlockJson = gson.toJson(newBlockMessage);
        for (Connection connection : peersConnections) {
            sendNewBlockToPeer(newBlockJson, connection);
        }
    }

    private void sendNewBlockToPeer(String blockJson, Connection peerConnection) {
        PrintWriter outToPeer = peerConnection.getOutToPeerStream();
        Integer peerAddress = peerConnection.getPeerAddress();
        outToPeer.println(blockJson);
        outToPeer.flush();
        LOGGER.info("A new block has been shared with peer " + peerAddress);
    }

    void sendConnectedPeersAddressesToPeer(Integer peerAddress) {
        Map<String, Set<Integer>> peersListMessage = new HashMap<>();
        Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
        peersListMessage.put("peers", peersAddresses);
        Gson gson = new Gson();
        String messageJson = gson.toJson(peersListMessage);
        if (peerAddress.equals(PEER_ADDRESS)) return;
        PrintWriter outToPeer = peersConnections.stream().filter(connection ->
                connection.getPeerAddress().equals(peerAddress)).findFirst().get().getOutToPeerStream();
        outToPeer.println(messageJson);
        outToPeer.flush();
        LOGGER.info("This peer's stored peers addresses list has been sent to " + peerAddress);
    }

    void sendMyChainToPeer(Integer peerAddress) {
        Map<String, List<Block>> chainMessage = new HashMap<>();
        List<Block> chain = Blockchain.getSharedInstance().getBlocks();
        chainMessage.put("chain", chain);
        Gson gson = new Gson();
        String messageJson = gson.toJson(chainMessage);
        if (peerAddress.equals(PEER_ADDRESS)) return;
        PrintWriter outToPeer = peersConnections.stream().filter(connection ->
                connection.getPeerAddress().equals(peerAddress)).findFirst().get().getOutToPeerStream();
        outToPeer.println(messageJson);
        outToPeer.flush();
        LOGGER.info("This peer's local chain has been sent to " + peerAddress);
    }

}

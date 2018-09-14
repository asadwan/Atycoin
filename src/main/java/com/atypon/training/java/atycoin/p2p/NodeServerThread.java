package com.atypon.training.java.atycoin.p2p;

import com.atypon.training.java.atycoin.blockchain_core.Block;
import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.atypon.training.java.atycoin.transactions_system.AtycoinTransaction;
import com.atypon.training.java.atycoin.transactions_system.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class NodeServerThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(NodeServerThread.class.getName());

    private Set<Integer> peersAddresses = Node.getSharedInstance().getPeersAddresses();
    private Socket skt;
    private BufferedReader inputFromPeer;
    private Integer peerAddress; // The address of the peer this thread is handling communication with
    private Blockchain blockchain = Blockchain.getSharedInstance();
    private NodeClient nodeClient = NodeClient.getSharedInstance();
    private String message;
    private Gson gson = new Gson();

    NodeServerThread(Socket skt) {
        this.skt = skt;
        try {
            this.inputFromPeer = new BufferedReader(new InputStreamReader(skt.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while ((message = inputFromPeer.readLine()) != null) {
                dispatchMessagesToHandlers();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LOGGER.info("Bye from ServerThread handling communication with peer " + peerAddress);
            try {
                if (skt != null) skt.close();
                if (inputFromPeer != null) inputFromPeer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatchMessagesToHandlers() {
        Map messageMap = gson.fromJson(message, Map.class);
        if (messageMap.keySet().contains("peer")) {
            handleNewPeerMessage();
        } else if (messageMap.keySet().contains("transaction")) {
            handleNewTransactionMessage();
        } else if (messageMap.keySet().contains("block")) {
            handleNewBlockMessage();
        } else if (messageMap.keySet().contains("peers")) {
            handleNewPeersListMessage();
        } else if (messageMap.keySet().contains("chain")) {
            handleNewChainMessage();
        }
    }

    private void handleNewChainMessage() {
        LOGGER.info("Received peer '" + peerAddress + "' copy of the blockchain");
        Type stringArrayListType = new TypeToken<Map<String, ArrayList<Block>>>() {
        }.getType();
        Map<String, ArrayList<Block>> chainMap = gson.fromJson(message, stringArrayListType);
        ArrayList<Block> chain = chainMap.get("chain");
        blockchain.replaceChain(chain);
    }

    private void handleNewPeersListMessage() {
        LOGGER.info("Received peer '" + peerAddress + "' connected peers list");
        Type stringSetType = new TypeToken<Map<String, Set<Integer>>>() {
        }.getType();
        Map<String, Set<Integer>> peersMap = gson.fromJson(message, stringSetType);
        Set<Integer> newPeersAddresses = peersMap.get("peers");
        newPeersAddresses.removeAll(peersAddresses);
        for (Integer newPeerAddress : newPeersAddresses) {
            peersAddresses.add(newPeerAddress);
            nodeClient.connectToPeer(newPeerAddress);
            nodeClient.sendMyAddressToPeer(newPeerAddress);
            nodeClient.sendMyChainToPeer(newPeerAddress);
        }
    }

    private void handleNewBlockMessage() {

        LOGGER.info("A new block has been received from  " + peerAddress);
        Type stringBlockMapType = new TypeToken<Map<String, Block<AtycoinTransaction>>>() {
        }.getType();
        Map<String, Block> blockMapMap = gson.fromJson(message, stringBlockMapType);
        Block block = blockMapMap.get("block");
        new Thread(() -> blockchain.addBlock(block)).start();
    }

    private void handleNewTransactionMessage() {
        LOGGER.info("A new transaction has been received from  " + peerAddress);
        Type stringTransactionMapType = new TypeToken<Map<String, AtycoinTransaction>>() {
        }.getType();
        Map<String, Transaction> transactionMap = gson.fromJson(message, stringTransactionMapType);
        Transaction transaction = transactionMap.get("transaction");
        blockchain.addTransaction((AtycoinTransaction) transaction);
        LOGGER.info("A new transaction received from peer " + peerAddress +
                " has been added to the mempool ");
    }

    private void handleNewPeerMessage() {
        Type stringNodeMapType = new TypeToken<Map<String, Integer>>() {
        }.getType();
        Map<String, Integer> nodeMap = gson.fromJson(message, stringNodeMapType);
        Integer newPeerAddress = nodeMap.get("peer");
        peerAddress = newPeerAddress;
        LOGGER.info("An incoming connection from peer '" + newPeerAddress + "' has been established");
        if (!peersAddresses.contains(newPeerAddress)) {
            peersAddresses.add(newPeerAddress);
            nodeClient.connectToPeer(newPeerAddress);
            nodeClient.sendMyAddressToPeer(newPeerAddress); //Connect back to the peer
            nodeClient.sendConnectedPeersAddressesToPeer(newPeerAddress);
        }
        nodeClient.sendMyChainToPeer(newPeerAddress);
    }
}

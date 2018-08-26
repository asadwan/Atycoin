package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Block;
import com.atypon.training.java.traniningproject.Transaction;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


public class NodeClient {

    public final String HOST = "localhost";
    private final String INITIAL_NODES_FILE = "initial-nodes.txt";
    public ArrayList<Socket> nodesSkts = new ArrayList<>();
    public Set<Node> peers = new HashSet<>();
    private ArrayList<PrintWriter> outputStreams = new ArrayList<>();

    public void start() {
        loadSavedNodesAddresses();
        connectToNodes();
        broadcastMyAddress(null);
    }

    public void connectToNodes() {
        int nodePort;
        String nodeHost;
        for (Node node : peers) {
            nodePort = node.getPort();
            nodeHost = node.getHost();
            try (Socket skt = new Socket(nodeHost, nodePort);
                 PrintWriter pw = new PrintWriter(skt.getOutputStream())) {
                nodesSkts.add(skt);
                outputStreams.add(pw);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadSavedNodesAddresses() {
        try (BufferedReader br = new BufferedReader(new FileReader(INITIAL_NODES_FILE))) {
            String line;
            int port;
            while ((line = br.readLine()) != null) {
                port = Integer.parseInt(line);
                Node node = new Node(port);
                peers.add(node);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMyAddress(Node node) {
        Map<String, Node> myNodeMessage = new HashMap<>();
        myNodeMessage.put("node", node);
        Gson gson = new Gson();
        String messageJson = gson.toJson(myNodeMessage);
        for (PrintWriter ouputStream : outputStreams) {
            ouputStream.print(messageJson);
            ouputStream.flush();
        }
    }

    public void broadcastNewTransaction(Transaction transaction) {
        Map<String, Transaction> newTransactionMessage = new HashMap<>();
        newTransactionMessage.put("transaction", transaction);
        Gson gson = new Gson();
        String messageJson = gson.toJson(newTransactionMessage);
        for (PrintWriter ouputStream : outputStreams) {
            ouputStream.print(messageJson);
            ouputStream.flush();
        }
    }

    public void broadcastNewBlock(Block block) {
        Map<String, Block> newBlockMessage = new HashMap<>();
        newBlockMessage.put("block", block);
        Gson gson = new Gson();
        String messageJson = gson.toJson(newBlockMessage);
        for (PrintWriter ouputStream : outputStreams) {
            ouputStream.print(messageJson);
            ouputStream.flush();
        }
    }

    public void broadcastPeersList() {
        Map<String, Set<Node>> peersListMessage = new HashMap<>();
        peersListMessage.put("peers", peers);
        Gson gson = new Gson();
        String messageJson = gson.toJson(peersListMessage);
        for (PrintWriter ouputStream : outputStreams) {
            ouputStream.print(messageJson);
            ouputStream.flush();
        }
    }

}

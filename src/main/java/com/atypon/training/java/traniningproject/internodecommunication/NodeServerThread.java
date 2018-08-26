package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Block;
import com.atypon.training.java.traniningproject.Transaction;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;
import java.util.Set;

public class NodeServerThread implements Runnable {

    private Socket skt;
    private BufferedReader inputStream;

    public NodeServerThread(Socket skt) {
        this.skt = skt;
    }

    @Override
    public void run() {
        try {
            inputStream = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            String message;
            Gson gson = new Gson();

            while (true) {
                message = inputStream.readLine();
                Map messageMap = gson.fromJson(message, Map.class);
                if (messageMap.keySet().contains("node")) {
                    Type stringNodeMapType = new TypeToken<Map<String, Node>>() {
                    }.getType();
                    Map<String, Node> nodeMap = gson.fromJson(message, stringNodeMapType);
                    Node node = nodeMap.get("node");
                    new Thread(() -> {

                    });

                } else if (messageMap.keySet().contains("transaction")) {
                    Type stringTransactionMapType = new TypeToken<Map<String, Transaction>>() {
                    }.getType();
                    Map<String, Transaction> transactionMap = gson.fromJson(message, stringTransactionMapType);
                    Transaction transaction = transactionMap.get("transaction");
                    new Thread(() -> {

                    });

                } else if (messageMap.keySet().contains("block")) {
                    Type stringBlockMapType = new TypeToken<Map<String, Block>>() {
                    }.getType();
                    Map<String, Block> blockMapMap = gson.fromJson(message, stringBlockMapType);
                    Block block = blockMapMap.get("block");
                    new Thread(() -> {
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
        }
    }
}

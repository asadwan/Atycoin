package com.atypon.training.java.traniningproject;

import com.atypon.training.java.traniningproject.internodecommunication.Node;

public class TestApp {
    public static void main(String[] args) throws InterruptedException {

        Node myNode = new Node();

        myNode.server.start();


        myNode.client.start();

        Transaction transaction = new Transaction();

        myNode.client.broadcastNewTransaction(transaction);

        Block block = new Block();
        //myNode.client.broadcastNewBlock(block);

        myNode.server.join();

    }
}

package com.atypon.training.java.traniningproject.internodecommunication;

import com.atypon.training.java.traniningproject.Blockchain;
import com.atypon.training.java.traniningproject.Wallet;

import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Node {

    public transient NodeServer server;
    public transient NodeClient client;

    private Blockchain blockchain;
    private Wallet wallet;

    private Set<Integer> peersAddresses = new ConcurrentSkipListSet<>();

    private String host;
    private int port; // Represents the peer address


    public Node() {
        this.host = "localhost";
        this.port = getPortCounter();
        server = new NodeServer(port, peersAddresses);
        client = new NodeClient(port, peersAddresses);
        blockchain = new Blockchain();
        wallet = new Wallet();
        server.start();
        client.start();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Blockchain getBlockchain() {
        return blockchain;
    }

    public Wallet getWallet() {
        return wallet;
    }

    private Integer getPortCounter() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(
                    new File("/Users/asadwan/IntellijIDEAProjects/TrainingProject/src/main/java" +
                            "/com/atypon/training/java/traniningproject/internodecommunication/port.txt")));
            int port = Integer.parseInt(bufferedReader.readLine());
            PrintWriter printWriter = new PrintWriter(new FileWriter(
                    new File("/Users/asadwan/IntellijIDEAProjects/TrainingProject/src/main/java" +
                            "/com/atypon/training/java/traniningproject/internodecommunication/port.txt")));
            printWriter.write(String.valueOf(port + 1) + "\n");
            printWriter.close();
            bufferedReader.close();
            return port;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node node = (Node) o;
        return getPort() == node.getPort() &&
                Objects.equals(getHost(), node.getHost());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getHost(), getPort());
    }

}

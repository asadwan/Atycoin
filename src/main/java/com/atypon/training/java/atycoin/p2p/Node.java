package com.atypon.training.java.atycoin.p2p;

import java.io.*;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Node {

    private static volatile Node instance = new Node();

    private Set<Integer> peersAddresses = new ConcurrentSkipListSet<>();
    private String host = "localhost";
    private int port = getPortCounter(); // Represents the peer network address

    public static Node getSharedInstance() {
        if (instance == null) { // Check 1
            synchronized (Node.class) {
                if (instance == null) { // Check 2
                    instance = new Node();
                }
            }
        }
        return instance;
    }

    public Set<Integer> getPeersAddresses() {
        return peersAddresses;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    private Integer getPortCounter() {
        File portFile = new File("src/main/java/com/atypon/training/java/atycoin/p2p/port.txt");
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(portFile));
            int port = Integer.parseInt(bufferedReader.readLine());
            peersAddresses.add(port);
            PrintWriter printWriter = new PrintWriter(new FileWriter(portFile));
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

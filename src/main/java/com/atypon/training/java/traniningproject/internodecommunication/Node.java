package com.atypon.training.java.traniningproject.internodecommunication;

import java.util.Objects;

public final class Node {

    public NodeServer server;
    public NodeClient client;
    private String host;
    private int port;

    public Node(int port) {
        this.host = "localhost";
        this.port = port;
        server = new NodeServer(port);
        client = new NodeClient();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
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

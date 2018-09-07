package com.atypon.training.java.atycoin.p2p;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Objects;

public final class Connection {

    private Socket socket;
    private Integer peerAddress;
    private PrintWriter outToPeer;

    public Connection(Socket socket, Integer peerAddress) {
        this.socket = socket;
        this.peerAddress = peerAddress;
        try {
            this.outToPeer = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Integer getPeerAddress() {
        return peerAddress;
    }

    public PrintWriter getOutToPeerStream() {
        return outToPeer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(socket, that.socket) &&
                Objects.equals(peerAddress, that.peerAddress) &&
                Objects.equals(outToPeer, that.outToPeer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(socket, peerAddress, outToPeer);
    }

    @Override
    public String toString() {
        return "Connection{" +
                "socket=" + socket +
                ", peerAddress='" + peerAddress + '\'' +
                ", outToPeer=" + outToPeer +
                '}';
    }
}

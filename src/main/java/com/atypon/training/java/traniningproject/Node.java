package com.atypon.training.java.traniningproject;

import java.net.URI;
import java.net.URISyntaxException;

public final class Node {

    private URI address;

    public Node(String address) {
        try {
            this.address = new URI(address);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public URI getAddress() {
        return address;
    }
}

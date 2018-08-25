package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;


@JsonIgnoreProperties
public final class Block {

    static int blockCounter = -1;

    @Setter
    @Getter
    private int index;
    @Setter
    @Getter
    private Long timestamp;
    @Setter
    @Getter
    private int nonce;
    @Setter
    @Getter
    private String previousBlockHash;
    @Setter
    @Getter
    private ArrayList<Transaction> transactions;

    @Getter
    @Setter
    private Coinbase coinbase;

    public Block() {
    }

    public Block(String previousBlockHash, Coinbase coinbase, ArrayList<Transaction> transactions) {
        this.index = ++blockCounter;
        this.coinbase = coinbase;
        this.timestamp = new Date().getTime();
        this.previousBlockHash = previousBlockHash;
        this.transactions = (ArrayList<Transaction>) transactions.clone();
    }

    public String getHash() {
        String hash = Utility.sha256(this.toString()).toUpperCase();
        return hash;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public void mine(int difficulty) {
        //Create a string with (difficulty * "0")
        String target = Utility.repeat("0", difficulty);
        while (!getHash().startsWith(target)) {
            nonce++;
        }
    }

    @Override
    public String toString() {
        return index + timestamp.toString() + nonce + previousBlockHash + transactions;
    }
}

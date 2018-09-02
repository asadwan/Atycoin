package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;


@JsonIgnoreProperties
public final class Block {


    private int index;
    private Long timestamp;
    private int nonce;
    private String previousBlockHash;
    private ArrayList<Transaction> transactions;
    private Coinbase coinbase;

    public Block(String previousBlockHash, Coinbase coinbase, ArrayList<Transaction> transactions) {
        this.index = Blockchain.getSharedInstance().getBlocks().size() + 1;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public int getIndex() {
        return index;
    }

    public int getNonce() {
        return nonce;
    }

    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    public Coinbase getCoinbase() {
        return coinbase;
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
        return "Block{" +
                "index=" + index +
                ", timestamp=" + timestamp +
                ", nonce=" + nonce +
                ", previousBlockHash='" + previousBlockHash + '\'' +
                ", transactions=" + transactions +
                ", coinbase=" + coinbase +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return index == block.index &&
                nonce == block.nonce &&
                Objects.equals(timestamp, block.timestamp) &&
                Objects.equals(previousBlockHash, block.previousBlockHash) &&
                Objects.equals(transactions, block.transactions) &&
                Objects.equals(coinbase, block.coinbase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, timestamp, nonce, previousBlockHash, transactions, coinbase);
    }
}

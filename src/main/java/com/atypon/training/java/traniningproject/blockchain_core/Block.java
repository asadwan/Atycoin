package com.atypon.training.java.traniningproject.blockchain_core;

import com.atypon.training.java.traniningproject.transactions_system.AtycoinTransaction;
import com.atypon.training.java.traniningproject.transactions_system.Coinbase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.Objects;

import static com.atypon.training.java.traniningproject.utility.Utility.repeat;
import static com.atypon.training.java.traniningproject.utility.Utility.sha256;


@JsonIgnoreProperties
public final class Block {


    private int index;
    private Long timestamp;
    private int nonce;
    private String previousBlockHash;
    private AtycoinTransaction transaction;
    private Coinbase coinbase;

    public Block(String previousBlockHash, Coinbase coinbase, AtycoinTransaction transaction) {
        this.index = Blockchain.getSharedInstance().getBlocks().size() + 1;
        this.coinbase = coinbase;
        this.timestamp = new Date().getTime();
        this.previousBlockHash = previousBlockHash;
        this.transaction = transaction;
    }

    public String getHash() {
        String hash = sha256(this.toString()).toUpperCase();
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

    public AtycoinTransaction getTransaction() {
        return transaction;
    }

    public Coinbase getCoinbase() {
        return coinbase;
    }

    public void mine(int difficulty) {
        //Create a string with (difficulty * "0")
        String target = repeat("0", difficulty);
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
                ", transaction=" + transaction +
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
                Objects.equals(transaction, block.transaction) &&
                Objects.equals(coinbase, block.coinbase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, timestamp, nonce, previousBlockHash, transaction, coinbase);
    }
}

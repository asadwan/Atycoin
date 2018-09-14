package com.atypon.training.java.atycoin.blockchain_core;

import com.atypon.training.java.atycoin.transactions_system.Coinbase;
import com.atypon.training.java.atycoin.transactions_system.Transaction;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.atypon.training.java.atycoin.utility.Utility.repeat;
import static com.atypon.training.java.atycoin.utility.Utility.sha256;

@JsonIgnoreProperties
public final class Block<T extends Transaction> {

    private int index;
    private Long timestamp;
    private int nonce;
    private String previousBlockHash;
    private List<T> transactions;
    private Coinbase coinbase;

    private Block(String previousBlockHash, Coinbase coinbase,
                  List<T> transactions) {
        this.index = Blockchain.getSharedInstance().getBlocks().size() + 1;
        this.coinbase = coinbase;
        this.timestamp = new Date().getTime();
        this.previousBlockHash = previousBlockHash;
        this.transactions = transactions;
    }

    public static Block createInstance(String previousBlockHash, Coinbase coinbase,
                                       List<Transaction> transactions) {
        return new Block(previousBlockHash, coinbase, transactions);
    }

    public String getHash() {
        return sha256(this.toString());
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

    public List<T> getTransactions() {
        return transactions;
    }

    public Coinbase getCoinbase() {
        return coinbase;
    }

    public void mine(int difficulty) {
        //Create a string with (DIFFICULTY * "0")
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

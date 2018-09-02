package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties
public final class Coinbase {

    private TransactionOutput blockReward;

    public Coinbase(String minerAddress) {
        blockReward = new TransactionOutput(minerAddress, 100f);
    }

    public TransactionOutput getBlockReward() {
        return blockReward;
    }

    @Override
    public String toString() {
        return "Coinbase{" +
                "blockReward=" + blockReward +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coinbase coinbase = (Coinbase) o;
        return Objects.equals(blockReward, coinbase.blockReward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockReward);
    }
}

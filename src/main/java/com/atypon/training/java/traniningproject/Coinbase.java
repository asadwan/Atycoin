package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public final class Coinbase {

    private TransactionOutput blockReward;

    public Coinbase(String minerAddress) {
        blockReward = new TransactionOutput(minerAddress, 100f);
    }

    public TransactionOutput getBlockReward() {
        return blockReward;
    }
}

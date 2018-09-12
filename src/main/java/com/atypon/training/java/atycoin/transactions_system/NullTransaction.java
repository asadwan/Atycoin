package com.atypon.training.java.atycoin.transactions_system;

import java.security.PrivateKey;
import java.util.ArrayList;

public final class NullTransaction implements Transaction {

    public String message;

    public NullTransaction() {
        message = "Transaction Failed: no sufficient funds.";
    }

    @Override
    public void generateSignature(PrivateKey privateKey) {

    }

    @Override
    public void processTransaction() {
    }

    @Override
    public String getTransactionId() {
        return null;
    }

    @Override
    public boolean isTransactionValid() {
        return false;
    }

    @Override
    public ArrayList<TransactionInput> getInputs() {
        return null;
    }

    @Override
    public ArrayList<TransactionOutput> getOutputs() {
        return null;
    }
}

package com.atypon.training.java.atycoin.transactions_system;

import java.security.PrivateKey;
import java.util.ArrayList;

public interface Transaction {

    void generateSignature(PrivateKey privateKey);

    void processTransaction();

    String getTransactionId();

    boolean isTransactionValid();

    ArrayList<TransactionInput> getInputs();

    ArrayList<TransactionOutput> getOutputs();
}

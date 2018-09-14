package com.atypon.training.java.atycoin.transactions_system;

import java.security.PrivateKey;
import java.util.List;

public interface Transaction {

    void generateSignature(PrivateKey privateKey);

    void processTransaction();

    String getTransactionId();

    boolean isTransactionValid();

    List<TransactionInput> getInputs();

    List<TransactionOutput> getOutputs();

    String getSenderPublicKeyString();

}



package com.atypon.training.java.atycoin.transactions_system;

import java.security.PublicKey;
import java.util.ArrayList;

public class TransactionFactory {

    private TransactionFactory() {
    }

    static Transaction getTransaction(PublicKey senderPublicKey, String recipientAddress,
                                                    float amount, ArrayList<TransactionInput> inputs) {
        return new AtycoinTransaction(senderPublicKey, recipientAddress, amount, inputs);
    }

    public static Transaction getNullTransaction() {
        return new NullTransaction();
    }
}


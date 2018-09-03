package com.atypon.training.java.traniningproject;

public class NullTransaction extends Transaction {

    public String message;

    public NullTransaction() {
        message = "Transaction Failed";
    }

    @Override
    public byte[] getSignature() {
        return null;
    }
}

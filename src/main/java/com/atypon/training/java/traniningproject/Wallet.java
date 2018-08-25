package com.atypon.training.java.traniningproject;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;

public final class Wallet {

    public PrivateKey privateKey;
    public PublicKey publicKey;

    public Wallet() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public float getBalance() {
        float balance = 0;
        for (TransactionOutput UTXO : Blockchain.UTXOs.values()) {
            if (UTXO.isMine(publicKey)) {
                balance += UTXO.getAmount();
            }
        }
        return balance;
    }

    public Transaction sendCoin(PublicKey recipient, float amount) {
        if (getBalance() < amount) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (TransactionOutput UTXO : Blockchain.UTXOs.values()) {
            if (!UTXO.isMine(publicKey)) {
                continue;
            }
            total += UTXO.getAmount();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > amount) {
                break;
            }
        }

        Transaction newTransaction = new Transaction(this.publicKey, recipient, amount, inputs);
        newTransaction.generateSignature(this.privateKey);

        for (TransactionInput input : inputs) {
            Blockchain.UTXOs.remove(input.getUTXO().getId());
        }

        return newTransaction;
    }


}

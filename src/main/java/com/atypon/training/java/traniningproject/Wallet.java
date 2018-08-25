package com.atypon.training.java.traniningproject;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;

import static com.atypon.training.java.traniningproject.Utility.sha160;

public final class Wallet {

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
    }

    public PrivateKey privateKey;
    public PublicKey publicKey;
    public String address;

    public Wallet() {
        generateKeyPair();
        address = sha160(this.publicKey.toString());
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
            if (UTXO.isMine(address)) {
                balance += UTXO.getAmount();
            }
        }
        return balance;
    }

    public Transaction sendCoin(String recipientAddress, float amount) {
        if (getBalance() < amount) {
            System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }

        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (TransactionOutput UTXO : Blockchain.UTXOs.values()) {
            if (!UTXO.isMine(address)) {
                continue;
            }
            total += UTXO.getAmount();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > amount) {
                break;
            }
        }

        Transaction newTransaction = new Transaction(this.publicKey, recipientAddress, amount, inputs);
        newTransaction.generateSignature(this.privateKey);

//        for (TransactionInput input : inputs) {
//            System.out.println(input.getUTXO().getId());
//            Blockchain.UTXOs.remove(input.getUTXO().getId());
//        }

        return newTransaction;
    }


}

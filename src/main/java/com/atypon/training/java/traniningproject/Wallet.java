package com.atypon.training.java.traniningproject;

import org.springframework.web.bind.annotation.RestController;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.logging.Logger;

import static com.atypon.training.java.traniningproject.Utility.sha160;

@RestController
public final class Wallet {

    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());

    private static final Wallet wallet = new Wallet();

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String address;

    private Wallet() {
        generateKeyPair();
        address = sha160(Utility.getStringFromPublicKey(publicKey));
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

    public static Wallet getSharedInstance() {
        return wallet;
    }

    public float getBalance() {
        float balance = 0;
        for (TransactionOutput UTXO : Blockchain.getSharedInstance().getUTXOs().values()) {
            if (UTXO.isMine(address)) {
                balance += UTXO.getAmount();
            }
        }
        return balance;
    }

    public Transaction sendCoin(String recipientAddress, float amount) {
        if (getBalance() < amount) {
            LOGGER.info("No enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TransactionInput> inputs = getInputs(amount);
        Transaction newTransaction = new Transaction(this.publicKey, recipientAddress, amount, inputs);
        newTransaction.processTransaction();
        newTransaction.generateSignature(this.privateKey);
        return newTransaction;
    }

    private ArrayList<TransactionInput> getInputs(float amount) {
        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        for (TransactionOutput UTXO : Blockchain.getSharedInstance().getUTXOs().values()) {
            if (!UTXO.isMine(address)) continue;
            total += UTXO.getAmount();
            inputs.add(new TransactionInput(UTXO.getId(), UTXO));
            if (total > amount) break;
        }
        return inputs;
    }

    public String getAddress() {
        return address;
    }


}

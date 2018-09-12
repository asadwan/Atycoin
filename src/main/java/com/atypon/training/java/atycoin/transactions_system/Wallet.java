package com.atypon.training.java.atycoin.transactions_system;

import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.atypon.training.java.atycoin.utility.Utility;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.atypon.training.java.atycoin.utility.Utility.sha160;

public final class Wallet {

    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());


    public Map<String, TransactionOutput> localUTXOs = new HashMap<>();

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String address;

    private static final Wallet WALLET = new Wallet();

    private Wallet() {
        generateKeyPair();
        address = sha160(Utility.getStringFromPublicKey(publicKey));
    }

    public static Wallet getSharedInstance() {
        return WALLET;
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
        for (TransactionOutput utxo : localUTXOs.values()) {
            if (utxo.isMine(address)) {
                balance += utxo.getAmount();
            }
        }
        return balance;
    }

    public Transaction sendCoin(String recipientAddress, float amount) {
        if (getBalance() < amount) {
            LOGGER.info("No enough funds to send transaction. Transaction Discarded.");
            return new NullTransaction();
        }
        ArrayList<TransactionInput> inputs = getInputs(amount);
        AtycoinTransaction newTransaction = TransactionFactory.getTransaction(this.publicKey, recipientAddress,
                amount, inputs);
        newTransaction.processTransaction();
        newTransaction.generateSignature(this.privateKey);
        return newTransaction;
    }

    private ArrayList<TransactionInput> getInputs(float amount) {
        ArrayList<TransactionInput> inputs = new ArrayList<>();
        float total = 0;
        for (TransactionOutput UTXO : localUTXOs.values()) {
            if (!UTXO.isMine(address)) continue;
            total += UTXO.getAmount();
            inputs.add(new TransactionInput(UTXO.getId(), UTXO));
            if (total >= amount) break;
        }
        return inputs;
    }

    public String getAddress() {
        return address;
    }

    public void updateLocalUTXOsOnNewTransactionConfirmed(ArrayList<AtycoinTransaction> transactions) {
        for (AtycoinTransaction transaction : transactions) {
            for (TransactionOutput output : transaction.getOutputs()) {
                if (sha160(transaction.getSenderPublicKeyString()).equals(address) && output.isMine(address))
                    continue;
                localUTXOs.put(output.getId(), output);
            }
            removeSTXOsFromLocalUTXOList(transaction.getInputs());
        }
    }

    public void addUTXOsToLocalUTXOsList(ArrayList<TransactionOutput> outputs) {
        for (TransactionOutput transactionOutput : outputs) {
            localUTXOs.put(transactionOutput.getId(), transactionOutput);
        }
    }

    public void removeSTXOsFromLocalUTXOList(ArrayList<TransactionInput> inputs) {
        for (TransactionInput transactionInput : inputs) {
            localUTXOs.remove(transactionInput.getUTXO().getId());
        }

    }
}

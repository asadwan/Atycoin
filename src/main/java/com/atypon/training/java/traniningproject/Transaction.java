package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

import static com.atypon.training.java.traniningproject.Utility.*;

@JsonIgnoreProperties
public final class Transaction {


    private static int sequence = 0;
    public String transactionId;

    @JsonIgnore
    public PublicKey sender;
    public String recipientAddress;
    public float amount;
    public byte[] signature;
    public ArrayList<TransactionInput> inputs = new ArrayList<>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<>();

    @JsonIgnore
    private boolean signatureWasGenerated = false;


    //For Coinbase transaction use
    public Transaction(PublicKey recipient) {
        this.sender = null;
        this.recipientAddress = recipientAddress;
        this.amount = 100f;
        this.inputs = null;
    }

    public Transaction(PublicKey sender, String recipientAddress, float amount, ArrayList<TransactionInput> inputs) {
        this.sender = sender;
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.inputs = inputs;
    }

    public Transaction() {
    }

    public boolean processTransaction() {
        if (verifySignature() == false) {
            return false;
        }

        // Verify inputs are not spent
        for (TransactionInput input : inputs) {
            input.setUTXO(Blockchain.UTXOs.get(input.getTransactionOutputId()));
        }

        // Check if inputs amount is greater than minimum transaction amount
        if (getInputsAmount() < Blockchain.minimumTransaction) {
            System.out.println("Transaction Inputs too small: " + getInputsAmount());
            System.out.println("Please enter the amount greater than " + Blockchain.minimumTransaction);
            return false;
        }

        // Generate outputs
        float change = getInputsAmount() - amount;
        transactionId = generateTransactionHash();
        // Send transaction amount to the recipient
        TransactionOutput transactionOutputToRecipient = new TransactionOutput(recipientAddress,
                amount, transactionId);
        outputs.add(transactionOutputToRecipient);
        if (change > 0) {
            // Send change back to sender
            TransactionOutput transactionOutputToSender = new TransactionOutput(sha160(sender.toString()),
                    change, transactionId);
            outputs.add(transactionOutputToSender);
        }

        // Add outputs to UTXOs list
        for (TransactionOutput output : outputs) {
            Blockchain.UTXOs.put(output.getId(), output);
        }

        // Remove inputs from UTXOs list
        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null) {
                continue;
            }
            Blockchain.UTXOs.remove(input.getUTXO().getId());
        }

        return true;
    }

    public float getInputsAmount() {
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null)
                continue; //if Transaction can't be found skip it, This behavior may not be optimal.
            total += input.getUTXO().getAmount();
        }
        return total;
    }

    public float getOutputsAmount() {
        float total = 0;
        for (TransactionOutput output : outputs) {
            total += output.getAmount();
        }
        return total;
    }

    public void generateSignature(PrivateKey privateKey) {
        String data;
        data = getStringFromKey(sender) + recipientAddress + amount;
        signature = applyECDSASignuture(privateKey, data);
        signatureWasGenerated = true;
    }

    public boolean verifySignature() {
        String data;
        if (signatureWasGenerated) {
            data = getStringFromKey(sender) + recipientAddress + amount;
            return verifyECDSASignuture(sender, signature, data);
        }
        System.out.println("Can't verify transaction signature, it has not yet been generated");
        return false;
    }

    private String generateTransactionHash() {
        ++sequence;
        String hash = sha256(getStringFromKey(sender) + recipientAddress
                + amount + sequence);
        return hash;
    }

    @Override
    public String toString() {
        return getStringFromKey(sender) + "  " + "To: " + recipientAddress + "  " + "Amount: " + amount;
    }
}

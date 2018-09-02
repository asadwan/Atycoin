package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.atypon.training.java.traniningproject.Utility.*;

@JsonIgnoreProperties
public final class Transaction {


    private static int sequence = 0;

    public transient PublicKey sender;
    private String transactionId;
    public String recipientAddress;
    public float amount;
    public byte[] signature;
    private ArrayList<TransactionInput> inputs = new ArrayList<>();
    private ArrayList<TransactionOutput> outputs = new ArrayList<>();

    private transient boolean signatureWasGenerated = false;


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

    public String getTransactionId() {
        return transactionId;
    }

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    public ArrayList<TransactionOutput> getOutputs() {
        return outputs;
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
        String hash = sha256(this.toString() + sequence);
        return hash;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", amount=" + amount +
                ", signature=" + Arrays.toString(signature) +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Float.compare(that.amount, amount) == 0 &&
                signatureWasGenerated == that.signatureWasGenerated &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(sender, that.sender) &&
                Objects.equals(recipientAddress, that.recipientAddress) &&
                Arrays.equals(signature, that.signature) &&
                Objects.equals(inputs, that.inputs) &&
                Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(transactionId, sender, recipientAddress, amount, inputs, outputs, signatureWasGenerated);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}

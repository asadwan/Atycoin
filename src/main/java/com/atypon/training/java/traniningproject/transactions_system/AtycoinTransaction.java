package com.atypon.training.java.traniningproject.transactions_system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static com.atypon.training.java.traniningproject.utility.Utility.*;

@JsonIgnoreProperties
public final class AtycoinTransaction implements Transaction {

    private static int sequence = 0;

    private String senderPublicKeyString;
    private String transactionId;
    private String recipientAddress;
    private float amount;
    private byte[] signature;

    private ArrayList<TransactionInput> inputs = new ArrayList<>();
    private ArrayList<TransactionOutput> outputs = new ArrayList<>();


    public AtycoinTransaction(PublicKey senderPublicKey, String recipientAddress,
                              float amount, ArrayList<TransactionInput> inputs) {
        this.senderPublicKeyString = getStringFromPublicKey(senderPublicKey);
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.inputs = inputs;
    }

    public AtycoinTransaction() {
    }

    @Override
    public void processTransaction() {
        generateOutputs();
        Wallet.getSharedInstance().addUTXOsToLocalUTXOsList(outputs);
        Wallet.getSharedInstance().removeSTXOsFromLocalUTXOList(inputs);
    }

    private void generateOutputs() {
        float change = getInputsAmount() - amount;
        transactionId = generateTransactionHash();
        TransactionOutput transactionOutputToRecipient = new TransactionOutput(recipientAddress,
                amount, transactionId);
        outputs.add(transactionOutputToRecipient);
        if (change > 0) {
            TransactionOutput transactionOutputToSender = new TransactionOutput(Wallet.getSharedInstance().getAddress(),
                    change, transactionId);
            outputs.add(transactionOutputToSender);
        }
    }

    public float getInputsAmount() {
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null)
                continue; //if AtycoinTransaction can't be found skip it, This behavior may not be optimal.
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

    public String getSenderPublicKeyString() {
        return senderPublicKeyString;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public float getAmount() {
        return amount;
    }

    public byte[] getSignature() {
        return signature;
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

    @Override
    public void generateSignature(PrivateKey privateKey) {
        signature = applyECDSASignuture(privateKey, this.toString());
    }

    @JsonIgnore
    public boolean isSignatureValid() {
        PublicKey senderPublicKey = getPublicKeyFromString(senderPublicKeyString);
        return verifyECDSASignuture(senderPublicKey, signature, this.toString());
    }

    @JsonIgnore
    public boolean isTransactionValid() {
        return areInputsValid() && isSignatureValid();
    }

    @JsonIgnore
    private boolean areInputsValid() {
        for (TransactionInput transactionInput : inputs) {
            if (!isInputValid(transactionInput)) return false;
        }
        return true;
    }

    private boolean isInputValid(TransactionInput transactionInput) {
        boolean isInputUnspent = transactionInput.getUTXO().isUnspent();
        boolean doesSenderOwnsInput = transactionInput.getUTXO().getRecipientAddress().
                equals(sha160(senderPublicKeyString));
        return isInputUnspent && doesSenderOwnsInput;
    }

    private String generateTransactionHash() {
        ++sequence;
        return sha256(this.toString() + sequence);
    }

    @Override
    public String toString() {
        return "AtycoinTransaction{" +
                "transactionId='" + transactionId + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", amount=" + amount +
                ", inputs=" + inputs +
                ", outputs=" + outputs +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AtycoinTransaction that = (AtycoinTransaction) o;
        return Float.compare(that.amount, amount) == 0 &&
                Objects.equals(transactionId, that.transactionId) &&
                Objects.equals(senderPublicKeyString, that.senderPublicKeyString) &&
                Objects.equals(recipientAddress, that.recipientAddress) &&
                Arrays.equals(signature, that.signature) &&
                Objects.equals(inputs, that.inputs) &&
                Objects.equals(outputs, that.outputs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(transactionId, senderPublicKeyString, recipientAddress, amount, inputs, outputs);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }
}

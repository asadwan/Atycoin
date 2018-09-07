package com.atypon.training.java.atycoin.transactions_system;

import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

import static com.atypon.training.java.atycoin.utility.Utility.sha256;

@JsonIgnoreProperties
public final class TransactionOutput {

    static transient int number = 0; // used to guarantee unique id for coinbase transaction output

    private String id;
    private String recipientAddress;
    private float amount;

    // The id of the transaction where this output was generated
    private String parentTransactionId;

    public TransactionOutput(String recipientAddress, float amount, String parentTransactionId) {
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = sha256(recipientAddress + amount + parentTransactionId);
    }

    // For coinbase AtycoinTransaction use
    public TransactionOutput(String recipientAddress, float amount) {
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.id = sha256(recipientAddress + amount + number++);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public boolean isUnspent() {
        return Blockchain.getSharedInstance().getUTXOs().containsKey(this.id);
    }

    // Checks whether this coin belongs to this public key owner
    @JsonIgnore
    public boolean isMine(String address) {
        return (address.equals(recipientAddress));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionOutput that = (TransactionOutput) o;
        return Float.compare(that.amount, amount) == 0 &&
                Objects.equals(id, that.id) &&
                Objects.equals(recipientAddress, that.recipientAddress) &&
                Objects.equals(parentTransactionId, that.parentTransactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recipientAddress, amount, parentTransactionId);
    }

    @Override
    public String toString() {
        return "TransactionOutput{" +
                "id='" + id + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", amount=" + amount +
                ", parentTransactionId='" + parentTransactionId + '\'' +
                '}';
    }
}

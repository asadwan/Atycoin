package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import static com.atypon.training.java.traniningproject.Utility.sha256;

@JsonIgnoreProperties
public class TransactionOutput {

    @JsonIgnore
    static int number = 0; // used to guarantee unique id for coinbase transaction output
    @Setter
    @Getter
    private String id;
    @Setter
    @Getter
    private String recipientAddress;
    @Setter
    @Getter
    private float amount;
    // The id of the transaction where this output was generated
    @JsonIgnore
    private String parentTransactionId;

    public TransactionOutput(String recipientAddress, float amount, String parentTransactionId) {
        this.recipientAddress = recipientAddress;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = sha256(recipientAddress + amount + parentTransactionId);
    }

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

    // Checks whether this coin belongs to this public key owner
    @JsonIgnore
    public boolean isMine(String address) {
        return (address.equals(recipientAddress));
    }
}

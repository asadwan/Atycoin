package com.atypon.training.java.traniningproject;

import java.security.PublicKey;

import static com.atypon.training.java.traniningproject.Utility.getStringFromKey;
import static com.atypon.training.java.traniningproject.Utility.sha256;

public class TransactionOutput {

    private String id;
    private PublicKey recipient;
    private float amount;

    // The id of the transaction where this output was generated
    private String parentTransactionId;

    public TransactionOutput(PublicKey recipient, float amount, String parentTransactionId) {
        this.recipient = recipient;
        this.amount = amount;
        this.parentTransactionId = parentTransactionId;
        this.id = sha256(getStringFromKey(recipient) + amount + parentTransactionId);
    }

    public String getId() {
        return id;
    }

    public PublicKey getRecipient() {
        return recipient;
    }

    public float getAmount() {
        return amount;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    // Checks whether this coin belongs to this public key owner
    public boolean isMine(PublicKey publicKey) {
        return (publicKey.equals(recipient));
    }
}

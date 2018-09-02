package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties
public final class TransactionInput {

    private String transactionOutputId;
    private TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionInput() {
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }

    @Override
    public String toString() {
        return "TransactionInput{" +
                "transactionOutputId='" + transactionOutputId + '\'' +
                ", UTXO=" + UTXO +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionInput that = (TransactionInput) o;
        return Objects.equals(transactionOutputId, that.transactionOutputId) &&
                Objects.equals(UTXO, that.UTXO);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionOutputId, UTXO);
    }
}

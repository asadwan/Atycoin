package com.atypon.training.java.traniningproject.transactions_system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties
public final class TransactionInput {

    private String transactionOutputId;
    private TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId, TransactionOutput UTXO) {
        this.transactionOutputId = transactionOutputId;
        this.UTXO = UTXO;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
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

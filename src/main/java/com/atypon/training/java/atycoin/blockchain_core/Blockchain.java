package com.atypon.training.java.atycoin.blockchain_core;

import com.atypon.training.java.atycoin.transactions_system.*;
import com.atypon.training.java.atycoin.utility.Utility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

@JsonIgnoreProperties
public final class Blockchain {

    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());

    // Singlton
    private static volatile Blockchain INSTANCE = new Blockchain();

    public static int difficulty = 4;

    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private ArrayList<Block> chain = new ArrayList<>();
    private BlockingQueue<AtycoinTransaction> mempool = new LinkedBlockingQueue<>();

    // Singlton
    public static Blockchain getSharedInstance() {
        if (INSTANCE == null) { // Check 1
            synchronized (Blockchain.class) {
                if (INSTANCE == null) { // Check 2
                    INSTANCE = new Blockchain();
                }
            }
        }
        return INSTANCE;
    }

    public void createGenesisBlock() {
        String genesisBlockPreviousHash = Utility.repeat("0", 64);
        mineBlock(genesisBlockPreviousHash);
    }

    public ArrayList<Block> getBlocks() {
        return chain;
    }

    public void addBlock(Block block) {
        if (!isBlockValid(block)) {
            LOGGER.info("A new block received has been found " + "invalid and was not added to the chain");
            return;
        }
        chain.add(block);
        removeTransactionsFromMempool(block);
        updateUTXOsListOnAddBlock(block);
        LOGGER.info("A new block has been added to the chain");
    }

    private boolean isBlockValid(Block block) {
        if (!isHashChainValid(getPreviousBlock(), block)) return false;
        String target = Utility.repeat("0", difficulty);
        boolean isPoWValid = block.getHash().startsWith(target);
        return isPoWValid;
    }

    public int getLength() {
        return chain.size();
    }

    @JsonIgnore
    public HashMap<String, TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public Block mineBlock(String previousHash) {
        Coinbase coinbase = new Coinbase(Wallet.getSharedInstance().getAddress());
        ArrayList<AtycoinTransaction> pickedTransaction = pickTransactionForNextBlock();
        Block block = new Block(previousHash, coinbase, pickedTransaction);
        block.mine(difficulty);
        chain.add(block);
        updateUTXOsListOnAddBlock(block);
        return block;
    }

    private ArrayList<AtycoinTransaction> pickTransactionForNextBlock() {
        AtycoinTransaction pickedTransaction;
        ArrayList<AtycoinTransaction> pickedTransactions = new ArrayList<>();
        while (true) {
            if ((pickedTransaction = mempool.poll()) == null) return pickedTransactions;
            if (pickedTransaction.isTransactionValid()) {
                pickedTransactions.add(pickedTransaction);
                return pickedTransactions;
            }
        }
    }

    @JsonIgnore
    public Block getPreviousBlock() {
        return chain.get(chain.size() - 1);
    }

    @JsonIgnore
    public BlockingQueue<AtycoinTransaction> getMempool() {
        return mempool;
    }

    public void addTransaction(AtycoinTransaction transaction) {
        this.mempool.add(transaction);
    }

    public boolean isChainValid(ArrayList<Block> chain) {
        for (int i = 1; i < chain.size(); i++) {
            if (!isHashChainValid(chain.get(i - 1), chain.get(i))) return false;
            if (!isProofOfWorkValid(chain.get(i - 1))) return false;
        }
        return true;
    }

    private boolean isHashChainValid(Block previousBlock, Block currentBlock) {
        return currentBlock.getPreviousBlockHash().equalsIgnoreCase(previousBlock.getHash());
    }

    private boolean isProofOfWorkValid(Block currentBlock) {
        String target = Utility.repeat("0", difficulty);
        boolean proofOfWorkValid = currentBlock.getHash().startsWith(target);
        return proofOfWorkValid;
    }

    public boolean replaceChain(ArrayList<Block> otherChain, Integer fromPeer) {
        if ((otherChain.size() == this.chain.size() && isOtherChainOlderThanMyChain(otherChain))
                || otherChain.size() > this.chain.size()) {
            this.chain.clear();
            this.chain.addAll(otherChain);
            updateUTXOsListOnReplaceChain(otherChain);
            updateMempool(otherChain);
            Wallet.getSharedInstance().localUTXOs.clear();
            UTXOs.values().stream().forEach(output -> Wallet.getSharedInstance().localUTXOs.put(output.getId(), output));
            return true;
        }
        return false;
    }

    private boolean isOtherChainOlderThanMyChain(ArrayList<Block> otherChain) {
        Long otherChainLastBlockTimeStamp = otherChain.get(otherChain.size() - 1).getTimestamp();
        Long myChainLastBlockTimestamp = getPreviousBlock().getTimestamp();
        return (otherChainLastBlockTimeStamp < myChainLastBlockTimestamp);
    }

    public void updateUTXOsListOnReplaceChain(ArrayList<Block> otherChain) {
        UTXOs.clear();
        for (Block block : otherChain) {
            TransactionOutput coinbaseTx = block.getCoinbase().getBlockReward();
            updateUTXOsListOnAddBlock(block);
            UTXOs.put(coinbaseTx.getId(), coinbaseTx);
        }
    }

    public void updateUTXOsListOnAddBlock(Block block) {
        UTXOs.put(block.getCoinbase().getBlockReward().getId(), block.getCoinbase().getBlockReward());
        Wallet.getSharedInstance().localUTXOs.put(block.getCoinbase().getBlockReward().getId(),
                block.getCoinbase().getBlockReward());
        removeSTXOsFromUTXOList(block.getTransactions());
        addUTXOsToUTXOsList(block.getTransactions());
        Wallet.getSharedInstance().updateLocalUTXOsOnNewTransactionConfirmed(block.getTransactions());
    }

    public void removeSTXOsFromUTXOList(ArrayList<AtycoinTransaction> transactions) {
        for (AtycoinTransaction transaction : transactions) {
            for (TransactionInput transactionInput : transaction.getInputs()) {
                UTXOs.remove(transactionInput.getTransactionOutputId());
            }
        }
    }

    public void addUTXOsToUTXOsList(ArrayList<AtycoinTransaction> transactions) {
        for (AtycoinTransaction transaction : transactions) {
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                UTXOs.put(transactionOutput.getId(), transactionOutput);
            }
        }
    }

    private void updateMempool(ArrayList<Block> otherChain) {
        for (Block block : chain) {
            removeTransactionsFromMempool(block);
        }
    }

    private void removeTransactionsFromMempool(Block block) {
        mempool.removeIf(trx -> trx.getTransactionId().equals(block.getTransactions().get(0).getTransactionId()));
    }
}

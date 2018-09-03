package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

@JsonIgnoreProperties
public final class Blockchain {

    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());

    // Singlton
    private static volatile Blockchain INSTANCE = new Blockchain();

    public static int difficulty = 5;

    private HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private ArrayList<Block> chain = new ArrayList<>();

    private List<Transaction> mempool = Collections.synchronizedList(new ArrayList<>());

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
        updateUTXOsListOnAddBlock(block);
        LOGGER.info("A new block has been added to the chain");
    }

    private boolean isBlockValid(Block block) {
        Block previousBlock = getPreviousBlock();
        if (!block.getPreviousBlockHash().equalsIgnoreCase(previousBlock.getHash())) return false;
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
        UTXOs.put(coinbase.getBlockReward().getId(), coinbase.getBlockReward());
        List<Transaction> pickedTransactions = pickTransactionsForNextBlock();
        Block block = new Block(previousHash, coinbase, pickedTransactions);
        block.mine(difficulty);
        chain.add(block);
        updateUTXOsListOnAddBlock(block);
        return block;
    }

    private List<Transaction> pickTransactionsForNextBlock() {
        List<Transaction> pickedTransactions = new ArrayList<Transaction>();
        for (Transaction transaction : mempool) {
            if (transaction.isTransactionValid()) {
                pickedTransactions.add(transaction);
                //mempool.removeIf(trx -> transaction == trx);
            } else {
                LOGGER.info("Transaction " + transaction.getTransactionId() + " has been found invalid" +
                        "and was discarded and will not be included in block " + (chain.size() + 1));
            }
        }
        mempool.clear();
        return pickedTransactions;
    }

    @JsonIgnore
    public Block getPreviousBlock() {
        return chain.get(chain.size() - 1);
    }

    public boolean isChainValid(ArrayList<Block> chain) {
        Block previousBlock;
        Block currentBlock;
        String target = Utility.repeat("0", difficulty);
        for (int i = 1; i < chain.size(); i++) {
            currentBlock = chain.get(i);
            previousBlock = chain.get(i - 1);
            if (!currentBlock.getPreviousBlockHash().equalsIgnoreCase(previousBlock.getHash())) {
                return false;
            }

            boolean proofOfWorkValid = currentBlock.getHash().startsWith(target);
            if (!proofOfWorkValid) {
                return false;
            }
        }
        return true;
    }

    public void addTransaction(Transaction transaction) {
        this.mempool.add(transaction);
    }

    public boolean replaceChain(ArrayList<Block> otherChain, Integer fromPeer) {
        if ((otherChain.size() == this.chain.size() && isOtherChainOlderThanMyChain(otherChain))
                || otherChain.size() > this.chain.size()) {
            this.chain.clear();
            this.chain.addAll(otherChain);
            updateUTXOsListOnReplaceChain(otherChain);
            updateMempool(otherChain);
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
        for (Transaction transaction : block.getTransactions()) {
            removeSTXOsFromUTXOList(transaction.getInputs());
            addUTXOsToUTXOsList(transaction.getOutputs());
        }
    }

    public void removeSTXOsFromUTXOList(ArrayList<TransactionInput> transactionInputs) {
        for (TransactionInput transactionInput : transactionInputs) {
            UTXOs.remove(transactionInput.getTransactionOutputId());
        }
    }

    public void addUTXOsToUTXOsList(ArrayList<TransactionOutput> transactionOutputs) {
        for (TransactionOutput transactionOutput : transactionOutputs) {
            UTXOs.put(transactionOutput.getId(), transactionOutput);
        }
    }

    private void updateMempool(ArrayList<Block> otherChain) {
        for (Block block : chain) {
            removeTransactionsFromMempool(block);
        }
    }

    private void removeTransactionsFromMempool(Block block) {
        for (Transaction transaction : block.getTransactions()) {
            mempool.removeIf(trx -> trx.getTransactionId().equals(transaction.getTransactionId()));
        }
    }

}

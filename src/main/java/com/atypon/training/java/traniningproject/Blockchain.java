package com.atypon.training.java.traniningproject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

@JsonIgnoreProperties
public final class Blockchain {

    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());

    // Singlton
    private static volatile Blockchain INSTANCE = new Blockchain();

    public static float minimumTransaction = 0.1f;
    public static int difficulty = 5;
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private ArrayList<Block> chain = new ArrayList<>();
    private ArrayList<Transaction> mempool = new ArrayList<>();

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
        if (isBlockValid(block)) {
            chain.add(block);
            LOGGER.info("A new block has been added to the chain");
            return;
        }
        LOGGER.info("A new block received has been found " +
                "invalid and was not added to the chain");
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

    public Block mineBlock(String previousHash) {
        Coinbase coinbase = new Coinbase(Wallet.getSharedInstance().getAddress());
        UTXOs.put(coinbase.getBlockReward().getId(), coinbase.getBlockReward());
        Block block = new Block(previousHash, coinbase, mempool);
        block.mine(difficulty);
        mempool.clear();
        chain.add(block);
        return block;
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
            updateUTXOsList(otherChain);
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

    public void updateUTXOsList(ArrayList<Block> otherChain) {
        UTXOs.clear();
        for (Block block : otherChain) {
            TransactionOutput coinbaseTx = block.getCoinbase().getBlockReward();
            for (Transaction transaction : block.getTransactions()) {
                removeSTXOsFromUTXOList(transaction.getInputs());
                addUTXOsToUTXOsList(transaction.getOutputs());
            }
            UTXOs.put(coinbaseTx.getId(), coinbaseTx);
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

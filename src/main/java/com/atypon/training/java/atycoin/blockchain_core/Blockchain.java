package com.atypon.training.java.atycoin.blockchain_core;

import com.atypon.training.java.atycoin.transactions_system.*;
import com.atypon.training.java.atycoin.utility.Utility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

@JsonIgnoreProperties
public final class Blockchain {

    private static final int DIFFICULTY = 4;
    private static final Logger LOGGER = Logger.getLogger(Blockchain.class.getName());

    private Map<String, TransactionOutput> UTXOs = new HashMap<>();

    private List<Block> chain = new LinkedList<>();

    private Queue<AtycoinTransaction> mempool = new ConcurrentLinkedQueue<>();

    private static final Blockchain INSTANCE = new Blockchain();

    private Blockchain() {
    }

    public static Blockchain getSharedInstance() {
        return INSTANCE;
    }

    public void createGenesisBlock() {
        String genesisBlockPreviousHash = Utility.repeat("0", 64);
        Coinbase coinbase = new Coinbase(Wallet.getSharedInstance().getAddress());
        ArrayList<Transaction> pickedTransaction = pickTransactionForNextBlock();
        Block block = Block.createInstance(genesisBlockPreviousHash, coinbase, pickedTransaction);
        block.mine(DIFFICULTY);
        updateUTXOsListOnAddBlock(block);
        chain.add(block);
    }

    public List<Block> getBlocks() {
        return chain;
    }

    public void addBlock(Block block) {
        if (!isBlockValid(block)) {
            LOGGER.info("A new block received/mined has been found " + "invalid and was not added to the chain");
            return;
        }
        chain.add(block);
        removeTransactionsFromMempool(block);
        updateUTXOsListOnAddBlock(block);
        LOGGER.info("A new block has been added to the chain");
    }

    private boolean isBlockValid(Block block) {
        return isHashChainValid(getPreviousBlock(), block) && isProofOfWorkValid(block) &&
                block.getIndex() == getLength() + 1;
    }

    public int getLength() {
        return chain.size();
    }

    @JsonIgnore
    public Map<String, TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public Block mineBlock(String previousHash) {
        Coinbase coinbase = new Coinbase(Wallet.getSharedInstance().getAddress());
        ArrayList<Transaction> pickedTransaction = pickTransactionForNextBlock();
        Block block = Block.createInstance(previousHash, coinbase, pickedTransaction);
        block.mine(DIFFICULTY);
        addBlock(block);
        return block;
    }

    private ArrayList<Transaction> pickTransactionForNextBlock() {
        Transaction pickedTransaction;
        ArrayList<Transaction> pickedTransactions = new ArrayList<>();
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

    public void addTransaction(AtycoinTransaction transaction) {
        this.mempool.add(transaction);
    }

    public boolean isChainValid(List<Block> chain) {
        for (int i = 1; i < chain.size(); i++) {
            if (!isHashChainValid(chain.get(i - 1), chain.get(i))) return false;
            if (!isProofOfWorkValid(chain.get(i - 1))) return false;
        }
        return true;
    }

    private boolean isHashChainValid(Block previousBlock, Block currentBlock) {
        return currentBlock.getPreviousBlockHash().equalsIgnoreCase(previousBlock.getHash());
    }

    private boolean isProofOfWorkValid(Block block) {
        String target = Utility.repeat("0", DIFFICULTY);
        return block.getHash().startsWith(target);
    }

    public void replaceChain(ArrayList<Block> otherChain) {
        if ((otherChain.size() == this.chain.size() && isOtherChainOlderThanMyChain(otherChain))
                || otherChain.size() > this.chain.size()) {
            this.chain.clear();
            this.chain.addAll(otherChain);
            updateUTXOsListOnReplaceChain(otherChain);
            updateMempool();
            Wallet.getSharedInstance().localUTXOs.clear();
            UTXOs.values().forEach(output -> Wallet.getSharedInstance().localUTXOs.put(output.getId(), output));
        }
    }

    private boolean isOtherChainOlderThanMyChain(ArrayList<Block> otherChain) {
        Long otherChainLastBlockTimeStamp = otherChain.get(otherChain.size() - 1).getTimestamp();
        Long myChainLastBlockTimestamp = getPreviousBlock().getTimestamp();
        return (otherChainLastBlockTimeStamp < myChainLastBlockTimestamp);
    }

    private void updateUTXOsListOnReplaceChain(ArrayList<Block> otherChain) {
        UTXOs.clear();
        for (Block block : otherChain) {
            TransactionOutput coinbaseTx = block.getCoinbase().getBlockReward();
            updateUTXOsListOnAddBlock(block);
            UTXOs.put(coinbaseTx.getId(), coinbaseTx);
        }
    }

    private void updateUTXOsListOnAddBlock(Block block) {
        UTXOs.put(block.getCoinbase().getBlockReward().getId(), block.getCoinbase().getBlockReward());
        Wallet.getSharedInstance().localUTXOs.put(block.getCoinbase().getBlockReward().getId(),
                block.getCoinbase().getBlockReward());
        removeSTXOsFromUTXOList(block.getTransactions());
        addUTXOsToUTXOsList(block.getTransactions());
        Wallet.getSharedInstance().updateLocalUTXOsOnNewTransactionConfirmed(block.getTransactions());
    }

    private void removeSTXOsFromUTXOList(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            for (TransactionInput transactionInput : transaction.getInputs()) {
                UTXOs.remove(transactionInput.getTransactionOutputId());
            }
        }
    }

    private void addUTXOsToUTXOsList(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                UTXOs.put(transactionOutput.getId(), transactionOutput);
            }
        }
    }

    private void updateMempool() {
        for (Block block : chain) {
            removeTransactionsFromMempool(block);
        }
    }

    private void removeTransactionsFromMempool(Block<Transaction> block) {
        mempool.removeIf(trx -> trx.getTransactionId().equals(block.
                getTransactions().get(0).getTransactionId()));
    }
}

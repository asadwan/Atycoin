package com.atypon.training.java.traniningproject;

import com.atypon.training.java.traniningproject.internodecommunication.Node;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.atypon.training.java.traniningproject.Utility.sha256;

@JsonIgnoreProperties
public final class Blockchain {

    public static float minimumTransaction = 0.1f;
    public static int difficulty = 5;
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private ArrayList<Block> chain;
    private ArrayList<Transaction> mempool = new ArrayList<>();
    private Set<Node> network = new HashSet<>();
    Wallet wallet = new Wallet();

    public Blockchain() {
        this.chain = new ArrayList<>();

        //Create Genesis Block
        String genesisBlockPreviousHash = Utility.repeat("0", 64);
        addBlock(genesisBlockPreviousHash);
    }

    public ArrayList<Block> getBlocks() {
        return chain;
    }

    public int getLength() {
        return chain.size();
    }

    public Block addBlock(String previousHash) {
        Coinbase coinbase = new Coinbase(wallet.address);
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

    public String calculateHash(Block block) {
        String hash = sha256(block.toString());
        return hash;
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

    public void addNode(Node node) {
        this.network.add(node);
    }

    /**
     public void replaceChain() {
     ArrayList<Block> longestChain = this.chain;
     int maxLength = this.chain.size();
     RestTemplate restTemplate = new RestTemplate();
     for (Node node : network) {
     System.out.println(node.getAddress().toString() + "/get_chain");
     Blockchain nodeBlockchain = restTemplate.getForObject(node.getAddress().toString() + "/get_chain",
     Blockchain.class);
     System.out.println(nodeBlockchain.getLength());
     if (nodeBlockchain.getLength() > maxLength && isChainValid(nodeBlockchain.chain)) {
     longestChain = nodeBlockchain.chain;
     }
     }
     this.chain = longestChain;
     }
     **/
}
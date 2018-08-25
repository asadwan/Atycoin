package com.atypon.training.java.traniningproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SpringBootApplication
@RestController
public class BlockchainRESTfulAPI {

    static Blockchain blockchain = new Blockchain();
    static Wallet wallet = new Wallet();

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider
        Blockchain.address = wallet.publicKey;
        Blockchain.privateKey = wallet.privateKey;
    }

    @RequestMapping(value = "/mine_block", method = GET, produces = "application/json")
    public static Block mineBlock() {
        Block previousBlock = blockchain.getPreviousBlock();
        String previousHash = blockchain.calculateHash(previousBlock);
        //Transaction transaction = new Transaction(, "Abdullah", 100.0, new ArrayList<TransactionInput>());
        //blockchain.addTransaction(transaction);
        Block block = blockchain.addBlock(previousHash);
        return block;
    }

    @RequestMapping(value = "/get_chain", method = GET, produces = "application/json")
    public static Blockchain getChain() {
        return blockchain;
    }

    @RequestMapping(value = "/blockchain_valid", method = GET, produces = "application/json")
    public static boolean isBlockChainValid() {
        return blockchain.isChainValid(blockchain.getChain());
    }

    @RequestMapping(value = "/add_transaction", method = POST, produces = "application/json")
    public static HashMap<String, String> AddTransaction(@RequestBody Transaction transaction) {
        blockchain.addTransaction(transaction);
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "transaction was added successfully");
        return response;
    }

    @RequestMapping(value = "get_balance", method = GET, produces = "application/json")
    public static Map<String, Float> get_balance() {
        float balance = blockchain.wallet.getBalance();
        Map<String, Float> response = new HashMap<>();
        response.put("balance", balance);
        return response;
    }

    @RequestMapping(value = "get_utxo_list", method = GET, produces = "application/json")
    public static Map<String, TransactionOutput> getUTXOList() {
        return Blockchain.UTXOs;
    }

    @RequestMapping(value = "send_coin", method = POST, produces = "application/json")
    public static Transaction sendCoin(@RequestBody HashMap<String, String> responseBodyJson) {
        float amount = Float.parseFloat(responseBodyJson.get("amount"));
        String recipient = responseBodyJson.get("recipient");
        Transaction transaction = blockchain.wallet.sendCoin(recipient, amount);
        boolean success = transaction.processTransaction();
        if (success) {
            blockchain.addTransaction(transaction);
            return transaction;
        } else {
            return new Transaction();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BlockchainRESTfulAPI.class, args);
    }
}
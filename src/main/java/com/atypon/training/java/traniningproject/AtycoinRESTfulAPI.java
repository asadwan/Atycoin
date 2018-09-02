package com.atypon.training.java.traniningproject;

import com.atypon.training.java.traniningproject.p2p.Node;
import com.atypon.training.java.traniningproject.p2p.NodeClient;
import com.atypon.training.java.traniningproject.p2p.NodeServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@SpringBootApplication
@RestController
public class AtycoinRESTfulAPI {

    private static Blockchain blockchain;
    private static Wallet wallet;

    static {
        //Setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    @RequestMapping(value = "/mine_block", method = GET, produces = "application/json")
    public static Block mineBlock() {
        Block previousBlock = blockchain.getPreviousBlock();
        String previousHash = previousBlock.getHash();
        Block block = blockchain.mineBlock(previousHash);
        NodeClient.getSharedInstance().broadcastNewBlock(block);
        return block;
    }

    @RequestMapping(value = "/get_chain", method = GET, produces = "application/json")
    public static Blockchain getChain() {
        return blockchain;
    }

    @RequestMapping(value = "/blockchain_valid", method = GET, produces = "application/json")
    public static boolean isBlockChainValid() {
        return blockchain.isChainValid(blockchain.getBlocks());
    }

    @RequestMapping(value = "get_balance", method = GET, produces = "application/json")
    public static Map<String, Float> getBalance() {
        float balance = wallet.getBalance();
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
        Transaction transaction = Wallet.getSharedInstance().sendCoin(recipient, amount);
        boolean success = transaction.processTransaction();
        if (success) {
            blockchain.addTransaction(transaction);
            NodeClient.getSharedInstance().broadcastNewTransaction(transaction);
            return transaction;
        } else {
            return new Transaction();
        }
    }

    @RequestMapping(value = "get_connected_peers", method = GET, produces = "application/json")
    public static Set<Integer> getConnectedPeers() {
        return Node.getSharedInstance().getPeersAddresses();
    }

    @RequestMapping(value = "get_wallet_address", method = GET, produces = "application/json")
    public static Map<String, String> getWalletAddress() {
        String address = wallet.getAddress();
        Map<String, String> response = new HashMap<>();
        response.put("address", address);
        return response;
    }

    public static void main(String[] args) {
        blockchain = Blockchain.getSharedInstance();
        blockchain.createGenesisBlock();
        NodeServer.getSharedInstance().start();
        NodeClient.getSharedInstance().start();
        wallet = Wallet.getSharedInstance();
        SpringApplication.run(AtycoinRESTfulAPI.class, args);
    }
}
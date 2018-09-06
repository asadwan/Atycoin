package com.atypon.training.java.traniningproject.api;

import com.atypon.training.java.traniningproject.blockchain_core.Block;
import com.atypon.training.java.traniningproject.blockchain_core.Blockchain;
import com.atypon.training.java.traniningproject.p2p.Node;
import com.atypon.training.java.traniningproject.p2p.NodeClient;
import com.atypon.training.java.traniningproject.transactions_system.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class AtycoinRESTfulAPI {

    private static Blockchain blockchain = Blockchain.getSharedInstance();
    private static Wallet wallet = Wallet.getSharedInstance();

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
        return blockchain.getUTXOs();
    }

//    @RequestMapping(value = "get_local_utxo_list", method = GET, produces = "application/json")
//    public static Map<String, TransactionOutput> getLocalUTXOList() {
//        return Wallet.getSharedInstance().localUTXOs;
//    }

    @RequestMapping(value = "send_coin", method = POST, produces = "application/json")
    public static Transaction sendCoin(@RequestBody HashMap<String, String> responseBodyJson) {
        float amount = Float.parseFloat(responseBodyJson.get("amount"));
        String recipient = responseBodyJson.get("recipient");
        Transaction transaction = wallet.sendCoin(recipient, amount);
        if (transaction instanceof NullTransaction) return new NullTransaction();
        blockchain.addTransaction((AtycoinTransaction) transaction);
        NodeClient.getSharedInstance().broadcastNewTransaction(transaction);
        return transaction;
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

}
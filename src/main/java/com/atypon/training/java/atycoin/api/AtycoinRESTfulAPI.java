package com.atypon.training.java.atycoin.api;

import com.atypon.training.java.atycoin.blockchain_core.Block;
import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.atypon.training.java.atycoin.p2p.Node;
import com.atypon.training.java.atycoin.p2p.NodeClient;
import com.atypon.training.java.atycoin.transactions_system.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class AtycoinRESTfulAPI {

    private static Blockchain blockchain = Blockchain.getSharedInstance();
    private static Wallet wallet = Wallet.getSharedInstance();

    /**
     * This put request starts the process of mining a new block in the node and potentially
     * getting added to chain if it was found valid.
     * sample request: http://localhost:5000/mine_block where 5000 is the port where the peer REST API
     * is exposed on
     * @return the mined block in an application/json format
     */
    @RequestMapping(value = "/mine_block", method = PUT, produces = "application/json")
    public static Block mineBlock() {
        String previousBlockHash = blockchain.getPreviousBlock().getHash();
        Block block = blockchain.mineBlock(previousBlockHash);
        NodeClient.getSharedInstance().broadcastNewBlock(block);
        return block;
    }


    /** This GET request returns the blockchain of the peer
     * sample request: http://localhost:5000/get_chain where 5000 is the port where the peer REST API
     * is exposed on
     * @return the blockchain in an application/json format
     */
    @RequestMapping(value = "/get_chain", method = GET, produces = "application/json")
    public static Blockchain getChain() {
        return blockchain;
    }


    /** This GET requests returns true if the blockchain of the peer is valid and false otherwise.
     * sample request: http://localhost:5000/blockchain_valid where 5000 is the port where the peer's REST API
     * is exposed on
     * @return true if blockchain of the peer is valid. returns false otherwise
     */
    @RequestMapping(value = "/blockchain_valid", method = GET, produces = "application/json")
    public static boolean isBlockChainValid() {
        return blockchain.isChainValid(blockchain.getBlocks());
    }

    /** This GET request returns the amount of coins (UTXOs) the peer owns.
     * sample request: http://localhost:5000/get_balance where 5000 is the port where the peer's REST API
     * is exposed on
     * @return the amount of coins the peer owns
     */
    @RequestMapping(value = "get_balance", method = GET, produces = "application/json")
    public static Map<String, Float> getBalance() {
        float balance = wallet.getBalance();
        Map<String, Float> response = new HashMap<>();
        response.put("balance", balance);
        return response;
    }


    /**
     * This POST request initiates a payment to another peer with request body containing the amount to send
     * and the wallet address of the peer this payment is wired to. the format of the request body is
     * application/json.
     * sample request body:
     * {
     * 	"amount":"100",
     * 	"recipient": "d2fbd8a94f6b189b35152a2106ea5905b606145f"
     * }
     * sample request: http://localhost:5000/send_coin where 5000
     * is the port where the peer's REST API is exposed on
     * @return the transaction details in an application/json format
     */
    @RequestMapping(value = "send_coin", method = POST, produces = "application/json")
    public static Transaction sendCoin(@RequestBody HashMap<String, String> responseBodyJson) {
        float amount = Float.parseFloat(responseBodyJson.get("amount"));
        String recipient = responseBodyJson.get("recipient");
        Transaction transaction = wallet.sendCoin(recipient, amount);
        if (transaction instanceof NullTransaction) return TransactionFactory.getNullTransaction();
        blockchain.addTransaction((AtycoinTransaction) transaction);
        NodeClient.getSharedInstance().broadcastNewTransaction(transaction);
        return transaction;
    }


    /**
     * This GET request gets the list of connected peers addresses of this peer
     * sample request: http://localhost:5000/get_connected_peers where 5000 is the port where the peer REST API
     * is exposed on
     * @return the list of of connected peers addresses of this peer
     */
    @RequestMapping(value = "get_connected_peers", method = GET, produces = "application/json")
    public static Set<Integer> getConnectedPeers() {
        return Node.getSharedInstance().getPeersAddresses();
    }

    /**
     * This GET request returns the wallet address of the peer
     * sample request: http://localhost:5000/get_wallet_address where 5000 is the
     * port where the peer's REST API is exposed on
     * @return the peer's wallet address
     */
    @RequestMapping(value = "get_wallet_address", method = GET, produces = "application/json")
    public static Map<String, String> getWalletAddress() {
        String address = wallet.getAddress();
        Map<String, String> response = new HashMap<>();
        response.put("address", address);
        return response;
    }

}
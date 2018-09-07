package com.atypon.training.java.atycoin;

import com.atypon.training.java.atycoin.blockchain_core.Blockchain;
import com.atypon.training.java.atycoin.p2p.NodeClient;
import com.atypon.training.java.atycoin.p2p.NodeServer;
import com.atypon.training.java.atycoin.transactions_system.Wallet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Security;

@SpringBootApplication
public class Atycoin {
    public static void main(String[] args) {

        //Setting Bouncey Castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        SpringApplication.run(Atycoin.class, args);
        Blockchain.getSharedInstance().createGenesisBlock();
        NodeServer.getSharedInstance().start();
        NodeClient.getSharedInstance().start();
        Wallet.getSharedInstance();
    }
}

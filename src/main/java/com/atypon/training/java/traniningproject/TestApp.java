package com.atypon.training.java.traniningproject;

public class TestApp {
    public static void main(String[] args) {
//        try {
//            URI nodeAddress = new URI("http://127.0.0.1:5000/");
//            System.out.println(nodeAddress);
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }

//        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
//        Transaction transaction1 = new Transaction("Ahmad", "Ali", 43.9);
//        Transaction transaction2 = new Transaction("Ahmad", "Ali", 43.9);
//        Transaction transaction3 = new Transaction("Ahmad", "Ali", 43.9);
//        transactions.add(transaction1);
//        transactions.add(transaction2);
//        transactions.add(transaction3);
//        System.out.println(transactions);


//        Blockchain blockchain = new Blockchain();
//        int previousProof = blockchain.getChain().blocks.get(0).getProofOfWork();
//        String previousHash = blockchain.getChain().blocks.get(0).getHash();
//        for (int i = 0; i < 10 ; i++) {
//            int proof = blockchain.findProofOfWork(previousProof);
//            Block block = blockchain.addBlock(previousProof,previousHash);
//            System.out.println(block);
//            previousProof = proof;
//            previousHash = block.getHash();
//        }

        byte[] bytes = new byte[0];
        String fuck = "fuck";
        bytes = fuck.getBytes();
        String fuckHex = Utility.bytesToHex(bytes);

        System.out.println(fuckHex);
        System.out.println(bytes);
        System.out.println(fuckHex.getBytes());
    }
}

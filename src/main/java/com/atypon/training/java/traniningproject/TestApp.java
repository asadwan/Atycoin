package com.atypon.training.java.traniningproject;

public class TestApp {
    public static void main(String[] args) {

        String fuck = "fuck";
        System.out.println(Utility.sha160(fuck));

        Wallet wallet = new Wallet();
        System.out.println(wallet.publicKey.toString());
        System.out.println(wallet.address);
    }
}

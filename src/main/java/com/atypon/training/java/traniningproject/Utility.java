package com.atypon.training.java.traniningproject;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class Utility {

    /**
     * Calculates the sha256 hash to a given input "data"
     */
    public static String sha256(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data.getBytes());
        return bytesToHex(md.digest()).toUpperCase();
    }

    /**
     * Converts from binary(Bytes) to hexadicimal
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * Returns a string that contains str repeated "times" times
     */
    public static String repeat(String str, int times) {
        return Stream.generate(() -> str).limit(times).collect(joining());
    }

    /**
     * Return the ECDSA signature resulting from signing data with private key
     */
    public static byte[] applyECDSASignuture(PrivateKey privateKey, String data) {
        Signature dsaSignature;
        byte[] signature;
        try {
            dsaSignature = Signature.getInstance("ECDSA", "BC");
            dsaSignature.initSign(privateKey);
            byte[] dataInBytes = data.getBytes();
            dsaSignature.update(dataInBytes);
            signature = dsaSignature.sign();
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return signature;
    }

    /**
     * Verify the ECDSA signature using the public key and the data signed, returns true
     * if signature valid otherwise returns false
     */
    public static boolean verifyECDSASignuture(PublicKey publicKey, byte[] signature, String data) {
        boolean verify = false;
        try {
            Signature dsaSignature = Signature.getInstance("ECDSA", "BC");
            dsaSignature.initVerify(publicKey);
            dsaSignature.update(data.getBytes());
            verify = dsaSignature.verify(signature);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return verify;
    }

    /**
     * return a string representation of the key
     */
    public static String getStringFromKey(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     *
     */
    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for (Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while (count > 1) {
            treeLayer = new ArrayList<String>();
            for (int i = 1; i < previousTreeLayer.size(); i += 2) {
                treeLayer.add(sha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
        return merkleRoot;
    }

}

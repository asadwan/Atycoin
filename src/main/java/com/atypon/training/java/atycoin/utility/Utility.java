package com.atypon.training.java.atycoin.utility;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public final class Utility {

    private Utility() {
    }

    public static String sha256(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }

    public static String sha160(String data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data.getBytes());
        return bytesToHex(md.digest());
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    public static String repeat(String str, int times) {
        return Stream.generate(() -> str).limit(times).collect(joining());
    }

    public static byte[] applyECDSASignature(PrivateKey privateKey, String data) {
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

    public static boolean verifyECDSASignature(PublicKey publicKey, byte[] signature, String data) {
        boolean verified = false;
        try {
            Signature dsaSignature = Signature.getInstance("ECDSA", "BC");
            dsaSignature.initVerify(publicKey);
            dsaSignature.update(data.getBytes());
            verified = dsaSignature.verify(signature);
        } catch (Exception e) {
            throw new RuntimeException();
        }
        return verified;
    }

    public static String getStringFromPublicKey(PublicKey publicKey) {
        try {
            KeyFactory fact = KeyFactory.getInstance("ECDSA");
            X509EncodedKeySpec spec = fact.getKeySpec(publicKey, X509EncodedKeySpec.class);
            return Base64.getEncoder().encodeToString(spec.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static PublicKey getPublicKeyFromString(String publicKeyString) {
        try {
            byte[] data = Base64.getDecoder().decode(publicKeyString.getBytes());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("ECDSA");
            return fact.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

}

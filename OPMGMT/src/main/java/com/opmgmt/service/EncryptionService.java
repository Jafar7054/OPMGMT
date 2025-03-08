package com.opmgmt.services;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import java.security.KeyPair;

@Service
public class EncryptionService {
	/**
     * Encrypts a file based on the chosen algorithm.
     */
    public void encryptFile(String algorithm, Path inputFile, Path outputFile) throws Exception {
        if ("AES".equalsIgnoreCase(algorithm)) {
            encryptAES(inputFile, outputFile);
        } else if ("DES".equalsIgnoreCase(algorithm)) {
            encryptDES(inputFile, outputFile);
        } else if ("RSA".equalsIgnoreCase(algorithm)) {
            encryptRSA(inputFile, outputFile);
        } else {
            throw new IllegalArgumentException("Unsupported encryption algorithm: " + algorithm);
        }
    }

    /**
     * Encrypts file using AES
     */
    private void encryptAES(Path inputFile, Path outputFile) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] fileData = Files.readAllBytes(inputFile);
        byte[] encryptedData = cipher.doFinal(fileData);

        Files.write(outputFile, encryptedData, StandardOpenOption.CREATE);

        // Save key for decryption
        Files.write(Path.of(outputFile.toString() + ".key"), secretKey.getEncoded(), StandardOpenOption.CREATE);
    }

    /**
     * Encrypts file using DES
     */
    private void encryptDES(Path inputFile, Path outputFile) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("DES");
        keyGen.init(56);
        SecretKey secretKey = keyGen.generateKey();

        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] fileData = Files.readAllBytes(inputFile);
        byte[] encryptedData = cipher.doFinal(fileData);

        Files.write(outputFile, encryptedData, StandardOpenOption.CREATE);

        // Save key for decryption
        Files.write(Path.of(outputFile.toString() + ".key"), secretKey.getEncoded(), StandardOpenOption.CREATE);
    }

    /**
     * Encrypts file using RSA
     */
    private void encryptRSA(Path inputFile, Path outputFile) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

        byte[] fileData = Files.readAllBytes(inputFile);
        byte[] encryptedData = cipher.doFinal(fileData);

        Files.write(outputFile, encryptedData, StandardOpenOption.CREATE);

        // Save keys for decryption
        Files.write(Path.of(outputFile.toString() + ".pubkey"), keyPair.getPublic().getEncoded(), StandardOpenOption.CREATE);
        Files.write(Path.of(outputFile.toString() + ".privkey"), keyPair.getPrivate().getEncoded(), StandardOpenOption.CREATE);
    }
}

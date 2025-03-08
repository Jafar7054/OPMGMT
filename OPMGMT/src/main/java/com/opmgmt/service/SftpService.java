package com.opmgmt.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Properties;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.opmgmt.entity.SftpInfo;

@Service
public class SftpService {

   
    
    private ChannelSftp setupJsch(SftpInfo info) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(info.getSftpUsername(), info.getSftpHost(), info.getSftpPort());
        session.setPassword(info.getSftpPassword());

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        return (ChannelSftp) channel;
    }
    /**
    /**
 * Upload all files from local directory to SFTP that match the given fileNameFormat.
 * @throws JSchException 
 */
public void uploadFiles(String fileNameFormat, String encryptionType, SftpInfo info, String secretKey) throws JSchException {
    ChannelSftp channelSftp = null;
    try {
        channelSftp = setupJsch(info);
        channelSftp.cd(info.getRemoteDir());

        File localFolder = new File(info.getLocalDir());
        File[] matchingFiles = localFolder.listFiles((dir, name) -> name.startsWith(fileNameFormat));

        if (matchingFiles == null || matchingFiles.length == 0) {
            System.out.println("No files found matching format: " + fileNameFormat);
            return;
        }

        for (File originalFile : matchingFiles) {
            if(encryptionType!=null)
            {
            	File encryptedFile = new File(info.getLocalDir() + "encrypted_" + originalFile.getName());

                // Encrypt the file
                encryptFile(originalFile, encryptedFile, encryptionType, secretKey);

                // Upload encrypted file
                channelSftp.put(new FileInputStream(encryptedFile), encryptedFile.getName());
                System.out.println("File uploaded successfully (encrypted): " + encryptedFile.getName());
            }
            else 
            {
            	channelSftp.put(new FileInputStream(originalFile), originalFile.getName());
            	 System.out.println("File uploaded successfully(Un-encrypted) : " + originalFile.getName());
            }
        }
    } catch (Exception e) {
        System.err.println("Error uploading files: " + e.getMessage());
    } finally {
        if (channelSftp != null) {
            channelSftp.exit();
            channelSftp.getSession().disconnect();
        }
    }
}

/**
 * Download all files from SFTP that match the given fileNameFormat and decrypt them.
 * @throws JSchException 
 */
public void downloadFiles(String fileNameFormat, SftpInfo info, String encryptionType, String secretKey) throws JSchException {
    ChannelSftp channelSftp = null;
    try {
        channelSftp = setupJsch(info);
        channelSftp.cd(info.getRemoteDir());

        Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(info.getRemoteDir());
        for (ChannelSftp.LsEntry entry : fileList) {
            String fileName = entry.getFilename();
            if (fileName.startsWith("encrypted_" + fileNameFormat)) {
                File encryptedFile = new File(info.getLocalDir() + fileName);
                File decryptedFile = new File(info.getLocalDir() + "decrypted_" + fileName.replace("encrypted_", ""));

                // Download encrypted file
                channelSftp.get(fileName, new FileOutputStream(encryptedFile));
                System.out.println("File downloaded successfully (encrypted): " + encryptedFile.getName());

                // Decrypt the file
                decryptFile(encryptedFile, decryptedFile, encryptionType, secretKey);
                System.out.println("File decrypted successfully: " + decryptedFile.getName());
            }
            else if(fileName.startsWith(fileNameFormat))
            {
            	File normalFile=new File(info.getLocalDir()+fileName);
            	channelSftp.get(fileName,new FileOutputStream(normalFile));
            	 System.out.println("File downloaded successfully: " + normalFile.getName());
            }
            else
            {
            	File normalFile=new File(info.getLocalDir()+fileName);
            	channelSftp.get(fileName,new FileOutputStream(normalFile));
            	 System.out.println("File downloaded successfully: " + normalFile.getName());
            }
        }
    } catch (Exception e) {
        System.err.println("Error downloading files: " + e.getMessage());
    } finally {
        if (channelSftp != null) {
            channelSftp.exit();
            channelSftp.getSession().disconnect();
        }
    }

    }
    
    
    /**
     * Encrypts a file using the specified encryption type.
     */
    private void encryptFile(File inputFile, File outputFile, String encryptionType, String secretKey) throws Exception {
        Cipher cipher = getCipher(encryptionType, Cipher.ENCRYPT_MODE, secretKey);
        processFile(cipher, inputFile, outputFile);
    }

    /**
     * Decrypts a file using the specified encryption type.
     */
    private void decryptFile(File inputFile, File outputFile, String encryptionType, String secretKey) throws Exception {
        Cipher cipher = getCipher(encryptionType, Cipher.DECRYPT_MODE, secretKey);
        processFile(cipher, inputFile, outputFile);
    }

    /**
     * Get Cipher based on encryption type.
     */
    private Cipher getCipher(String encryptionType, int mode, String secretKey) throws Exception {
        Key key;
        Cipher cipher;
        switch (encryptionType) {
            case "AES":
                key = new SecretKeySpec(secretKey.getBytes(), "AES");
                cipher = Cipher.getInstance("AES");
                cipher.init(mode, key);
                return cipher;
            case "DES":
                key = new SecretKeySpec(secretKey.getBytes(), "DES");
                cipher = Cipher.getInstance("DES");
                cipher.init(mode, key);
                return cipher;
            case "RSA":
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
                keyPairGen.initialize(2048);
                KeyPair pair = keyPairGen.generateKeyPair();
                cipher = Cipher.getInstance("RSA");
                if (mode == Cipher.ENCRYPT_MODE) {
                    cipher.init(mode, pair.getPublic());
                } else {
                    cipher.init(mode, pair.getPrivate());
                }
                return cipher;
            default:
                throw new IllegalArgumentException("Unsupported encryption type: " + encryptionType);
        }
    }

    /**
     * Process file with encryption or decryption.
     */
    private void processFile(Cipher cipher, File inputFile, File outputFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(inputFile);
             FileOutputStream fos = new FileOutputStream(outputFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }
}

    

package com.keystore.controller;
import jakarta.servlet.http.HttpServletResponse;

import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.io.FileOutputStream;

import java.util.Date;
@Controller
public class CCController {

    @GetMapping("/createKeyStore")
    @ResponseBody
    public String createKeyStore(HttpServletResponse response) {
        try {
            CertificatePolicy policy = CertificatePolicy.QCP_PUBLIC;
            String description = policy.getDescription();
            String oid = policy.getOid();

            System.out.println("Certificate Policy: " + description);
            System.out.println("OID: " + oid);

            KeyPairGenerator rootKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            rootKeyPairGenerator.initialize(2048);
            KeyPair rootKeyPair = rootKeyPairGenerator.generateKeyPair();
            X509Certificate rootCertificate = generateCertificate(rootKeyPair, null, null, "Root Certificate");

            ///////////////////
            KeyPairGenerator intermediteKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            intermediteKeyPairGenerator.initialize(2048);
            KeyPair intermefiteKeyPair = intermediteKeyPairGenerator.generateKeyPair();
            X509Certificate intermediateCertificate = generateCertificate(intermefiteKeyPair, rootKeyPair.getPrivate(), rootCertificate, "Intermediate Certificate");
            ///////////////////////

            KeyPairGenerator endpointKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            endpointKeyPairGenerator.initialize(2048);
            KeyPair endpointKeyPair = endpointKeyPairGenerator.generateKeyPair();
            X509Certificate endpointCertificate = generateCertificate(endpointKeyPair, intermefiteKeyPair.getPrivate(), intermediateCertificate, "Endpoint Certificate");
///////////////////////

            KeyPairGenerator ownerKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
            ownerKeyPairGenerator.initialize(2048);
            KeyPair ownerKeyPair = ownerKeyPairGenerator.generateKeyPair();
            X509Certificate ownerCertificate = generateCertificate(ownerKeyPair, endpointKeyPair.getPrivate(), endpointCertificate, "Owner Certificate");
///////////////////////

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(null, null);


            keyStore.setKeyEntry("mykey", ownerKeyPair.getPrivate(), "password".toCharArray(), new X509Certificate[]{ownerCertificate, endpointCertificate,intermediateCertificate, rootCertificate});



            File tempFile = File.createTempFile("keystore", ".p12");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                keyStore.store(fos, "password".toCharArray());
            }


            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=keystore.p12");
            response.setContentLength((int) tempFile.length());


            try (
                    FileInputStream fis = new FileInputStream(tempFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            tempFile.delete();

            return "Key Store created and downloaded successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to create Key Store";
        }
    }



    private X509Certificate generateCertificate(KeyPair keyPair, PrivateKey signerPrivateKey, X509Certificate signerCertificate, String commonName) throws Exception {
        X500Principal owner = new X500Principal("CN=" + commonName);
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 365 * 24 * 60 * 60 * 1000L);
        BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());

        X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
        certGenerator.setSerialNumber(serialNumber);
        certGenerator.setIssuerDN(signerCertificate != null ? signerCertificate.getSubjectX500Principal() : owner);
        certGenerator.setNotBefore(startDate);
        certGenerator.setNotAfter(endDate);
        certGenerator.setSubjectDN(owner);
        certGenerator.setPublicKey(keyPair.getPublic());
        certGenerator.setSignatureAlgorithm("SHA256withRSA");

        if (signerPrivateKey != null) {
            X509Certificate cert = certGenerator.generate(signerPrivateKey);
            return cert;
        } else {

            return certGenerator.generate(keyPair.getPrivate());
        }
    }


}


























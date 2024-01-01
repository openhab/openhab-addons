/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.androidtv.internal.utils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AndroidTVPKI} class controls all aspects of the PKI/keyStore
 *
 * Some methods adapted from Bosch binding
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class AndroidTVPKI {

    private final Logger logger = LoggerFactory.getLogger(AndroidTVPKI.class);

    private final int keySize = 128;
    private final int dataLength = 128;

    private String privKey = "";
    private String cert = "";
    private String caCert = "";
    private String keystoreFileName = "";
    private String keystoreAlgorithm = "RSA";
    private int keyLength = 2048;
    private String alias = "openhab";
    private String distName = "CN=openHAB, O=openHAB, L=None, ST=None, C=None";
    private String cipher = "AES/GCM/NoPadding";
    private String keyAlgorithm = "";

    private @Nullable Cipher encryptionCipher;

    public AndroidTVPKI() {
        try {
            encryptionCipher = Cipher.getInstance(cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.debug("Could not get cipher instance", e);
        }
    }

    public byte[] generateEncryptionKey() {
        Key key;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            key = keyGenerator.generateKey();
            byte[] newKey = key.getEncoded();
            this.keyAlgorithm = key.getAlgorithm();
            return newKey;
        } catch (NoSuchAlgorithmException e) {
            logger.debug("Could not generate encryption keys", e);
        }
        return new byte[0];
    }

    private Key convertByteToKey(byte[] keyString) {
        Key key = new SecretKeySpec(keyString, keyAlgorithm);
        return key;
    }

    public String encrypt(String data, Key key) throws Exception {
        return encrypt(data, key, this.cipher);
    }

    public String encrypt(String data, Key key, String cipher) throws Exception {
        byte[] dataInBytes = data.getBytes();
        Cipher encryptionCipher = this.encryptionCipher;
        if (encryptionCipher != null) {
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = encryptionCipher.doFinal(dataInBytes);
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } else {
            return "";
        }
    }

    public String decrypt(String encryptedData, Key key) throws Exception {
        return decrypt(encryptedData, key, this.cipher);
    }

    public String decrypt(String encryptedData, Key key, String cipher) throws Exception {
        byte[] dataInBytes = Base64.getDecoder().decode(encryptedData);
        Cipher decryptionCipher = Cipher.getInstance(cipher);
        Cipher encryptionCipher = this.encryptionCipher;
        if (encryptionCipher != null) {
            GCMParameterSpec spec = new GCMParameterSpec(dataLength, encryptionCipher.getIV());
            decryptionCipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] decryptedBytes = decryptionCipher.doFinal(dataInBytes);
            return new String(decryptedBytes);
        } else {
            return "";
        }
    }

    public void setPrivKey(String privKey, byte[] keyString) throws Exception {
        Key key = convertByteToKey(keyString);
        this.privKey = encrypt(privKey, key);
    }

    public String getPrivKey(byte[] keyString) throws Exception {
        Key key = convertByteToKey(keyString);
        return decrypt(this.privKey, key);
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public void setCert(Certificate cert) throws CertificateEncodingException {
        this.cert = new String(Base64.getEncoder().encode(cert.getEncoded()));
    }

    public Certificate getCert() throws CertificateException {
        Certificate cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(this.cert.getBytes())));
        return cert;
    }

    public void setCaCert(String caCert) {
        this.caCert = caCert;
    }

    public void setCaCert(Certificate caCert) throws CertificateEncodingException {
        this.caCert = new String(Base64.getEncoder().encode(caCert.getEncoded()));
    }

    public Certificate getCaCert() throws CertificateException {
        Certificate caCert = CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(this.caCert.getBytes())));
        return caCert;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlgorithm(String keystoreAlgorithm) {
        this.keystoreAlgorithm = keystoreAlgorithm;
    }

    public String getAlgorithm() {
        return this.keystoreAlgorithm;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }

    public int getKeyLength() {
        return this.keyLength;
    }

    public void setDistName(String distName) {
        this.distName = distName;
    }

    public String getDistName() {
        return this.distName;
    }

    public void setKeystoreFileName(String keystoreFileName) {
        this.keystoreFileName = keystoreFileName;
    }

    public String getKeystoreFileName() {
        return this.keystoreFileName;
    }

    public void setKeys(String privKey, byte[] keyString, String cert) throws GeneralSecurityException, Exception {
        setPrivKey(privKey, keyString);
        setCert(cert);
    }

    public void setKeyStore(String keystoreFileName) {
        this.keystoreFileName = keystoreFileName;
    }

    public void loadFromKeyStore(String keystoreFileName, String keystorePassword, byte[] keyString)
            throws GeneralSecurityException, IOException, Exception {
        this.keystoreFileName = keystoreFileName;
        loadFromKeyStore(keystorePassword, keyString);
    }

    public void loadFromKeyStore(String keystorePassword, byte[] keyString)
            throws GeneralSecurityException, IOException, Exception {
        Key key = convertByteToKey(keyString);
        KeyStore keystore = KeyStore.getInstance("JKS");
        FileInputStream keystoreInputStream = new FileInputStream(this.keystoreFileName);
        keystore.load(keystoreInputStream, keystorePassword.toCharArray());
        byte[] byteKey = keystore.getKey(this.alias, keystorePassword.toCharArray()).getEncoded();
        this.privKey = encrypt(new String(Base64.getEncoder().encode(byteKey)), key);
        setCert(keystore.getCertificate(this.alias));
        Certificate caCert = keystore.getCertificate("trustedCa");
        if (caCert != null) {
            setCaCert(caCert);
        }
    }

    public KeyStore getKeyStore(String keystorePassword, byte[] keyString)
            throws GeneralSecurityException, IOException, Exception {
        KeyStore keystore = KeyStore.getInstance("JKS");
        keystore.load(null, null);
        byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(getPrivKey(keyString));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
        KeyFactory kf = KeyFactory.getInstance(this.keystoreAlgorithm);
        keystore.setKeyEntry(this.alias, kf.generatePrivate(keySpec), keystorePassword.toCharArray(),
                new java.security.cert.Certificate[] { getCert() });
        if (!caCert.isEmpty()) {
            keystore.setCertificateEntry("trustedCa", getCaCert());
        }
        return keystore;
    }

    public void saveKeyStore(String keystorePassword, byte[] keyString)
            throws GeneralSecurityException, IOException, Exception {
        saveKeyStore(this.keystoreFileName, keystorePassword, keyString);
    }

    public void saveKeyStore(String keystoreFileName, String keystorePassword, byte[] keyString)
            throws GeneralSecurityException, IOException, Exception {
        FileOutputStream keystoreStream = new FileOutputStream(keystoreFileName);
        KeyStore keystore = getKeyStore(keystorePassword, keyString);
        keystore.store(keystoreStream, keystorePassword.toCharArray());
    }

    private X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String distName)
            throws GeneralSecurityException, OperatorCreationException {
        final Instant now = Instant.now();
        final Date notBefore = Date.from(now);
        final Date notAfter = Date.from(now.plus(Duration.ofDays(365 * 10)));
        X500Name name = new X500Name(distName);
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(name,
                BigInteger.valueOf(now.toEpochMilli()), notBefore, notAfter, name, keyPair.getPublic());
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider())
                .getCertificate(certificateBuilder.build(contentSigner));
    }

    public void generateNewKeyPair(byte[] keyString)
            throws GeneralSecurityException, OperatorCreationException, IOException, Exception {
        Key key = convertByteToKey(keyString);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(this.keystoreAlgorithm);
        kpg.initialize(this.keyLength);
        KeyPair kp = kpg.generateKeyPair();
        Security.addProvider(new BouncyCastleProvider());
        Signature signer = Signature.getInstance("SHA256withRSA", "BC");
        signer.initSign(kp.getPrivate());
        signer.update("openhab".getBytes(StandardCharsets.UTF_8));
        signer.sign();
        X509Certificate signedcert = generateSelfSignedCertificate(kp, this.distName);
        this.privKey = encrypt(new String(Base64.getEncoder().encode(kp.getPrivate().getEncoded())), key);
        setCert(signedcert);
    }
}

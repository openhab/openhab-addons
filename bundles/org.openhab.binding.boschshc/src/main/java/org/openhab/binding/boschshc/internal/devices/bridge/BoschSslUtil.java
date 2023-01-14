/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;
import org.openhab.core.OpenHAB;
import org.openhab.core.id.InstanceUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSL context utility.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class BoschSslUtil {

    private static final String OSS_OPENHAB_BINDING = "oss_openhab_binding";
    private static final String KEYSTORE_PASSWORD = "openhab";

    private final Logger logger = LoggerFactory.getLogger(BoschSslUtil.class);

    private final String boschShcServerID;
    private final String keystorePath;

    /**
     * Returns unique ID for this Bosch SmartHomeController client.
     * 
     * @return unique string containing the openhab UUID.
     */
    public static String getBoschShcClientId() {
        return OSS_OPENHAB_BINDING + "_" + InstanceUUID.get();
    }

    /**
     * Returns ID for passed Bosch SmartHomeController server.
     * 
     * @param shcServerID the ip address of the SHC server
     * @return unique string containing the server id
     */
    public static String getBoschShcServerId(String shcServerID) {
        return OSS_OPENHAB_BINDING + "_" + shcServerID;
    }

    /**
     * Constructor
     * 
     * @param boschShcServerID the ip address of the SHC server
     */
    public BoschSslUtil(String boschShcServerID) {
        this.boschShcServerID = boschShcServerID;
        this.keystorePath = getKeystorePath();
    }

    /// Returns unique ID for Bosch SmartHomeController server.
    public String getBoschShcServerId() {
        return BoschSslUtil.getBoschShcServerId(boschShcServerID);
    }

    /// Returns the unique keystore for each Bosch Smart Home Controller server.
    public String getKeystorePath() {
        return Paths.get(OpenHAB.getUserDataFolder(), "etc", getBoschShcServerId() + ".jks").toString();
    }

    public SslContextFactory getSslContextFactory() throws PairingFailedException {
        // Instantiate and configure the SslContextFactory
        SslContextFactory sslContextFactory = new SslContextFactory.Client.Client(true); // Accept all certificates

        // during pairing the cert from this keystore is accessed by HTTP client via name
        sslContextFactory.setKeyStore(getKeyStoreAndCreateIfNecessary());

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(KEYSTORE_PASSWORD);

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }

    public KeyStore getKeyStoreAndCreateIfNecessary() throws PairingFailedException {
        try {
            File file = new File(keystorePath);
            if (!file.exists()) {
                // create new keystore
                logger.info("Creating new keystore {} because it doesn't exist.", keystorePath);
                return createKeyStore(keystorePath);
            } else {
                // load keystore as a first check
                KeyStore keyStore = KeyStore.getInstance("JKS");
                try (FileInputStream keystoreStream = new FileInputStream(file)) {
                    keyStore.load(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
                }
                logger.debug("Using existing keystore {}", keystorePath);
                return keyStore;
            }
        } catch (OperatorCreationException | GeneralSecurityException | IOException e) {
            logger.debug("Exception during keystore creation {}", e.getMessage());
            throw new PairingFailedException("Can not create or load keystore file: " + keystorePath
                    + ". Check path, write access and JKS content.", e);
        }
    }

    private X509Certificate generateClientCertificate(KeyPair keyPair)
            throws GeneralSecurityException, OperatorCreationException {
        final String dirName = "CN=" + getBoschShcClientId() + ", O=openHAB, L=None, ST=None, C=None";
        logger.debug("Creating a new self signed certificate: {}", dirName);
        final Instant now = Instant.now();
        final Date notBefore = Date.from(now);
        final Date notAfter = Date.from(now.plus(Duration.ofDays(365 * 10)));
        X500Name name = new X500Name(dirName);

        // create the certificate
        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(name, // Issuer
                BigInteger.valueOf(now.toEpochMilli()), notBefore, notAfter, name, // Subject
                keyPair.getPublic() // Public key to be associated with the certificate
        );
        // and sign it
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider())
                .getCertificate(certificateBuilder.build(contentSigner));
    }

    private KeyStore createKeyStore(String keystore)
            throws IOException, OperatorCreationException, GeneralSecurityException {
        // create a new keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        // create new key pair for BoschSHC binding
        logger.debug("Creating new keypair");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();

        Security.addProvider(new BouncyCastleProvider());
        Signature signer = Signature.getInstance("SHA256withRSA", "BC");
        signer.initSign(keyPair.getPrivate());
        signer.update("Hello openHAB".getBytes(StandardCharsets.UTF_8));
        signer.sign();

        X509Certificate cert = generateClientCertificate(keyPair);

        logger.debug("Adding keyEntry '{}' with self signed certificate to keystore", getBoschShcServerId());
        keyStore.setKeyEntry(getBoschShcServerId(), keyPair.getPrivate(), KEYSTORE_PASSWORD.toCharArray(),
                new Certificate[] { cert });

        // add Bosch Certs
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        logger.debug("Adding Issuing CA to keystore");
        try (BufferedInputStream streamIssuingCA = new BufferedInputStream(
                this.getClass().getResourceAsStream("SmartHomeControllerIssuingCA.pem"))) {
            Certificate certIssuingCA = cf.generateCertificate(streamIssuingCA);
            keyStore.setCertificateEntry("Smart Home Controller Issuing CA", certIssuingCA);
        }

        logger.debug("Adding root CA to keystore");
        try (BufferedInputStream streamRootCa = new BufferedInputStream(
                this.getClass().getResourceAsStream("SmartHomeControllerProductiveRootCA.pem"))) {
            Certificate certRooCA = cf.generateCertificate(streamRootCa);
            keyStore.setCertificateEntry("Smart Home Controller Productive Root CA", certRooCA);
        }

        logger.debug("Storing keystore to file {}", keystore);
        try (FileOutputStream keystoreStream = new FileOutputStream(keystore)) {
            keyStore.store(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
        }

        return keyStore;
    }
}

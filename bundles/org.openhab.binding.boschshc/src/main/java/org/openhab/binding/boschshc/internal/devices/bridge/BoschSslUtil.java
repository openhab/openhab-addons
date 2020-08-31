/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.id.InstanceUUID;
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

    private final Logger logger = LoggerFactory.getLogger(BoschSslUtil.class);

    private String keystorePath;
    private String keystorePassword;

    public static String getBoschSHCId() {
        return OSS_OPENHAB_BINDING + "_" + InstanceUUID.get();
    }

    public BoschSslUtil(String keystorePassword) {
        this.keystorePath = Paths.get(ConfigConstants.getUserDataFolder(), "etc", getBoschSHCId() + ".jks").toString();
        this.keystorePassword = keystorePassword;
    }

    public SslContextFactory getSslContextFactory() {
        // Instantiate and configure the SslContextFactory
        SslContextFactory sslContextFactory = new SslContextFactory(true); // Accept all certificates

        // during pairing the cert from this keystore is accessed by HTTP client via name
        sslContextFactory.setKeyStore(getKeyStoreAndCreateIfNecessary());

        // Keystore for managing the keys that have been used to pair with the SHC
        // https://www.eclipse.org/jetty/javadoc/9.4.12.v20180830/org/eclipse/jetty/util/ssl/SslContextFactory.html
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword(keystorePassword);

        // Bosch is using a self signed certificate
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        return sslContextFactory;
    }

    public KeyStore getKeyStoreAndCreateIfNecessary() {
        try {
            File file = new File(keystorePath);
            if (!file.exists()) {
                // create new keystore
                logger.info("Creating new keystore {} because it doesn't exist.", keystorePath);
                return createKeyStore(keystorePath, keystorePassword);
            } else {
                // load keystore as a first check
                KeyStore keyStore = KeyStore.getInstance("JKS");
                // TODO if SHC system password is changed the keystore can't be loaded and an IOException "... password
                // was incorrect" is thrown
                // Either use a different secret instead of the system password (e.g. OpenHAB UUID?)
                // or recreate a new keystore with the different system password again (needs pairing)
                keyStore.load(new FileInputStream(file), keystorePassword.toCharArray());
                logger.debug("Using existing keystore {}", keystorePath);
                return keyStore;
            }
        } catch (OperatorCreationException | GeneralSecurityException | IOException e) {
            logger.warn("Can not create or load keystore {}. Check path, write access and JKS content.", keystorePath);
            logger.debug("Exception during kesstore creation: {}", e);
            throw new IllegalStateException(e);
        }
    }

    private X509Certificate generateClientCertificate(KeyPair keyPair)
            throws GeneralSecurityException, IOException, OperatorCreationException {
        final String dirName = "CN=" + getBoschSHCId() + ", O=OpenHAB, L=None, ST=None, C=None";
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

    private KeyStore createKeyStore(String keystore, String keystorePassword)
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
        signer.update("Hello OpenHAB".getBytes());
        signer.sign();

        X509Certificate cert = generateClientCertificate(keyPair);

        logger.debug("Adding keypair and self signed certificate to keystore");
        keyStore.setKeyEntry(getBoschSHCId(), keyPair.getPrivate(), keystorePassword.toCharArray(),
                new Certificate[] { cert });

        // add Bosch Certs
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        logger.debug("Adding Issuing CA to keystore");
        BufferedInputStream streamIssuingCA = new BufferedInputStream(
                BoschSslUtil.class.getClassLoader().getResourceAsStream("Smart Home Controller Issuing CA.pem"));
        Certificate certIssueingCA = cf.generateCertificate(streamIssuingCA);
        keyStore.setCertificateEntry("Smart Home Controller Issuing CA", certIssueingCA);

        logger.debug("Adding root CA to keystore");
        BufferedInputStream streamRootCa = new BufferedInputStream(BoschSslUtil.class.getClassLoader()
                .getResourceAsStream("Smart Home Controller Productive Root CA.pem"));
        Certificate certRooCA = cf.generateCertificate(streamRootCa);
        keyStore.setCertificateEntry("Smart Home Controller Productive Root CA", certRooCA);

        logger.debug("Storing keystore to file {}", keystore);
        keyStore.store(new FileOutputStream(new File(keystore)), keystorePassword.toCharArray());

        return keyStore;
    }
}

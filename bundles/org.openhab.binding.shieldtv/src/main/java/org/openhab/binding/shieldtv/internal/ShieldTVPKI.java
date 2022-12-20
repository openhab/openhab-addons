/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.shieldtv.internal;

import java.io.ByteArrayInputStream;
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
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShieldTVPKI} class controls all aspects of the PKI/keyStore
 *
 * Some methods adapted from Bosch binding
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class ShieldTVPKI {

    private final Logger logger = LoggerFactory.getLogger(ShieldTVPKI.class);

    private String privKey = "";
    private String cert = "";
    private String keystoreFileName = "";
    private String keystorePassword = "";

    public void setKeystore(String keystoreFileName, String keystorePassword) {
        this.keystoreFileName = keystoreFileName;
        this.keystorePassword = keystorePassword;
    }

    public void setKeys(String privKey, String cert) {
        this.privKey = privKey;
        this.cert = cert;
    }

    public void saveKeys() {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            logger.trace("PrivKey to store: {}", this.privKey);
            byte[] pkcs8EncodedBytes = Base64.getDecoder().decode(this.privKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8EncodedBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Key key = kf.generatePrivate(keySpec);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            logger.trace("Cert to store: {}", this.cert);
            Certificate crt = cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(this.cert)));

            keystore.setKeyEntry("nvidia", key, keystorePassword.toCharArray(),
                    new java.security.cert.Certificate[] { crt });
            logger.debug("Writing to {}", keystoreFileName);
            FileOutputStream keystoreStream = new FileOutputStream(keystoreFileName);
            keystore.store(keystoreStream, keystorePassword.toCharArray());
        } catch (GeneralSecurityException | IOException e) {
            logger.debug("createKeystore Exception", e);
            return;
        }
    }

    private X509Certificate generateClientCertificate(KeyPair keyPair)
            throws GeneralSecurityException, OperatorCreationException {
        final String dirName = "CN=openHAB, O=openHAB, L=None, ST=None, C=None";
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

    public void createKeystore() {
        try {
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(null, null);
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keystorePassword.toCharArray());
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();
            Security.addProvider(new BouncyCastleProvider());
            Signature signer = Signature.getInstance("SHA256withRSA", "BC");
            signer.initSign(kp.getPrivate());
            signer.update("Hello openHAB".getBytes(StandardCharsets.UTF_8));
            signer.sign();
            X509Certificate signedcert = generateClientCertificate(kp);
            keystore.setKeyEntry("nvidia", kp.getPrivate(), keystorePassword.toCharArray(),
                    new java.security.cert.Certificate[] { signedcert });
            logger.debug("Writing to {}", keystoreFileName);
            FileOutputStream keystoreStream = new FileOutputStream(keystoreFileName);
            keystore.store(keystoreStream, keystorePassword.toCharArray());
        } catch (GeneralSecurityException | OperatorCreationException | IOException e) {
            logger.debug("createKeystore Exception", e);
            return;
        }
    }

    public String getKeystoreFileName() {
        return keystoreFileName;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }
}

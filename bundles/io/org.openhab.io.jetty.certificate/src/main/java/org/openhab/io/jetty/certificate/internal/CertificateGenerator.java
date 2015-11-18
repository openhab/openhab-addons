/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.jetty.certificate.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CertificateGenerator implements BundleActivator {

    private static final String JETTY_KEYSTORE_PATH_PROPERTY = "jetty.keystore.path";
    private static final String KEYSTORE_PASSWORD = "openhab";
    private static final String KEYSTORE_ENTRY_ALIAS = "mykey";
    private static final String KEYSTORE_JKS_TYPE = "JKS";
    private static final String CURVE_NAME = "prime256v1";
    private static final String KEY_PAIR_GENERATOR_TYPE = "EC";
    private static final String KEY_FACTORY_TYPE = "EC";
    private static final String CONTENT_SIGNER_ALGORITHM = "SHA256withECDSA";
    private static final String CERTIFICATE_X509_TYPE = "X.509";
    private static final String X500_NAME = "CN=openhab.org, OU=None, O=None, L=None, C=None";

    private Logger logger;

    private File keystoreFile;

    @Override
    public void start(BundleContext context) throws Exception {
        logger = LoggerFactory.getLogger(CertificateGenerator.class);
        try {
            KeyStore keystore = ensureKeystore();

            if (!isCertificateInKeystore(keystore)) {
                logger.debug("{} alias not found. Generating a new certificate.", KEYSTORE_ENTRY_ALIAS);
                generateCertificate(keystore);
            } else {
                logger.debug("{} alias found. Do nothing.", KEYSTORE_ENTRY_ALIAS);
            }
        } catch (CertificateException | KeyStoreException e) {
            logger.error("Failed to generate a new SSL Certificate.", e);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // Nothing to do.
    }

    /**
     * Ensure that the keystore exist and is readable. If not, create a new one.
     *
     * @throws KeyStoreException if the creation of the keystore fails or if it is not readable.
     */
    private KeyStore ensureKeystore() throws KeyStoreException {
        String keystorePath = System.getProperty(JETTY_KEYSTORE_PATH_PROPERTY);
        keystoreFile = new File(keystorePath);
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_JKS_TYPE);
        if (!keystoreFile.exists()) {
            try {
                logger.debug("No keystore found. Creation of {}", keystoreFile.getAbsolutePath());
                boolean newFileCreated = keystoreFile.createNewFile();
                if (newFileCreated) {
                    keyStore.load(null, null);
                } else {
                    throw new IOException("Keystore file creation failed.");
                }
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                throw new KeyStoreException("Failed to create the keystore " + keystoreFile.getAbsolutePath(), e);
            }
        } else {
            try (InputStream keystoreStream = new FileInputStream(keystoreFile);) {
                logger.debug("Keystore found. Trying to load {}", keystoreFile.getAbsolutePath());
                keyStore.load(keystoreStream, KEYSTORE_PASSWORD.toCharArray());
            } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
                throw new KeyStoreException("Failed to load the keystore " + keystoreFile.getAbsolutePath(), e);
            }
        }

        return keyStore;
    }

    /**
     * Check if the keystore contains a certificate with the KEYSTORE_ENTRY_ALIAS.
     *
     * @param keystore
     * @return true if the alias is already present in the keystore, else false.
     * @throws KeyStoreException If the keystore cannot be read.
     */
    private boolean isCertificateInKeystore(KeyStore keystore) throws KeyStoreException {
        return keystore.getCertificate(KEYSTORE_ENTRY_ALIAS) != null;
    }

    /**
     * Generate a new certificate and store it in the given keystore.
     *
     * @param keystore
     * @throws CertificateException if the certificate generation has failed.
     * @throws KeyStoreException If save of the keystore has failed.
     */
    private void generateCertificate(KeyStore keystore) throws CertificateException, KeyStoreException {
        try {
            long startTime = System.currentTimeMillis();
            org.bouncycastle.jce.spec.ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(CURVE_NAME);
            ECField field = new ECFieldFp(ecSpec.getCurve().getField().getCharacteristic());
            EllipticCurve curve = new EllipticCurve(field, ecSpec.getCurve().getA().toBigInteger(),
                    ecSpec.getCurve().getB().toBigInteger());
            ECPoint pointG = new ECPoint(ecSpec.getG().getXCoord().toBigInteger(),
                    ecSpec.getG().getYCoord().toBigInteger());
            ECParameterSpec spec = new ECParameterSpec(curve, pointG, ecSpec.getN(), ecSpec.getH().intValue());
            KeyPairGenerator g = KeyPairGenerator.getInstance(KEY_PAIR_GENERATOR_TYPE);
            g.initialize(spec, new SecureRandom());
            KeyPair keysPair = g.generateKeyPair();

            ECPrivateKeySpec ecPrivSpec = new ECPrivateKeySpec(((ECPrivateKey) keysPair.getPrivate()).getS(), spec);
            ECPublicKeySpec ecPublicSpec = new ECPublicKeySpec(((ECPublicKey) keysPair.getPublic()).getW(), spec);
            KeyFactory kf = KeyFactory.getInstance(KEY_FACTORY_TYPE);
            PrivateKey privateKey = kf.generatePrivate(ecPrivSpec);
            PublicKey publicKey = kf.generatePublic(ecPublicSpec);

            logger.debug("Keys generated in {} ms.", (System.currentTimeMillis() - startTime));

            X500Name issuerDN = new X500Name(X500_NAME);
            Integer randomNumber = new Random().nextInt();
            BigInteger serialNumber = BigInteger.valueOf(randomNumber >= 0 ? randomNumber : randomNumber * -1);
            Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30);
            Date notAfter = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365 * 10));
            X500Name subjectDN = new X500Name(X500_NAME);
            byte[] publickeyb = publicKey.getEncoded();
            ASN1Sequence sequence = (ASN1Sequence) ASN1Primitive.fromByteArray(publickeyb);
            SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(sequence);
            X509v3CertificateBuilder v3CertGen = new X509v3CertificateBuilder(issuerDN, serialNumber, notBefore,
                    notAfter, subjectDN, subPubKeyInfo);

            ContentSigner contentSigner = new JcaContentSignerBuilder(CONTENT_SIGNER_ALGORITHM)
                    .build(keysPair.getPrivate());
            X509CertificateHolder certificateHolder = v3CertGen.build(contentSigner);

            Certificate certificate = java.security.cert.CertificateFactory.getInstance(CERTIFICATE_X509_TYPE)
                    .generateCertificate(new ByteArrayInputStream(
                            ByteBuffer.wrap(certificateHolder.toASN1Structure().getEncoded()).array()));

            logger.debug("Total certificate generation time: {} ms.", (System.currentTimeMillis() - startTime));

            keystore.setKeyEntry(KEYSTORE_ENTRY_ALIAS, privateKey, KEYSTORE_PASSWORD.toCharArray(),
                    new java.security.cert.Certificate[] { certificate });

            logger.debug("Save the keystore into {}.", keystoreFile.getAbsolutePath());

            keystore.store(new FileOutputStream(keystoreFile), KEYSTORE_PASSWORD.toCharArray());

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | OperatorCreationException
                | InvalidAlgorithmParameterException e) {
            throw new CertificateException("Failed to generate the new certificate.", e);
        }
    }

}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.io.eebus.internal.cert;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Generates a self-signed EC (P-256) identity certificate for a local EEBus SHIP node. The
 * BouncyCastle provider instance is passed explicitly to each builder rather than registered
 * globally via {@code java.security.Security}, so this does not affect other bundles in the OSGi
 * runtime.
 *
 * @author openHAB EEBus Binding Contributors - Initial contribution
 */
@NonNullByDefault
public final class SelfSignedCertificateFactory {

    private static final String CURVE = "secp256r1";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";

    private SelfSignedCertificateFactory() {
    }

    /**
     * Generates a fresh self-signed certificate/key pair.
     *
     * @param subjectDn the certificate subject, e.g. {@code "CN=openHAB EEBus"}
     * @param validityDays how many days from now the certificate should remain valid
     * @return the generated private key and matching self-signed certificate
     * @throws GeneralSecurityException if key generation or signing fails
     */
    public static KeyAndCertificate generate(String subjectDn, int validityDays) throws GeneralSecurityException {
        BouncyCastleProvider provider = new BouncyCastleProvider();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", provider);
        keyPairGenerator.initialize(new ECGenParameterSpec(CURVE), new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Instant now = Instant.now();
        Date notBefore = Date.from(now);
        Date notAfter = Date.from(now.plus(Duration.ofDays(validityDays)));
        BigInteger serial = new BigInteger(159, new SecureRandom());
        X500Name subject = new X500Name(subjectDn);

        X509v3CertificateBuilder certificateBuilder = new JcaX509v3CertificateBuilder(subject, serial, notBefore,
                notAfter, subject, keyPair.getPublic());

        // EEBus SHIP identifies peers by their certificate's Subject Key Identifier (the "SKI") -
        // a plain self-signed cert without this X.509v3 extension is rejected by SHIP peers
        // during the TLS handshake ("no valid SKI provided in certificate").
        JcaX509ExtensionUtils extensionUtils = new JcaX509ExtensionUtils();
        try {
            certificateBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                    extensionUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to add Subject Key Identifier extension", e);
        }

        ContentSigner signer;
        try {
            signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(provider).build(keyPair.getPrivate());
        } catch (OperatorCreationException e) {
            throw new GeneralSecurityException("Failed to create certificate signer", e);
        }

        X509CertificateHolder certificateHolder = certificateBuilder.build(signer);
        X509Certificate certificate = new JcaX509CertificateConverter().setProvider(provider)
                .getCertificate(certificateHolder);

        return new KeyAndCertificate(keyPair.getPrivate(), certificate);
    }
}

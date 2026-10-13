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
package org.openhab.binding.smartthings.internal.ocf;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.CertificateVerificationResult;
import org.eclipse.californium.scandium.dtls.ConnectionId;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * CA path verification without accepting unknown roots, invalid certificates or raw public keys.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class SamsungCertificateVerifierTest {
    @Test
    void bundledRootHasTheExpectedFingerprintAndIsAValidSelfSignedCa() throws Exception {
        var root = SamsungCertificateVerifier.rootCertificate();
        root.checkValidity();
        root.verify(root.getPublicKey());
        assertTrue(root.getBasicConstraints() >= 0);
        assertEquals("e363fd4cc50380266d757321469e9adec15e5ecbee28201447ece02a52ed627f",
                HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(root.getEncoded())));
        var verifier = new SamsungCertificateVerifier();
        assertEquals(List.of(CertificateType.X_509), verifier.getSupportedCertificateTypes());
        assertTrue(verifier.getAcceptedIssuers().isEmpty());
    }

    @Test
    void acceptsAValidChainWithOcfSubjectsNotIpNamesAndRejectsAnotherCa() throws Exception {
        var ca = keyPair();
        var root = certificate(ca, ca, "root", "root", true, true, false);
        var leaf = certificate(keyPair(), ca, "urn:uuid:01234567-89ab-cdef-0123-456789abcdef", "root", false, true,
                false);
        var verifier = new SamsungCertificateVerifier(root);
        var intermediateKey = keyPair();
        var intermediate = certificate(intermediateKey, ca, "intermediate", "root", true, true, false);
        var chained = certificate(keyPair(), intermediateKey, "device", "intermediate", false, true, false);
        assertNull(verify(verifier, List.of(chained, intermediate, root), false).getException());
        assertNull(verify(verifier, List.of(leaf, root), false).getException());
        assertNull(verify(verifier, List.of(leaf), false).getException());
        assertNotNull(verify(verifier, List.of(leaf, root), true).getException());
        var otherCa = keyPair();
        var otherRoot = certificate(otherCa, otherCa, "root", "root", true, true, false);
        var unknown = certificate(keyPair(), otherCa, "other-device", "root", false, true, false);
        assertNotNull(verify(verifier, List.of(unknown, otherRoot), false).getException());
        assertNotNull(verify(new SamsungCertificateVerifier(), List.of(leaf, root), false).getException());
    }

    @Test
    void rejectsExpiredNonSigningEmptyAndRawPublicKeyPresentations() throws Exception {
        var ca = keyPair();
        var root = certificate(ca, ca, "root", "root", true, true, false);
        var verifier = new SamsungCertificateVerifier(root);
        for (var leaf : List.of(certificate(keyPair(), ca, "expired", "root", false, true, true),
                certificate(keyPair(), ca, "non-signing", "root", false, false, false),
                certificate(keyPair(), ca, "client-only", "root", false, true, false, KeyPurposeId.id_kp_clientAuth))) {
            assertNotNull(verify(verifier, List.of(leaf, root), false).getException());
        }
        assertNotNull(verify(verifier, List.of(), false).getException());
        var raw = mock(CertificateMessage.class);
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, true, false, raw).getException());
        var nonCa = certificate(keyPair(), ca, "not-ca", "root", false, true, false);
        assertThrows(java.security.GeneralSecurityException.class, () -> new SamsungCertificateVerifier(nonCa));
    }

    private static CertificateVerificationResult verify(SamsungCertificateVerifier verifier,
            List<X509Certificate> certificates, boolean clientUsage) throws Exception {
        var message = mock(CertificateMessage.class);
        when(message.getCertificateChain())
                .thenReturn(CertificateFactory.getInstance("X.509").generateCertPath(certificates));
        return verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                clientUsage, true, false, message);
    }

    private static KeyPair keyPair() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    private static X509Certificate certificate(KeyPair key, KeyPair signer, String subject, String issuer, boolean ca,
            boolean signing, boolean expired) throws Exception {
        return certificate(key, signer, subject, issuer, ca, signing, expired, KeyPurposeId.id_kp_serverAuth);
    }

    private static X509Certificate certificate(KeyPair key, KeyPair signer, String subject, String issuer, boolean ca,
            boolean signing, boolean expired, KeyPurposeId purpose) throws Exception {
        var now = Instant.now();
        var builder = new JcaX509v3CertificateBuilder(new X500Name("CN=" + issuer),
                new BigInteger(100, new java.security.SecureRandom()), Date.from(now.minusSeconds(3600)),
                Date.from(now.plusSeconds(expired ? -60 : 3600)), new X500Name("CN=" + subject), key.getPublic());
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(ca));
        builder.addExtension(Extension.keyUsage, true, new KeyUsage(ca ? KeyUsage.keyCertSign | KeyUsage.cRLSign
                : signing ? KeyUsage.digitalSignature : KeyUsage.keyEncipherment));
        if (!ca) {
            builder.addExtension(Extension.extendedKeyUsage, false, new ExtendedKeyUsage(purpose));
        }
        return new JcaX509CertificateConverter()
                .getCertificate(builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(signer.getPrivate())));
    }
}

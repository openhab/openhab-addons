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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import javax.naming.NamingException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.eclipse.californium.scandium.dtls.AlertMessage;
import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.CertificateVerificationResult;
import org.eclipse.californium.scandium.dtls.ConnectionId;
import org.eclipse.californium.scandium.dtls.HandshakeException;
import org.eclipse.californium.scandium.dtls.HandshakeResultHandler;
import org.eclipse.californium.scandium.dtls.x509.NewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.dtls.x509.StaticNewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.util.ServerNames;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates appliance certificate paths against the bundled Samsung OCF root.
 * No system roots, remotely downloaded anchors or trust-on-first-use are accepted.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class SamsungCertificateVerifier implements NewAdvancedCertificateVerifier {
    private final NewAdvancedCertificateVerifier verifier;
    private volatile @Nullable UUID serverIdentity;

    SamsungCertificateVerifier() throws IOException, GeneralSecurityException {
        this(rootCertificate());
    }

    SamsungCertificateVerifier(X509Certificate root) throws GeneralSecurityException {
        root.checkValidity();
        if (root.getBasicConstraints() < 0) {
            throw new GeneralSecurityException("A CA certificate is required");
        }
        verifier = StaticNewAdvancedCertificateVerifier.builder().setTrustedCertificates(root)
                .setSupportedCertificateTypes(List.of(CertificateType.X_509)).setUseEmptyAcceptedIssuers(true).build();
    }

    static X509Certificate rootCertificate() throws IOException, GeneralSecurityException {
        try (var input = SamsungCertificateVerifier.class.getResourceAsStream("/samsung-ocf-root.pem")) {
            if (input == null) {
                throw new IOException("Samsung OCF trust anchor is missing");
            }
            var root = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(input);
            if (!MessageDigest.isEqual(MessageDigest.getInstance("SHA-256").digest(root.getEncoded()),
                    HexFormat.of().parseHex("e363fd4cc50380266d757321469e9adec15e5ecbee28201447ece02a52ed627f"))) {
                throw new GeneralSecurityException("Unexpected Samsung OCF trust anchor");
            }
            root.verify(root.getPublicKey());
            return root;
        }
    }

    @Override
    @NonNullByDefault({})
    public List<CertificateType> getSupportedCertificateTypes() {
        return List.of(CertificateType.X_509);
    }

    @Override
    @NonNullByDefault({})
    public CertificateVerificationResult verifyCertificate(ConnectionId cid, @Nullable ServerNames serverName,
            InetSocketAddress remotePeer, boolean clientUsage, boolean verifySubject, boolean truncateCertificatePath,
            CertificateMessage message) {
        var chain = message.getCertificateChain();
        if (clientUsage || chain == null || chain.getCertificates().isEmpty()) {
            return new CertificateVerificationResult(cid, new HandshakeException(
                    "An appliance X.509 certificate chain is required",
                    new AlertMessage(AlertMessage.AlertLevel.FATAL, AlertMessage.AlertDescription.BAD_CERTIFICATE)),
                    null);
        }
        // Samsung certificates contain OCF identities, not DNS/IP names. Bind the validated certificate identity to
        // the identity returned over the authenticated /oic/d connection.
        try {
            var leafCertificate = chain.getCertificates().getFirst();
            if (!(leafCertificate instanceof X509Certificate certificate)) {
                throw new IllegalArgumentException("Appliance certificate is not X.509");
            }
            UUID identity = certificateIdentity(certificate);
            CertificateVerificationResult result = verifier.verifyCertificate(cid, serverName, remotePeer, false, false,
                    truncateCertificatePath, message);
            if (result.getException() == null) {
                serverIdentity = identity;
            }
            return result;
        } catch (NamingException | IllegalArgumentException e) {
            return new CertificateVerificationResult(cid, new HandshakeException(
                    "Appliance certificate does not contain a valid OCF identity",
                    new AlertMessage(AlertMessage.AlertLevel.FATAL, AlertMessage.AlertDescription.BAD_CERTIFICATE)),
                    null);
        }
    }

    @Nullable
    UUID serverIdentity() {
        return serverIdentity;
    }

    private static UUID certificateIdentity(X509Certificate certificate) throws NamingException {
        var subject = new LdapName(certificate.getSubjectX500Principal().getName(X500Principal.RFC2253));
        for (Rdn rdn : subject.getRdns()) {
            if ("CN".equalsIgnoreCase(rdn.getType()) && rdn.getValue() instanceof String commonName
                    && commonName.startsWith("urn:uuid:")) {
                return UUID.fromString(commonName.substring("urn:uuid:".length()));
            }
        }
        throw new IllegalArgumentException("Missing OCF identity");
    }

    @Override
    @NonNullByDefault({})
    public List<X500Principal> getAcceptedIssuers() {
        return List.of();
    }

    @Override
    @NonNullByDefault({})
    public void setResultHandler(HandshakeResultHandler resultHandler) {
        verifier.setResultHandler(resultHandler);
    }
}

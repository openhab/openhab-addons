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
        // Samsung certificates contain OCF identities, not DNS/IP names. Logical /oic/d identity is checked by the
        // handler.
        return verifier.verifyCertificate(cid, serverName, remotePeer, false, false, truncateCertificatePath, message);
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

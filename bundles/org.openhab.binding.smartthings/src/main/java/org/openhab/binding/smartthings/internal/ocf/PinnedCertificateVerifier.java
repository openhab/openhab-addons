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

import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertPath;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.x500.X500Principal;

import org.eclipse.californium.scandium.dtls.AlertMessage;
import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.CertificateVerificationResult;
import org.eclipse.californium.scandium.dtls.ConnectionId;
import org.eclipse.californium.scandium.dtls.HandshakeException;
import org.eclipse.californium.scandium.dtls.HandshakeResultHandler;
import org.eclipse.californium.scandium.dtls.x509.NewAdvancedCertificateVerifier;
import org.eclipse.californium.scandium.util.ServerNames;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Authenticates the appliance by an explicit certificate pin or trust-on-first-use.
 * Pinning replaces CA trust, not certificate validity or server-authentication usage checks.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
final class PinnedCertificateVerifier implements NewAdvancedCertificateVerifier {
    private static final String SERVER_AUTHENTICATION = "1.3.6.1.5.5.7.3.1";

    private record Pin(byte[] fingerprint) {
    }

    private final AtomicReference<@Nullable Pin> fingerprint;

    PinnedCertificateVerifier() {
        fingerprint = new AtomicReference<>(null);
    }

    PinnedCertificateVerifier(byte[] fingerprint) {
        if (fingerprint.length != 32) {
            throw new IllegalArgumentException("A SHA-256 certificate fingerprint is required");
        }
        this.fingerprint = new AtomicReference<>(new Pin(fingerprint.clone()));
    }

    @Nullable
    String fingerprint() {
        Pin current = fingerprint.get();
        return current == null ? null : HexFormat.of().formatHex(current.fingerprint());
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
        CertPath chain = message.getCertificateChain();
        if (!clientUsage && chain != null && !chain.getCertificates().isEmpty()
                && chain.getCertificates().getFirst() instanceof X509Certificate leaf) {
            try {
                leaf.checkValidity();
                boolean[] usage = leaf.getKeyUsage();
                // Californium's authentication helper swallows EKU parsing failures; reject malformed extensions here.
                List<String> extendedUsage = leaf.getExtendedKeyUsage();
                if (usage == null || usage.length > 0 && usage[0]) {
                    if (extendedUsage == null || extendedUsage.contains(SERVER_AUTHENTICATION)) {
                        byte[] presented = MessageDigest.getInstance("SHA-256").digest(leaf.getEncoded());
                        Pin pinned = fingerprint.get();
                        if (pinned == null) {
                            fingerprint.compareAndSet(null, new Pin(presented));
                            pinned = fingerprint.get();
                        }
                        if (pinned != null && MessageDigest.isEqual(pinned.fingerprint(), presented)) {
                            return new CertificateVerificationResult(cid, chain, null);
                        }
                    }
                }
            } catch (GeneralSecurityException e) {
                // Return a protocol authentication failure, without certificate or credential diagnostics.
            }
        }
        return new CertificateVerificationResult(cid,
                new HandshakeException("Appliance certificate does not match the configured trust anchor",
                        new AlertMessage(AlertMessage.AlertLevel.FATAL, AlertMessage.AlertDescription.BAD_CERTIFICATE)),
                null);
    }

    @Override
    @NonNullByDefault({})
    public List<X500Principal> getAcceptedIssuers() {
        return List.of();
    }

    @Override
    @NonNullByDefault({})
    public void setResultHandler(HandshakeResultHandler resultHandler) {
    }
}

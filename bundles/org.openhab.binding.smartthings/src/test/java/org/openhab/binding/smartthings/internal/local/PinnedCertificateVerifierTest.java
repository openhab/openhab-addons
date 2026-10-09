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
package org.openhab.binding.smartthings.internal.local;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.cert.CertPath;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.eclipse.californium.scandium.dtls.CertificateMessage;
import org.eclipse.californium.scandium.dtls.CertificateType;
import org.eclipse.californium.scandium.dtls.ConnectionId;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Out-of-band certificate pin verification and failure tests.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
class PinnedCertificateVerifierTest {
    @Test
    void acceptsOnlyTheExplicitlyPinnedValidServerLeafAndCopiesThePin() throws Exception {
        byte[] encoded = new byte[] { 1, 2, 3, 4 };
        X509Certificate leaf = mock(X509Certificate.class);
        when(leaf.getEncoded()).thenReturn(encoded);
        when(leaf.getKeyUsage()).thenReturn(new boolean[] { true });
        CertPath chain = mock(CertPath.class);
        doReturn(List.of(leaf)).when(chain).getCertificates();
        CertificateMessage message = mock(CertificateMessage.class);
        when(message.getCertificateChain()).thenReturn(chain);
        byte[] pin = MessageDigest.getInstance("SHA-256").digest(encoded);
        PinnedCertificateVerifier verifier = new PinnedCertificateVerifier(pin);
        pin[0] ^= 1;
        assertEquals(List.of(CertificateType.X_509), verifier.getSupportedCertificateTypes());
        assertTrue(verifier.getAcceptedIssuers().isEmpty());
        var result = verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, true, false, message);
        assertNull(result.getException());
        assertSame(chain, result.getCertificatePath());
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                true, true, false, message).getException());
        when(leaf.getEncoded()).thenReturn(new byte[] { 5 });
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, true, false, message).getException());
    }

    @Test
    void rejectsExpiredNonSigningEmptyAndUnpinnedCertificates() throws Exception {
        X509Certificate leaf = mock(X509Certificate.class);
        when(leaf.getEncoded()).thenReturn(new byte[] { 1 });
        CertPath chain = mock(CertPath.class);
        doReturn(List.of(leaf)).when(chain).getCertificates();
        CertificateMessage message = mock(CertificateMessage.class);
        when(message.getCertificateChain()).thenReturn(chain);
        PinnedCertificateVerifier verifier = new PinnedCertificateVerifier(
                MessageDigest.getInstance("SHA-256").digest(new byte[] { 1 }));
        when(leaf.getKeyUsage()).thenReturn(new boolean[] { false });
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, false, false, message).getException());
        when(leaf.getKeyUsage()).thenReturn(new boolean[] { true });
        doThrow(new CertificateExpiredException("sensitive certificate detail")).when(leaf).checkValidity();
        var result = verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, false, false, message);
        assertNotNull(result.getException());
        assertFalse(result.getException().getMessage().contains("sensitive certificate detail"));
        when(chain.getCertificates()).thenReturn(List.of());
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, false, false, message).getException());
        when(message.getCertificateChain()).thenReturn(null);
        assertNotNull(verifier.verifyCertificate(ConnectionId.EMPTY, null, new InetSocketAddress("127.0.0.1", 49155),
                false, false, false, message).getException());
        assertThrows(IllegalArgumentException.class, () -> new PinnedCertificateVerifier(new byte[31]));
    }
}

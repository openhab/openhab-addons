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

import static org.junit.jupiter.api.Assertions.*;

import java.security.cert.X509Certificate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link SelfSignedCertificateFactory}.
 */
@NonNullByDefault
class SelfSignedCertificateFactoryTest {

    @Test
    void generatesASelfSignedCertificateValidForTheRequestedPeriod() throws Exception {
        KeyAndCertificate result = SelfSignedCertificateFactory.generate("CN=openHAB EEBus test", 3650);

        X509Certificate certificate = result.certificate();
        assertEquals(certificate.getSubjectX500Principal(), certificate.getIssuerX500Principal(),
                "certificate should be self-signed");
        // Verifying with its own public key must not throw if the certificate is correctly signed.
        assertDoesNotThrow(() -> certificate.verify(certificate.getPublicKey()));

        certificate.checkValidity();
        assertEquals("EC", result.privateKey().getAlgorithm());

        // EEBus SHIP peers read the SKI off this extension during the TLS handshake and reject
        // certificates that lack it ("no valid SKI provided in certificate") - verified live
        // against meisel2000/eebus-cbsim on 2026-08-01.
        assertNotNull(certificate.getExtensionValue("2.5.29.14"), "certificate must carry a Subject Key Identifier");
    }

    @Test
    void generatesADifferentIdentityOnEachCall() throws Exception {
        KeyAndCertificate first = SelfSignedCertificateFactory.generate("CN=openHAB EEBus test", 3650);
        KeyAndCertificate second = SelfSignedCertificateFactory.generate("CN=openHAB EEBus test", 3650);

        assertNotEquals(first.certificate().getSerialNumber(), second.certificate().getSerialNumber());
        assertFalse(first.privateKey().equals(second.privateKey()));
    }
}

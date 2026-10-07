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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openmuc.jeebus.ship.api.cert.CertificateInfo;

/**
 * Tests for {@link EEBusCertificateStorage}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class EEBusCertificateStorageTest {

    @Test
    void generatesAndPersistsACertificateOnFirstRead() throws Exception {
        FakeStorage storage = new FakeStorage();
        EEBusCertificateStorage certStorage = new EEBusCertificateStorage(storage, "org.openhab.io.eebus.identity",
                "CN=openHAB EEBus test", 3650);

        assertTrue(storage.getKeys().isEmpty());

        Optional<CertificateInfo> first = certStorage.readCertificate();
        assertTrue(first.isPresent());
        assertFalse(storage.getKeys().isEmpty(), "certificate should have been persisted");
    }

    @Test
    void returnsTheSameIdentityOnRepeatedReads() throws Exception {
        FakeStorage storage = new FakeStorage();
        EEBusCertificateStorage certStorage = new EEBusCertificateStorage(storage, "org.openhab.io.eebus.identity",
                "CN=openHAB EEBus test", 3650);

        CertificateInfo first = certStorage.readCertificate().orElseThrow();
        CertificateInfo second = certStorage.readCertificate().orElseThrow();

        assertEquals(first.certificate.getSerialNumber(), second.certificate.getSerialNumber());
        assertEquals(first.privateKey, second.privateKey);
    }

    @Test
    void differentIdentityKeysGetIndependentIdentities() throws Exception {
        FakeStorage storage = new FakeStorage();
        EEBusCertificateStorage storageA = new EEBusCertificateStorage(storage, "identity-a", "CN=A", 3650);
        EEBusCertificateStorage storageB = new EEBusCertificateStorage(storage, "identity-b", "CN=B", 3650);

        CertificateInfo a = storageA.readCertificate().orElseThrow();
        CertificateInfo b = storageB.readCertificate().orElseThrow();

        assertNotEquals(a.certificate.getSerialNumber(), b.certificate.getSerialNumber());
    }
}

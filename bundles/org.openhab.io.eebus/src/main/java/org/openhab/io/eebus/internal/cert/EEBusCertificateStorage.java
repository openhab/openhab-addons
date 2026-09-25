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

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.storage.Storage;
import org.openmuc.jeebus.ship.api.cert.CertificateInfo;
import org.openmuc.jeebus.ship.api.cert.CertificateStorage;
import org.openmuc.jeebus.ship.api.cert.CertificateStoreException;

/**
 * A {@link CertificateStorage} backed by openHAB's own {@link Storage} service instead of a
 * standalone keystore file. Generates and persists a self-signed identity certificate for the
 * given thing on first use, so every {@code controllableSystem} thing gets its own stable SHIP
 * identity (and thus a stable SKI) without any manual keystore provisioning step.
 *
 * @author openHAB EEBus Binding Contributors - Initial contribution
 */
@NonNullByDefault
public class EEBusCertificateStorage implements CertificateStorage {

    private final Storage<String> storage;
    private final String keyPrefix;
    private final String subjectDn;
    private final int validityDays;

    public EEBusCertificateStorage(Storage<String> storage, String thingUid, String subjectDn, int validityDays) {
        this.storage = storage;
        this.keyPrefix = thingUid + ":";
        this.subjectDn = subjectDn;
        this.validityDays = validityDays;
    }

    // jEEBus's CertificateStorage interface carries no null-safety annotations of its own; reset
    // this class's @NonNullByDefault for the two override signatures so they match it exactly.
    @Override
    @NonNullByDefault({})
    public Optional<CertificateInfo> readCertificate() throws CertificateStoreException {
        @Nullable
        String encodedKey = storage.get(keyPrefix + "privateKey");
        @Nullable
        String encodedCert = storage.get(keyPrefix + "certificate");

        if (encodedKey != null && encodedCert != null) {
            try {
                return Optional.of(decode(encodedKey, encodedCert));
            } catch (GeneralSecurityException e) {
                throw new CertificateStoreException("Failed to decode stored EEBus certificate", e);
            }
        }

        KeyAndCertificate generated;
        try {
            generated = SelfSignedCertificateFactory.generate(subjectDn, validityDays);
        } catch (GeneralSecurityException e) {
            throw new CertificateStoreException("Failed to generate a self-signed EEBus certificate", e);
        }
        CertificateInfo certificateInfo = new CertificateInfo(generated.privateKey(), generated.certificate());
        saveCertificate(certificateInfo);
        return Optional.of(certificateInfo);
    }

    @Override
    @NonNullByDefault({})
    public void saveCertificate(CertificateInfo certificateInfo) throws CertificateStoreException {
        try {
            storage.put(keyPrefix + "privateKey",
                    Base64.getEncoder().encodeToString(certificateInfo.privateKey.getEncoded()));
            storage.put(keyPrefix + "certificate",
                    Base64.getEncoder().encodeToString(certificateInfo.certificate.getEncoded()));
        } catch (GeneralSecurityException e) {
            throw new CertificateStoreException("Failed to encode EEBus certificate for storage", e);
        }
    }

    private CertificateInfo decode(String encodedKey, String encodedCert) throws GeneralSecurityException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(encodedKey));
        PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(keySpec);

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory
                .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(encodedCert)));

        return new CertificateInfo(privateKey, certificate);
    }

    /**
     * Removes this thing's stored identity, so a fresh certificate (and SKI) is generated on next
     * {@link #readCertificate()}.
     */
    public void reset() {
        storage.remove(keyPrefix + "privateKey");
        storage.remove(keyPrefix + "certificate");
    }
}

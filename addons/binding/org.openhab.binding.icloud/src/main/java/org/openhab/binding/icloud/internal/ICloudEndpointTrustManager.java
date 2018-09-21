/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.icloud.internal.to_be_moved.EndpointKeyStore;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a TrustManager for https://fmipmobile.icloud.com
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@Component
@NonNullByDefault
public class ICloudEndpointTrustManager implements EndpointKeyStore {

    @Override
    public String getHostName() {
        return "fmipmobile.icloud.com";
    }

    @Override
    public KeyStore getKeyStore() {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("trustedCerts.pkcs12")) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(inputStream, "passphrase".toCharArray());
            return ks;
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed loading (content) of keystore", e);
        }
    }
}

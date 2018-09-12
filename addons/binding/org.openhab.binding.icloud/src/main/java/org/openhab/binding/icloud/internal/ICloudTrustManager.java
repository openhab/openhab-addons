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
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * Implements a TrustManager for https://fmipmobile.icloud.com
 *
 * @author Martin van Wingerden - Initial Contribution
 */
public class ICloudTrustManager implements X509TrustManager {
    private final X509Certificate certicate;

    public ICloudTrustManager() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("trustedCerts.pkcs12");

        ks.load(inputStream, "passphrase".toCharArray());
        certicate = (X509Certificate) ks.getCertificate("appleRootCA");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // we don't care about the client certificates
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // we don't care about the server certificates
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        // but we do about CA's
        return new X509Certificate[] { certicate };
    }
}

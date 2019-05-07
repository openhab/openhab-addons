/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.samsungtv.internal;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.osgi.service.component.annotations.Component;

/**
 * Provides a TrustManager to allow secure websocket connections to any TV (=server)
 *
 * @author Arjan Mels - Initial Contribution
 */
@Component
@NonNullByDefault
public class SamsungTvTlsTrustManagerProvider implements TlsTrustManagerProvider {
    @Override
    public String getHostName() {
        return "SmartViewSDK";
    }

    private final X509ExtendedTrustManager trustAllCerts = new X509ExtendedTrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s)
                throws CertificateException {
        }

        @Override
        public X509Certificate @Nullable [] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable Socket socket) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable Socket socket) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable SSLEngine sslEngine) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate @Nullable [] x509Certificates, @Nullable String s,
                @Nullable SSLEngine sslEngine) throws CertificateException {
        }
    };

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return trustAllCerts;
    }
}

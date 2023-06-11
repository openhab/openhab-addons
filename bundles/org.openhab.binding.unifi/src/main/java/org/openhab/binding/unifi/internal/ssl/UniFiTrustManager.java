/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.ssl;

import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 *
 * The {@link UniFiTrustManager} is a "trust all" implementation of {@link X509ExtendedTrustManager}.
 *
 * @see {@link UniFiTrustManagerProvider}
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiTrustManager extends X509ExtendedTrustManager {

    private static UniFiTrustManager instance = new UniFiTrustManager();

    public static UniFiTrustManager getInstance() {
        return instance;
    }

    /**
     * private construction - singleton
     */
    private UniFiTrustManager() {
    }

    @Override
    public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType)
            throws CertificateException {
    }

    @Override
    public X509Certificate @Nullable [] getAcceptedIssuers() {
        return null;
    }

    @Override
    public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType,
            final @Nullable Socket socket) throws CertificateException {
    }

    @Override
    public void checkClientTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType,
            final @Nullable SSLEngine engine) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType,
            final @Nullable Socket socket) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(final X509Certificate @Nullable [] chain, final @Nullable String authType,
            final @Nullable SSLEngine engine) throws CertificateException {
    }
}

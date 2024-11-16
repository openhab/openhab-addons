/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.connection;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;

/**
 * Provides a {@link PEMTrustManager} to allow secure connections to a Hue HDMI
 * Sync Box
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncTrustManagerProvider implements TlsTrustManagerProvider {
    private final String host;
    private final Integer port;

    private final X509ExtendedTrustManager trustManager;

    public HueSyncTrustManagerProvider(String host, Integer port) throws IOException, CertificateException {
        this.trustManager = PEMTrustManager.getInstanceFromServer("https://" + host);
        this.port = port;
        this.host = host;
    }

    @Override
    public String getHostName() {
        return this.host + ":" + this.port;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return this.trustManager;
    }
}

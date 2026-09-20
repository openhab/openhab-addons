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
package org.openhab.binding.network.internal;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;

/**
 * Accepts any certificate presented by the given host. Instances are registered as a service while a thing which is
 * configured to ignore certificate errors is initialized.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class NetworkTlsTrustManagerProvider implements TlsTrustManagerProvider {

    private final String hostName;

    /**
     * @param hostName the host this provider is responsible for, in the form <code>host:port</code>
     */
    public NetworkTlsTrustManagerProvider(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return TrustAllTrustManager.getInstance();
    }
}

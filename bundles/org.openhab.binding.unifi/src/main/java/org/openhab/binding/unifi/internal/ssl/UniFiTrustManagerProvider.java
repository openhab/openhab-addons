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
package org.openhab.binding.unifi.internal.ssl;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;

/**
 *
 * The {@link UniFiTrustManagerProvider} is an implementation of {@link TlsTrustManagerProvider} which provides an
 * instance of {@link UniFiTrustManagerProvider} for all servers that use a certificate with the common name equal to
 * "UniFi" (<code>CN=UniFi</code>).
 *
 * @author Matthew Bowman - Initial contribution
 */
// @Component // [wip] mgb: disabled due to issues with service order loading
@NonNullByDefault
public class UniFiTrustManagerProvider implements TlsTrustManagerProvider {

    @Override
    public String getHostName() {
        return "UniFi";
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return UniFiTrustManager.getInstance();
    }
}

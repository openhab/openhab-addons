/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.unifi.internal.ssl;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;

/**
 *
 * The {@link UniFiTrustManagerProvider} is an implementation of {@link TlsTrustManagerProvider} which provides an
 * instance of {@link UniFiTrustManagerProvider} for all servers that use a certificate with the common name equal to
 * "UniFi" (<code>CN=UniFi</code>).
 *
 * @author Matthew Bowman - Initial contribution
 */
// @Component // [wip] mgb: disabled due to issues with service order loading
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

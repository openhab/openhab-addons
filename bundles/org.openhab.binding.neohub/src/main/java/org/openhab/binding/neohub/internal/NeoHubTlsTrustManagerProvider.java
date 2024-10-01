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
package org.openhab.binding.neohub.internal;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;

/**
 * A {@link TlsTrustManagerProvider} implementation to validate the NeoHub web socket self signed certificate.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class NeoHubTlsTrustManagerProvider implements TlsTrustManagerProvider {

    private final String fullHostName;

    public NeoHubTlsTrustManagerProvider(NeoHubConfiguration config) {
        fullHostName = String.format("%s:%d", config.hostName,
                config.portNumber > 0 ? config.portNumber : NeoHubBindingConstants.PORT_WSS);
    }

    @Override
    public String getHostName() {
        return fullHostName;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return TrustAllTrustManager.getInstance();
    }
}

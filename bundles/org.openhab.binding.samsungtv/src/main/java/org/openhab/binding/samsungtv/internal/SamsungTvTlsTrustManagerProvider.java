/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;
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

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return TrustAllTrustManager.getInstance();
    }
}

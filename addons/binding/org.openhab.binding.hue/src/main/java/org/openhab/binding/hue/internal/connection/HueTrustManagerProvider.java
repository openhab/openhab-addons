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
package org.openhab.binding.hue.internal.connection;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.smarthome.io.net.http.TlsTrustManagerProvider;
import org.osgi.service.component.annotations.Component;

/**
 *
 * The {@link HueTrustManagerProvider} is an implementation of {@link TlsTrustManagerProvider} which provides an
 * instance of {@link HueTrustManagerProvider} for all servers that use a certificate with the common name equal to
 * "001788fffe403ccd" (<code>CN=001788fffe403ccd</code>).
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@Component
public class HueTrustManagerProvider implements TlsTrustManagerProvider {

    @Override
    public String getHostName() {
        return "001788fffe403ccd";
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        return HueTrustManager.getInstance();
    }
}

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
package org.openhab.binding.vizio.internal.communication;

import java.net.MalformedURLException;
import java.security.cert.CertificateException;

import javax.net.ssl.X509ExtendedTrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.PEMTrustManager;
import org.openhab.core.io.net.http.TlsTrustManagerProvider;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link PEMTrustManager} to allow secure connections to a Vizio TV that uses self signed
 * certificates.
 *
 * @author Christoph Weitkamp - Initial Contribution
 * @author Michael Lobstein - Adapted for Vizio binding
 */
@NonNullByDefault
public class VizioTlsTrustManagerProvider implements TlsTrustManagerProvider {
    private final String hostname;

    private final Logger logger = LoggerFactory.getLogger(VizioTlsTrustManagerProvider.class);

    public VizioTlsTrustManagerProvider(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public String getHostName() {
        return hostname;
    }

    @Override
    public X509ExtendedTrustManager getTrustManager() {
        try {
            logger.trace("Use self-signed certificate downloaded from Vizio TV.");
            return PEMTrustManager.getInstanceFromServer("https://" + getHostName());
        } catch (CertificateException | MalformedURLException e) {
            logger.debug("An unexpected exception occurred - returning a TrustAllTrustManager: {}", e.getMessage(), e);
        }
        return TrustAllTrustManager.getInstance();
    }
}

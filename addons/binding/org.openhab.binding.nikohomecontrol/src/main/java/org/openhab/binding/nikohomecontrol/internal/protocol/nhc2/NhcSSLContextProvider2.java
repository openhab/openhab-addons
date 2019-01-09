/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.transport.mqtt.sslcontext.SSLContextProvider;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an implementation of the {@link SSLContextProvider}. The {@link NhcTrustManagerII}
 * will be used for the SSLContext. This implementation forces a TLS 1.2 context.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcSSLContextProvider2 implements SSLContextProvider {
    private final Logger logger = LoggerFactory.getLogger(NhcSSLContextProvider2.class);
    final TrustManager[] trustManagers;

    public NhcSSLContextProvider2(TrustManager[] trustManagers) {
        this.trustManagers = trustManagers;
    }

    @Override
    public SSLContext getContext() throws ConfigurationException {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());
            return sslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.warn("SSL configuration failed", e);
            throw new ConfigurationException("ssl", e.getMessage());
        }
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.icloud.internal;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.to_be_moved.EndpointTrustManager;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a TrustManager for https://fmipmobile.icloud.com
 *
 * @author Martin van Wingerden - Initial Contribution
 */
@Component
@NonNullByDefault
public class ICloudEndpointTrustManager implements EndpointTrustManager {
    private final Logger logger = LoggerFactory.getLogger(ICloudEndpointTrustManager.class);

    @Nullable
    private TrustManager trustManager;

    public ICloudEndpointTrustManager() {
        try {
            trustManager = new ICloudTrustManager();
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Failed to initialize the trustmanager", e);
            trustManager = null;
        }
    }

    @Override
    public boolean supports(String endpoint) {
        return trustManager != null && endpoint.startsWith("https://fmipmobile.icloud.com");
    }

    @Override
    public TrustManager getTrustManager() {
        TrustManager localTrustManager = trustManager;

        if (localTrustManager == null) {
            throw new IllegalStateException("This method should not be called if supported = false");
        }

        return localTrustManager;
    }
}

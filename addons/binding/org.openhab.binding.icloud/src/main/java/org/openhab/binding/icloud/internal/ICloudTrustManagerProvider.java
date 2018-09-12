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
import java.util.stream.Stream;

import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.io.net.http.TrustManagerProvider;
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
public class ICloudTrustManagerProvider implements TrustManagerProvider {
    private final Logger logger = LoggerFactory.getLogger(ICloudTrustManagerProvider.class);

    @Override
    public Stream<TrustManager> getTrustManagers(String endpoint) {
        if (endpoint.startsWith("https://fmipmobile.icloud.com")) {
            try {
                return Stream.of(new ICloudTrustManager());
            } catch (GeneralSecurityException | IOException e) {
                logger.error("Failed to initialize the trustmanager", e);
            }
        }
        return Stream.empty();
    }
}

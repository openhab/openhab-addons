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
package org.openhab.binding.airgradient.internal.config;

import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirGradientAPIConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class AirGradientAPIConfiguration {

    public String hostname = "";
    public String token = "";
    public int refreshInterval = 600;

    public boolean isValid() {
        // hostname must be entered and be a URI
        if ("".equals(hostname)) {
            return false;
        }

        try {
            URI.create(hostname);
        } catch (IllegalArgumentException iae) {
            return false;
        }

        // token is optional

        // refresh interval is positive integer
        return (refreshInterval > 0);
    }

    /**
     * Returns true if this is a URL against the cloud.
     *
     * @return true if this is a URL against the cloud API
     */
    public boolean hasCloudUrl() {
        URI url = URI.create(hostname);
        return url.getPath().equals("/");
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;

/**
 * The {@link Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK and respective runtime
 */
@NonNullByDefault
public class Configuration {
    /**
     * Server hostname
     */
    public String hostname = "";
    /**
     * Server hostname
     */
    public int port = 8096;
    /**
     * Use Https
     */
    public Boolean ssl = true;
    /**
     * Jellyfin base url
     */
    public String path = "";
    /**
     * Interval to pull devices state from the server
     */
    public int refreshSeconds = 60;
    /**
     * Amount off seconds allowed since the last client update to assert it's online
     */
    public int clientActiveWithInSeconds = 0;
    /**
     * Access Token
     */
    public String token = "";
    /**
     * User ID
     */
    public String userId = "";

    /**
     * Checks if a configuration exists for the given thing.
     *
     * @param thing the thing to check the configuration for
     * @return true if the configuration is complete, false otherwise
     */
    public static boolean configurationExists(Thing thing) {
        Configuration config = thing.getConfiguration().as(Configuration.class);
        return config != null && !config.hostname.isEmpty();
    }

    public static boolean tokenExists(Thing thing) {
        Configuration config = thing.getConfiguration().as(Configuration.class);
        return config != null && !config.token.isEmpty();
    }
}

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
package org.openhab.binding.mystrom.internal;

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.DEFAULT_REFRESH_RATE_SECONDS;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MyStromConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Paul Frank - Initial contribution
 */
@NonNullByDefault
public class MyStromConfiguration {

    private final String url_prefix = "http://";

    private String hostname = "localhost";

    private String apiToken = "";

    private int refresh = DEFAULT_REFRESH_RATE_SECONDS;

    /**
     * Returns the hostname with http prefix if missing.
     *
     * @return hostname
     */
    public String getHostname() {
        String prefix = "";
        if (!this.hostname.contains(url_prefix)) {
            prefix = url_prefix;
        }
        return prefix + this.hostname;
    }

    /**
     * returns API Token
     *
     * @return apiToken
     */
    public String getApiToken() {
        return apiToken;
    }

    /**
     * Returns the refreshrate in SECONDS
     *
     * @return refresh
     */
    public int getRefresh() {
        return refresh;
    }
}

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.mystrom.internal.MyStromBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MyStromConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Paul Frank - Initial contribution
 * @author Stefan Navratil - Added configuration for myStrom PIR
 */
@NonNullByDefault
public class MyStromConfiguration {

    private final String urlPrefix = "http://";

    private String hostname = "localhost";

    private String apiToken = "";

    private int refresh = DEFAULT_REFRESH_RATE_SECONDS;

    private int backoffTime = DEFAULT_BACKOFF_TIME_SECONDS;

    private boolean ledEnable = true;

    /**
     * Returns the hostname with http prefix if missing.
     *
     * @return hostname
     */
    public String getHostname() {
        String prefix = "";
        if (!this.hostname.contains(urlPrefix)) {
            prefix = urlPrefix;
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
     * Returns the refreshrate in SECONDS.
     *
     * @return refresh
     */
    public int getRefresh() {
        return refresh;
    }

    /**
     * Returns the Backoff time of the MotionSensor in SECONDS.
     *
     * @return backoff_time
     */
    public int getBackoffTime() {
        return backoffTime;
    }

    /**
     * Returns the Status LED Configuration.
     *
     * @return led_enable
     */
    public boolean getLedEnable() {
        return ledEnable;
    }
}

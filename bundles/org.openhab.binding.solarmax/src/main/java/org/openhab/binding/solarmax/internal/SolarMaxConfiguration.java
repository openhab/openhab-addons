/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarmax.internal.connector.SolarMaxConnectionSettings;

/**
 * The {@link SolarMaxConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxConfiguration {
    public String host = ""; // this will always need to be overridden
    public int portNumber = 12345; // default value is 12345
    public int deviceAddress = 1; // default value is 1

    public int refreshInterval = 15; // default value is 15

    public int timeout = SolarMaxConnectionSettings.DEFAULT_TIMEOUT; // ms, MaxComm protocol description: 3000
    public int retries = SolarMaxConnectionSettings.DEFAULT_RETRIES;
    public int retryDelay = SolarMaxConnectionSettings.DEFAULT_RETRY_DELAY; // ms
    public int offlineAfterFailures = 3; // failed polls in a row before the thing goes OFFLINE

    public SolarMaxConnectionSettings getConnectionSettings() {
        // a poll must not take longer than the refresh interval, so retries only happen within that time budget
        return new SolarMaxConnectionSettings(timeout, retries, retryDelay, refreshInterval * 1000L);
    }
}

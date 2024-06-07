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
package org.openhab.binding.iotawatt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IoTaWattConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class IoTaWattConfiguration {
    private final Logger logger = LoggerFactory.getLogger(IoTaWattConfiguration.class);
    /**
     * The default refresh interval of the IoTaWatt device
     */
    public static final int REFRESH_INTERVAL_DEFAULT = 10;
    /**
     * The default of the request timeout
     */
    public static final long REQUEST_TIMEOUT_DEFAULT = 10;

    /**
     * Configuration parameters
     */
    public String hostname = "";
    /**
     * The request timeout in seconds when fetching data from the IoTaWatt device
     */
    public long requestTimeout = REQUEST_TIMEOUT_DEFAULT;
    /**
     * The refresh interval of the IoTaWatt device in seconds
     */
    public int refreshInterval = REFRESH_INTERVAL_DEFAULT;

    public boolean isValid() {
        if (hostname.trim().isBlank()) {
            logger.warn("Hostname is blank, please specify the hostname/IP address of IoTaWatt.");
            return false;
        }
        if (requestTimeout <= 0) {
            logger.warn("Invalid requestTimeout {}, please use a positive number", requestTimeout);
            return false;
        }
        if (refreshInterval <= 0) {
            logger.warn("Invalid refreshInterval {}, please use a positive number", refreshInterval);
            return false;
        }
        // Also update "configuration-error" in src/main/resources/OH-INF/i18n/iotawatt_en.properties
        return true;
    }
}

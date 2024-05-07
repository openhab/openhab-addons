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

/**
 * The {@link IoTaWattConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Peter Rosenberg - Initial contribution
 */
@NonNullByDefault
public class IoTaWattConfiguration {
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
}

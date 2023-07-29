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
package org.openhab.binding.ojelectronics.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The configuration for {@link org.openhab.binding.ojelectronics.internal.OJElectronicsCloudHandler}
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class OJElectronicsBridgeConfiguration {

    /**
     * Password
     */
    public String password = "";

    /**
     * Customer-ID
     */
    public int customerId = 1;

    /**
     * User Name
     */
    public String userName = "";

    /**
     * Url for API
     */
    public String apiUrl = "https://OWD5-OJ001-App.ojelectronics.com/api";

    /**
     * API-Key
     */
    public String apiKey = "";

    /**
     * Software Version
     */
    public int softwareVersion = 1060;

    /**
     * Refresh-Delay
     */
    public long refreshDelayInSeconds = 30;
}

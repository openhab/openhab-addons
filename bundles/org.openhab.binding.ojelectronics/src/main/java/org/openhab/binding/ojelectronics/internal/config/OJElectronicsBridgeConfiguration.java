/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.openhab.binding.ojelectronics.internal.OJElectronicsBridgeHandler;

/**
 * The configuration for {@link OJElectronicsBridgeHandler}
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
    public Integer customerId = 1;

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
    public String APIKEY = "f219aab4-9ac0-4343-8422-b72203e2fac9";

    /**
     * Software Version
     */
    public Integer softwareVersion = 1060;

    /**
     * Refresh-Delay
     */
    public long refreshDelayInSeconds = 30;
}

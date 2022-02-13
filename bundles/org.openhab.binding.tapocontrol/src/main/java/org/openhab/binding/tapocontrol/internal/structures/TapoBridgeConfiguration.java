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
package org.openhab.binding.tapocontrol.internal.structures;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TapoBridgeConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */

@NonNullByDefault
public final class TapoBridgeConfiguration {
    private final Logger logger = LoggerFactory.getLogger(TapoBridgeConfiguration.class);

    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_EMAIL = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";
    public static final String CONFIG_DISCOVERY_CLOUD = "cloudDiscovery";
    public static final String CONFIG_DISCOVERY_INTERVAL = "discoveryInterval";

    /* DEFAULT & FIXED CONFIGURATIONS */
    public static final Integer CONFIG_CLOUD_FIXED_INTERVAL = 1440;

    /* thing configuration parameter. */
    public String username = "";
    public String password = "";
    public Boolean cloudDiscoveryEnabled = false;
    public Boolean udpDiscoveryEnabled = false;
    public Integer cloudReconnectIntervalM = CONFIG_CLOUD_FIXED_INTERVAL;
    public Integer discoveryIntervalM = 30;

    private Thing bridge;

    /**
     * Create settings
     * 
     * @param thing BridgeThing
     */
    public TapoBridgeConfiguration(Thing thing) {
        this.bridge = thing;
        loadSettings();
    }

    /**
     * LOAD SETTINGS
     */
    public void loadSettings() {
        try {
            Configuration config = this.bridge.getConfiguration();
            username = config.get(CONFIG_EMAIL).toString();
            password = config.get(CONFIG_PASS).toString();
            cloudDiscoveryEnabled = Boolean.parseBoolean(config.get(CONFIG_DISCOVERY_CLOUD).toString());
            discoveryIntervalM = Integer.valueOf(config.get(CONFIG_DISCOVERY_INTERVAL).toString());
        } catch (Exception e) {
            logger.warn("{} error reading configuration: '{}'", bridge.getUID(), e.getMessage());
        }
    }
}

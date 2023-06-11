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
 * The {@link TapoDeviceConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */

@NonNullByDefault
public final class TapoDeviceConfiguration {
    private final Logger logger = LoggerFactory.getLogger(TapoDeviceConfiguration.class);

    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";

    /* thing configuration parameter. */
    public String ipAddress = "";
    public Integer pollingInterval = 30;

    private final Thing device;

    /**
     * Create settings
     * 
     * @param thing BridgeThing
     */
    public TapoDeviceConfiguration(Thing thing) {
        this.device = thing;
        loadSettings();
    }

    /**
     * LOAD SETTINGS
     */
    public void loadSettings() {
        try {
            Configuration config = this.device.getConfiguration();
            this.ipAddress = config.get(CONFIG_DEVICE_IP).toString();
            this.pollingInterval = Integer.valueOf(config.get(CONFIG_UPDATE_INTERVAL).toString());
        } catch (Exception e) {
            logger.warn("{} error reading device-configuration: '{}'", device.getUID().toString(), e.getMessage());
        }
    }
}

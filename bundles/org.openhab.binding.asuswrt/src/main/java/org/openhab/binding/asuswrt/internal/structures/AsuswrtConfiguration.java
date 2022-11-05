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
package org.openhab.binding.asuswrt.internal.structures;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AsuswrtConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtConfiguration {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtConfiguration.class);

    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_USER = "username";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_HOSTNAME = "hostname";
    public static final String CONFIG_UPDATE_INTERVAL = "refreshInterval";

    /* THING CONFIGUTATION PARAMETERS */
    public String hostname = "";
    public String username = "";
    public String password = "";
    public String url = "";
    public int refreshInterval = 60;
    public boolean autoDiscoveryEnabled = false;

    private Thing router;

    /**
     * INIT CLASS
     * 
     * @param thing
     */
    public AsuswrtConfiguration(Thing thing) {
        router = thing;
        loadSettings();
    }

    /**
     * LOAD SETTINGS
     */
    public void loadSettings() {
        logger.trace("loading settings");
        try {
            Configuration config = this.router.getConfiguration();
            this.hostname = config.get(CONFIG_HOSTNAME).toString();
            this.username = config.get(CONFIG_USER).toString();
            this.password = config.get(CONFIG_PASS).toString();
            this.url = HTTP_PROTOCOL + hostname;
            this.refreshInterval = Integer.valueOf(config.get(CONFIG_UPDATE_INTERVAL).toString());
        } catch (Exception e) {
            logger.warn("{} error reading device-configuration: '{}'", router.getUID(), e.getMessage());
        }
    }
}

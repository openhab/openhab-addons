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
    public int pollingInterval = 20;
    public int reconnectInterval = 60;
    public int discoveryInterval = 3600;
    public boolean autoDiscoveryEnabled = false;

    private Thing router;
}

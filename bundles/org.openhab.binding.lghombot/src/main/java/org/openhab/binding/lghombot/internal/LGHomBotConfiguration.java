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
package org.openhab.binding.lghombot.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lghombot.internal.discovery.LGHomBotDiscovery;

/**
 * The {@link LGHomBotConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class LGHomBotConfiguration {

    /**
     * Constant field used in {@link LGHomBotDiscovery} to set the configuration property during discovery. Value of
     * this field needs to match {@link #ipAddress}
     */
    public static final String IP_ADDRESS = "ipAddress";

    /**
     * IP Address (or host name) of HomBot
     */
    public String ipAddress = "";

    /**
     * Port used by the HomBot
     */
    public int port = LGHomBotBindingConstants.DEFAULT_HOMBOT_PORT;

    /**
     * Polling time (in seconds) to refresh state from the HomBot itself.
     */
    public int pollingPeriod = 3;
}

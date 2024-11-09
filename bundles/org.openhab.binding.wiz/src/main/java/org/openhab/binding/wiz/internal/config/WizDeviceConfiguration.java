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
package org.openhab.binding.wiz.internal.config;

import static org.openhab.binding.wiz.internal.WizBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BondHomeConfiguration} class contains fields mapping thing
 * configuration parameters.
 *
 * @author Sara Geleskie Damiano - Initial contribution
 */
@NonNullByDefault
public class WizDeviceConfiguration {

    /**
     * Configuration for a WiZ Device
     */
    public String macAddress = "";
    public String ipAddress = "";
    public long updateInterval = DEFAULT_REFRESH_INTERVAL_SEC;
    public boolean useHeartBeats = false; // true: register to get 5s heart-beats
    public long reconnectInterval = DEFAULT_RECONNECT_INTERVAL_MIN;
}

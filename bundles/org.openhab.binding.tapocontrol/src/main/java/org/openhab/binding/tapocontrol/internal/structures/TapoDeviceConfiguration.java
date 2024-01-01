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
package org.openhab.binding.tapocontrol.internal.structures;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link TapoDeviceConfiguration} class contains fields mapping bridge configuration parameters.
 *
 * @author Christian Wild - Initial contribution
 */

@NonNullByDefault
public final class TapoDeviceConfiguration {
    /* THING CONFIGUTATION PROPERTYS */
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";

    /* thing configuration parameter. */
    public String ipAddress = "";
    public int pollingInterval = 30;
}

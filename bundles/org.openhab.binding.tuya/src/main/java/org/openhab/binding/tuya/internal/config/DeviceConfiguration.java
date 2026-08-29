/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tuya.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DeviceConfiguration} holds the configuration of a single device
 *
 * @author Jan N. Klug - Initial contribution
 * @author Maciej Jarzebowski - Add sub-device node id
 */
@NonNullByDefault
public class DeviceConfiguration {
    public String productId = "";
    public String deviceId = "";
    public String localKey = "";

    /**
     * The node id of a sub-device connected through a gateway. Empty for devices that are reached directly.
     */
    public String subDeviceId = "";

    public String ip = "";
    public int port = 6668;
    public String protocol = "";

    public int pollingInterval = 0;
}

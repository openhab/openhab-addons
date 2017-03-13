/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome.internal.motionsensor;

/**
 * The {@link DLinkMotionSensorConfig} provides configuration data
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkMotionSensorConfig {

    /**
     * Hardware version found during discovery
     */
    public static final String HARDWARE_VERSION = "hardwareVersion";

    /**
     * Constants representing the configuration strings
     */
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PIN = "pin";
    public static final String REBOOT_TIME = "rebootTime";
    public static final String REBOOT_INTERVAL = "rebootInterval";

    /**
     * The IP address of the device
     */
    public String ipAddress;

    /**
     * The pin code of the device
     */
    public String pin;

    /**
     * The time the device will be rebooted for the first time each day
     */
    public String rebootTime;

    /**
     * The number of hours after which the device will be rebooted
     */
    public Integer rebootInterval;
}

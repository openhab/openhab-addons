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
package org.openhab.binding.dlinksmarthome.internal.motionsensor;

/**
 * The {@link DLinkMotionSensorConfig} provides configuration data
 *
 * @author Mike Major - Initial contribution
 */
public class DLinkMotionSensorConfig {

    /**
     * Constants representing the configuration strings
     */
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PIN = "pin";

    /**
     * The IP address of the device
     */
    public String ipAddress;

    /**
     * The pin code of the device
     */
    public String pin;
}

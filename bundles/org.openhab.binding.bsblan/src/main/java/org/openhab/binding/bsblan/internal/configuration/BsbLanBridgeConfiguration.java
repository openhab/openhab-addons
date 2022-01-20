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
package org.openhab.binding.bsblan.internal.configuration;

/**
 * The {@link BsbLanBridgeConfiguration} is the class used to match the
 * bridge configuration.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanBridgeConfiguration {
    /**
     * Hostname or IP address of the device
     */
    public String host;

    /**
     * HTTP port where device is listening
     */
    public Integer port;

    /**
     * For "security" feature of BSB-LAN devices
     */
    public String passkey;

    /**
     * HTTP Basic Authentication User
     */
    public String username;

    /**
     * HTTP Basic Authentication Password
     */
    public String password;

    /**
     * Value refresh interval
     */
    public Integer refreshInterval;
}

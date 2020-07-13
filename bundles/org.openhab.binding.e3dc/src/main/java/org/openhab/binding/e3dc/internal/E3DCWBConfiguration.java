/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal;

/**
 * The {@link E3DCWBConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
public class E3DCWBConfiguration {

    /**
     * IP Address
     */
    public String ipAddress;

    /**
     * Port
     */
    public int port;

    /**
     * Wallbox number
     */
    public int number;

    /**
     * Refresh interval in seconds
     */
    public long refreshInterval_sec = 2;
}

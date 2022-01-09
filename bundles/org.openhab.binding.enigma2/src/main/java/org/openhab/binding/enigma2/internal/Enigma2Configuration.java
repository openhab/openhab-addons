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
package org.openhab.binding.enigma2.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link Enigma2Configuration} class contains fields mapping thing configuration parameters.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2Configuration {

    /**
     * Hostname or IP address of the Enigma2 device.
     */
    public String host = "";
    /**
     * The refresh interval in seconds.
     */
    public int refreshInterval = 5;
    /**
     * The refresh interval in seconds.
     */
    public int timeout = 5;
    /**
     * The Username of the Enigma2 Web API.
     */
    public String user = "";
    /**
     * The Password of the Enigma2 Web API.
     */
    public String password = "";
}

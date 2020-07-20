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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link E3DCDeviceConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCDeviceConfiguration {

    /**
     * Host Address
     */
    public String host = "undefined";

    /**
     * Port
     */
    public int deviceid = 1;

    /**
     * Port
     */
    public int port = -1;

    /**
     * Refresh interval in ms
     */
    public long refresh = 2000;
}

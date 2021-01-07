/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.config;

import org.openhab.binding.cul.internal.CULBindingConstants;

/**
 * Configuration class for {@link CULBindingConstants} bridge used to connect to the
 * cul device.
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */

public class MaxCULBridgeConfiguration {

    /**
     * Name of the CUL device serial port
     */
    public String serialPort;

    /**
     * Serial port baudrate.
     */
    public Integer baudrate;

    /**
     * Serial port parity.
     */
    public String parity;

    /**
     * Set timezone you want the units to be set to.
     */
    public String timezone;
}

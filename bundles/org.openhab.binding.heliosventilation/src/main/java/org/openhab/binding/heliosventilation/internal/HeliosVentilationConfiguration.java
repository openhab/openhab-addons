/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HeliosVentilationConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationConfiguration {

    /**
     * Port name for a serial connection to RS485 bus. Valid values are e.g. COM1 for Windows and /dev/ttyS0 or
     * /dev/ttyUSB0 for Linux.
     */
    public String serialPort = "";

    /**
     * The Panel Poll Period. Default is 60 sec. = 1 minute;
     */
    public int pollPeriod = 60;
}

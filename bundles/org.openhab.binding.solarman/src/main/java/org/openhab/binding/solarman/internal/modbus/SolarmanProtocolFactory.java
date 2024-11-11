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
package org.openhab.binding.solarman.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarman.internal.SolarmanLoggerConfiguration;

/**
 * @author Peter Kretz - Added RAW Modbus for LAN Stick
 */
@NonNullByDefault
public class SolarmanProtocolFactory {

    public static SolarmanProtocol createSolarmanProtocol(SolarmanLoggerConfiguration solarmanLoggerConfiguration) {
        switch (solarmanLoggerConfiguration.getSolarmanLoggerMode()) {
            case RAWMODBUS: {
                return new SolarmanRawProtocol(solarmanLoggerConfiguration);
            }
            case V5MODBUS:
            default:
                return new SolarmanV5Protocol(solarmanLoggerConfiguration);
        }
    }
}

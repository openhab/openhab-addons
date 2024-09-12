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
package org.openhab.binding.pentair.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link PentairSerialBridgeConfig } class contains the configuration parameters for Serial Bridge
 *
 * @author Jeff James - initial contribution
 *
 */
@NonNullByDefault
public class PentairSerialBridgeConfig {
    /** path or name of serial port, usually /dev/ttyUSB0 format for linux/mac, COM1 for windows */
    public String serialPort = "";
}

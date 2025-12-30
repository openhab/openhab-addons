/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mideaac.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link A1CommandBase} builds the base command frame for A1-style devices.
 *
 * @author Bob Eckhoff - Initial contribution
 */
@NonNullByDefault
public class A1CommandBase extends CommandBase {
    /**
     * Base class for A1-style commands
     */
    public A1CommandBase() {
        super(); // build the AC-style base frame then modify for A1
        data[2] = (byte) 0xa1; // override device type byte
        data[14] = (byte) 0x00; // override 0x03 byte for poll
        data[15] = (byte) 0x00; // override 0xff byte for poll
        data[17] = (byte) 0x00; // override 0x02 byte for poll
    }
}

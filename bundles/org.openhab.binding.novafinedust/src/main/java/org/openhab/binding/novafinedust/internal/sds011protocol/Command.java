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
package org.openhab.binding.novafinedust.internal.sds011protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class holding the command constants to be send to the sensor in the first data byte
 *
 * @author Stefan Triller - Initial contribution
 *
 */
@NonNullByDefault
public class Command {

    private Command() {
    }

    public static final byte MODE = 2;
    public static final byte REQUEST_DATA = 4;
    public static final byte HARDWARE_ID = 5;
    public static final byte SLEEP = 6;
    public static final byte FIRMWARE = 7;
    public static final byte WORKING_PERIOD = 8;
}

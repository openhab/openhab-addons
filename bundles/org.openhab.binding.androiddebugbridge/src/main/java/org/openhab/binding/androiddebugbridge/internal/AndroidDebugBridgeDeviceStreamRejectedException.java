/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.androiddebugbridge.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown when opening the adb shell stream fails, as a device in standby commonly causes.
 *
 * Distinct from a failure while reading an already-open stream, where the command certainly
 * reached the device. It is not proof the command never ran though: adblib writes the OPEN packet
 * inside {@code AdbConnection.open()}, so delivery is ambiguous if that write fails.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeDeviceStreamRejectedException extends AndroidDebugBridgeDeviceException {
    private static final long serialVersionUID = 5471982041566281957L;

    public AndroidDebugBridgeDeviceStreamRejectedException(String message) {
        super(message);
    }
}

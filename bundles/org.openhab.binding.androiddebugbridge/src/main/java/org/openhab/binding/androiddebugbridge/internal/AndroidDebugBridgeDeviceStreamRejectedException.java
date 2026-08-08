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
 * Thrown when opening the adb shell stream fails, which a device in standby commonly causes.
 *
 * It is a distinct type so the caller can tell this apart from a failure that happened while
 * reading an already-open stream, where the command certainly reached the device. It is however
 * <em>not</em> proof that the command never ran: adblib writes the OPEN packet inside
 * {@code AdbConnection.open()}, so an {@link java.io.IOException} raised while sending that packet
 * leaves delivery ambiguous. Callers must therefore only repeat commands that stay correct when
 * executed twice.
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

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
package org.openhab.binding.dreamscreen.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * {@link DreamScreenMode} defines the enum for Device Modes.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public enum DreamScreenMode {
    VIDEO(1),
    MUSIC(2),
    AMBIENT(3);

    public final byte deviceMode;

    private DreamScreenMode(int deviceMode) {
        this.deviceMode = (byte) deviceMode;
    }

    public static @Nullable DreamScreenMode fromDevice(byte value) {
        return value > 0 ? DreamScreenMode.values()[value - 1] : null;
    }

    public static DreamScreenMode fromState(DecimalType command) {
        return DreamScreenMode.values()[command.intValue()];
    }

    public DecimalType state() {
        return new DecimalType(this.ordinal());
    }
}

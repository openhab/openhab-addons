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

import static org.openhab.binding.dreamscreen.internal.DreamScreenBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * {@link DreamScreenMode} defines the enum for Device Modes.
 *
 * @author Bruce Brouwer - Initial contribution
 */
@NonNullByDefault
public enum DreamScreenMode {
    VIDEO(MODE_VIDEO, 1),
    MUSIC(MODE_MUSIC, 2),
    AMBIENT(MODE_AMBIENT, 3);

    public final String name;
    public final byte deviceMode;

    private DreamScreenMode(String name, int deviceMode) {
        this.name = name;
        this.deviceMode = (byte) deviceMode;
    }

    public static @Nullable DreamScreenMode fromDevice(byte value) {
        if (value == VIDEO.deviceMode) {
            return VIDEO;
        } else if (value == MUSIC.deviceMode) {
            return MUSIC;
        } else if (value == AMBIENT.deviceMode) {
            return AMBIENT;
        }
        return null;
    }

    public static DreamScreenMode fromState(StringType command) {
        String mode = command.toString().toLowerCase();
        switch (mode) {
            case MODE_VIDEO:
                return VIDEO;
            case MODE_MUSIC:
                return MUSIC;
            case MODE_AMBIENT:
                return AMBIENT;
        }
        throw new IllegalArgumentException("Invalid Mode value: " + mode);
    }

    public StringType state() {
        return new StringType(name);
    }
}

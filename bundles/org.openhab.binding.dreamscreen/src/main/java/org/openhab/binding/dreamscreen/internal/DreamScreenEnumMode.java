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
package org.openhab.binding.dreamscreen.internal;

import org.eclipse.smarthome.core.library.types.DecimalType;

/**
 * The {@link DreamScreenMode} defines the light modes
 *
 * @author Bruce Brouwer - Initial contribution
 */
enum DreamScreenEnumMode {
    VIDEO(1),
    MUSIC(2),
    AMBIENT(3);

    final byte deviceMode;

    private DreamScreenEnumMode(int deviceMode) {
        this.deviceMode = (byte) deviceMode;
    }

    public static DreamScreenEnumMode fromDevice(byte value) {
        return value > 0 ? DreamScreenEnumMode.values()[value - 1] : null;
    }

    public static DreamScreenEnumMode fromState(DecimalType command) {
        return DreamScreenEnumMode.values()[command.intValue()];
    }

    public DecimalType state() {
        return new DecimalType(this.ordinal());
    }
}
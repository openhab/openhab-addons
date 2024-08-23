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
package org.openhab.binding.hdpowerview.internal.dto;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Shade data for a single Shade, as returned by an HD PowerView hub
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShadeData {
    public int id;
    public @Nullable String name;
    public int roomId;
    public int groupId;
    public int order;
    public int type;
    public double batteryStrength;
    public int batteryStatus;
    public boolean batteryIsLow;
    public @Nullable ShadePosition positions;
    public @Nullable Boolean timedOut;
    public int signalStrength;
    public @Nullable Integer capabilities;
    public @Nullable Firmware firmware;
    public @Nullable Firmware motor;
    // note: in old JSON batteryKind was a string but now it's a number; fortunately GSON string accepts either
    public @Nullable String batteryKind;

    public String getName() {
        return new String(Base64.getDecoder().decode(name));
    }

    public BatteryKind getBatteryKind() {
        return BatteryKind.fromString(batteryKind);
    }
}

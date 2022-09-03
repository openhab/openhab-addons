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
package org.openhab.binding.hdpowerview.internal.api;

import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract class for state of a Shade as returned by an HD PowerView hub.
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Refactored into separate class
 */
@NonNullByDefault
public abstract class ShadeData {
    // fields common to Generation 1/2 and 3 hubs
    public int id;
    public @Nullable String name;
    public int roomId;
    public int type;
    public int batteryStatus;
    public @Nullable ShadePosition positions;
    public int signalStrength;
    public @Nullable Integer capabilities;
    public @Nullable Firmware firmware;

    public String getName() {
        return new String(Base64.getDecoder().decode(name));
    }

    public abstract BatteryKind getBatteryKind();

    public abstract int version();
}

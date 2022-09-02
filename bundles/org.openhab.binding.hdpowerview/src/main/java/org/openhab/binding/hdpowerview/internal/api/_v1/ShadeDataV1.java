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
package org.openhab.binding.hdpowerview.internal.api._v1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.BatteryKind;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.ShadeData;

/**
 * State of a Shade as returned by an HD PowerView hub of Generation 1 or 2.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataV1 extends ShadeData {
    // fields for Generation 1/2 hubs only; NOTE: all these are Nullable
    public int groupId;
    public int order;
    public double batteryStrength;
    public boolean batteryIsLow;
    public @Nullable Boolean timedOut;
    public @Nullable Firmware motor;
    // note: in old JSON batteryKind was a string but now it's a number; fortunately GSON string accepts either
    public @Nullable String batteryKind;

    @Override
    public BatteryKind getBatteryKind() {
        return BatteryKind.fromString(batteryKind);
    }

    @Override
    public int version() {
        return 1;
    }
}

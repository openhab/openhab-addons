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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * State of all Shades, as returned by an HD PowerView hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class Shades {

    public @Nullable List<ShadeData> shadeData;
    public @Nullable List<Integer> shadeIds;

    /*
     * the following SuppressWarnings annotation is because the Eclipse compiler
     * does NOT expect a NonNullByDefault annotation on the inner class, since it is
     * implicitly inherited from the outer class, whereas the Maven compiler always
     * requires an explicit NonNullByDefault annotation on all classes
     */
    @SuppressWarnings("null")
    @NonNullByDefault
    public static class ShadeData {
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

        public String getName() {
            return new String(Base64.getDecoder().decode(name));
        }
    }
}

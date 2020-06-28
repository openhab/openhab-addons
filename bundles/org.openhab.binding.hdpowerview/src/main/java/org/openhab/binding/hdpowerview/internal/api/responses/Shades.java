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
package org.openhab.binding.hdpowerview.internal.api.responses;

import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;

/**
 * A list of Shades, as returned by the HD Power View Hub
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class Shades {

    public @Nullable List<ShadeData> shadeData;
    public @Nullable List<String> shadeIds;

    @SuppressWarnings("null")
    @NonNullByDefault
    public static class ShadeData {
        public @Nullable String id;
        public @Nullable String name;
        public int roomId;
        public int groupId;
        public int order;
        public int type;
        public double batteryStrength;
        public int batteryStatus;
        public boolean batteryIsLow;
        public @Nullable ShadePosition positions;

        public String getName() {
            return new String(Base64.getDecoder().decode(name));
        }
    }
}

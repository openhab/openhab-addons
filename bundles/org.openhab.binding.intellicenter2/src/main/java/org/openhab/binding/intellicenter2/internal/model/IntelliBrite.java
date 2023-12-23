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
package org.openhab.binding.intellicenter2.internal.model;

import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.ACT;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.FEATR;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.LISTORD;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.MODE;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.STATUS;
import static org.openhab.binding.intellicenter2.internal.protocol.Attribute.USE;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.intellicenter2.internal.protocol.Attribute;
import org.openhab.binding.intellicenter2.internal.protocol.RequestObject;
import org.openhab.binding.intellicenter2.internal.protocol.ResponseObject;

import com.google.gson.annotations.SerializedName;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class IntelliBrite extends Circuit {

    private static final List<Attribute> REQUEST_ATTRIBUTES = List.of(STATUS, MODE, LISTORD, FEATR, USE, ACT);

    public IntelliBrite() {
        super(REQUEST_ATTRIBUTES);
    }

    public IntelliBrite(ResponseObject response) {
        super(REQUEST_ATTRIBUTES, response);
    }

    public static RequestObject createRefreshRequest(String objectName) {
        return new RequestObject(objectName, REQUEST_ATTRIBUTES);
    }

    // borrowed from
    // https://github.com/dustindclark/homebridge-pentair-intellicenter/blob/676b7dab2fbf5107443678c3ef5bc271108941f8/src/types.ts
    @NonNullByDefault
    public enum Color {

        @SerializedName("WHITER")
        WHITE("WHITER", 0, 0),
        @SerializedName("REDR")
        RED("REDR", 0, 100),
        @SerializedName("GREENR")
        GREEN("GREENR", 120, 100),
        @SerializedName("BLUER")
        BLUE("BLUER", 240, 100),
        @SerializedName("MAGNTAR")
        MAGENTA("MAGNTAR", 300, 100);

        public final String intellicenterCode;
        public final int hue;
        public final int saturation;

        public static Color from(int hue, int saturation) {
            if (saturation > ((RED.saturation - WHITE.saturation) / 2)) {
                if (hue < ((GREEN.hue - RED.hue) / 2 + RED.hue)) {
                    return RED;
                } else if (hue < ((BLUE.hue - GREEN.hue) / 2 + GREEN.hue)) {
                    return GREEN;
                } else if (hue < ((MAGENTA.hue - BLUE.hue) / 2 + BLUE.hue)) {
                    return BLUE;
                } else {
                    return MAGENTA;
                }
            }
            return WHITE;
        }

        Color(String intellicenterCode, int hue, int saturation) {
            this.intellicenterCode = intellicenterCode;
            this.hue = hue;
            this.saturation = saturation;
        }
    }

    @NonNull
    public Color getColor() {
        return getValueAsEnum(USE, Color.class);
    }
}

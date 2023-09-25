/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Utility class for generating some UIDs.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class UidUtils {

    public static String sanetizeId(String st) {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < st.length(); i++) {
            char c = st.charAt(i);
            if (c == 130) {

            } else if (c >= 232 && c <= 234) {
                c = 'e';
            } else if (c >= 200 && c <= 202) {
                c = 'E';
            }

            else if (c >= 236 && c <= 239) {
                c = 'i';
            } else if (c >= 204 && c <= 207) {
                c = 'I';
            }

            else if (c >= 242 && c <= 246) {
                c = 'o';
            } else if (c >= 249 && c <= 252) {
                c = 'u';
            } else if (c >= 217 && c <= 220) {
                c = 'U';
            }

            else if (c >= 224 && c <= 229) {
                c = 'a';
            } else if (c == 192 && c <= 197) {
                c = 'A';
            }

            else if (c == 199) {
                c = 'c';
            } else if (c == 231) {
                c = 'C';
            }

            else if (c == '_') {
                c = '_';
            } else if (c == ' ') {
                c = '_';
            } else if (c == '&') {
                c = '_';
            } else if (c == '/') {
                c = '_';
            } else if (c == '.') {
                c = '_';
            } else if (c == '(') {
                c = '_';
            } else if (c == ')') {
                c = '_';
            }

            else if (c == 248) {
                c = '_';
            } else if (c == '\'') {
                c = '_';
            }

            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
            } else {
                c = '_';
            }

            buffer.append(c);
        }

        return buffer.toString();
    }

    /**
     * Generates the ThingTypeUID for the given device. If it's a Homegear device, add a prefix because a Homegear
     * device has more datapoints.
     */
    public static ThingTypeUID generateThingTypeUID(Object device) {
        // String type = sanetizeId(device.getType());
        return new ThingTypeUID(SmartthingsBindingConstants.BINDING_ID, "type");
    }

    public static ThingTypeUID generateThingTypeUID(String name) {
        return new ThingTypeUID(SmartthingsBindingConstants.BINDING_ID, name);
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(Object dpt) {

        /*
         * String type = dpt.getDptType();
         * String id = "";
         *
         * if (SmartthingsBindingConstants.DPT_TYPE_ENUM.equals(type)) {
         * StringBuilder builder = new StringBuilder();
         * int idx = 0;
         * for (SmartthingsMetadataPointChild child : dpt.getChild()) {
         *
         * if (idx > 0) {
         * builder.append("_");
         * }
         *
         * String opt = child.getText();
         * String[] subParts = opt.split(" ");
         * for (String subPart : subParts) {
         * if (subPart.length() > 0) {
         * builder.append(subPart.charAt(0));
         * }
         * }
         * idx++;
         *
         * }
         * String token = sanetizeId(builder.toString());
         *
         * id = String.format("%s_%s", type, token);
         * } else if (SmartthingsBindingConstants.DPT_TYPE_NUMERIC.equals(type)) {
         * id = sanetizeId(String.format("%s_%s_%s_%s_%s_%s", type, dpt.getDptUnit(), dpt.getMin(), dpt.getMax(),
         * dpt.getFieldWitdh(), dpt.getResolution()));
         * } else if (SmartthingsBindingConstants.DPT_TYPE_STRING.equals(type)) {
         * id = String.format("%s_%s", type, dpt.getMaxLength());
         * } else {
         * id = String.format("%s", dpt.getDptType());
         * }
         *
         * // dpt.Display();
         */
        return new ChannelTypeUID(SmartthingsBindingConstants.BINDING_ID, "id");
    }

    /**
     * Generates the ThingUID for the given device in the given bridge.
     */
    public static ThingUID generateThingUID(Bridge bridge) {
        ThingTypeUID thingTypeUID = generateThingTypeUID("");
        return new ThingUID(thingTypeUID, bridge.getUID(), "");
    }

    /**
     * Generates the ChannelUID for the given datapoint with channelNumber and datapointName.
     */
    public static ChannelUID generateChannelUID(ThingUID thingUID) {
        return new ChannelUID(thingUID, "");
        // String.valueOf(dp.getChannel().getNumber()), dp.getName());
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType and channelNumber.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(Object menu) {
        return new ChannelGroupTypeUID(SmartthingsBindingConstants.BINDING_ID, String.format("%s", "menu.getId()"));
    }
}

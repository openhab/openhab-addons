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
package org.openhab.binding.siemenshvac.internal.type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDevice;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataMenu;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
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

            } else if (c == 131) {
                c = 'e';
            } else if (c == 136) {
                c = 'e';
            } else if (c == 137) {
                c = 'e';
            } else if (c == 144) {
                c = 'E';
            } else if (c == 212) {
                c = 'E';
            }

            else if (c == 140) {
                c = 'i';
            } else if (c == 139) {
                c = 'i';
            } else if (c == 161) {
                c = 'i';
            } else if (c == 141) {
                c = 'i';
            }

            else if (c == 147) {
                c = 'o';
            } else if (c == 162) {
                c = 'o';
            } else if (c == 149) {
                c = 'o';
            } else if (c == 148) {
                c = 'o';
            }

            else if (c == 163) {
                c = 'u';
            } else if (c == 151) {
                c = 'u';
            } else if (c == 150) {
                c = 'u';
            } else if (c == 129) {
                c = 'u';
            } else if (c == 233) {
                c = 'U';
            }

            else if (c == 133) {
                c = 'a';
            } else if (c == 132) {
                c = 'a';
            } else if (c == 131) {
                c = 'a';
            } else if (c == 160) {
                c = 'a';
            }

            else if (c == 135) {
                c = 'c';
            } else if (c == 128) {
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
    public static ThingTypeUID generateThingTypeUID(SiemensHvacMetadataDevice device) {
        String type = sanetizeId(device.getType());
        return new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, type);
    }

    public static ThingTypeUID generateThingTypeUID(String name) {
        return new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, name);
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(SiemensHvacMetadataDataPoint dpt) {

        String shortDesc = sanetizeId(dpt.getShortDesc());
        return new ChannelTypeUID(SiemensHvacBindingConstants.BINDING_ID,
                String.format("%s_%s_%s", dpt.getDptType(), dpt.getId(), shortDesc));
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
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(SiemensHvacMetadataMenu menu) {
        return new ChannelGroupTypeUID(SiemensHvacBindingConstants.BINDING_ID, String.format("%s", menu.getId()));
    }
}

/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
public class UidUtils {

    public static String sanetizeId(String st) {

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < st.length(); i++) {
            char c = st.charAt(i);

            if (c == 'é') {
                c = 'e';
            } else if (c == 'è') {
                c = 'e';
            } else if (c == 'ê') {
                c = 'e';
            } else if (c == 'ë') {
                c = 'e';
            } else if (c == 'ě') {
                c = 'e';
            } else if (c == 'É') {
                c = 'E';
            } else if (c == 'É') {
                c = 'E';
            }

            else if (c == 'î') {
                c = 'i';
            } else if (c == 'ï') {
                c = 'i';
            } else if (c == 'í') {
                c = 'i';
            } else if (c == 'í') {
                c = 'i';
            }

            else if (c == 'ô') {
                c = 'o';
            } else if (c == 'ó') {
                c = 'o';
            } else if (c == 'ò') {
                c = 'o';
            } else if (c == 'ö') {
                c = 'o';
            }

            else if (c == 'ú') {
                c = 'u';
            } else if (c == 'ù') {
                c = 'u';
            } else if (c == 'û') {
                c = 'u';
            } else if (c == 'ü') {
                c = 'u';
            } else if (c == 'ů') {
                c = 'u';
            } else if (c == 'Ú') {
                c = 'U';
            }

            else if (c == 'à') {
                c = 'a';
            } else if (c == 'ä') {
                c = 'a';
            } else if (c == 'â') {
                c = 'a';
            } else if (c == 'á') {
                c = 'a';
            }

            else if (c == 'ř') {
                c = 'r';
            } else if (c == 'ť') {
                c = 't';
            }

            else if (c == 'š') {
                c = 's';
            }

            else if (c == 'ý') {
                c = 'y';
            } else if (c == 'ÿ') {
                c = 'y';
            }

            else if (c == 'ž') {
                c = 'z';
            }

            else if (c == 'ç') {
                c = 'c';
            } else if (c == 'č') {
                c = 'c';
            } else if (c == 'Č') {
                c = 'C';
            }

            else if (c == 'Ž') {
                c = 'Z';
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

            else if (c == '°') {
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

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
package org.openhab.binding.siemenshvac.internal.type;

import java.text.Normalizer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.converter.ConverterFactory;
import org.openhab.binding.siemenshvac.internal.converter.ConverterTypeException;
import org.openhab.binding.siemenshvac.internal.converter.TypeConverter;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDevice;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataMenu;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Utility class for generating some UIDs.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class UidUtils {
    /**
     * The methods remove specific local character (like 'é'/'ê','â') so we have a correctly formated UID from a
     * localize item label
     *
     * @param label
     * @return the label without invalid character
     */
    public static String sanetizeId(String label) {
        String result = label;

        if (!Normalizer.isNormalized(label, Normalizer.Form.NFKD)) {
            result = Normalizer.normalize(label, Normalizer.Form.NFKD);
            result = result.replaceAll("\\p{M}", "");
        }

        result = result.replaceAll("[^a-zA-Z0-9_]", "-").toLowerCase();

        return result;
    }

    /**
     * Generates the ThingTypeUID for the given device. If it's a Homegear device, add a prefix because a Homegear
     * device has more datapoints.
     */
    public static ThingTypeUID generateThingTypeUID(SiemensHvacMetadataDevice device) {
        String type = sanetizeId(device.getType());
        return new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, type);
    }

    /**
     * get a more user friendly description from English short descriptor
     *
     * @param descriptor
     * @return
     */
    private static String normalizeDescriptor(String descriptor) {
        String result = descriptor.trim();

        if (result.indexOf("CC") >= 0 || result.indexOf("HC") >= 0) {
            for (int idx = 0; idx < 4; idx++) {
                result = result.replace("CC" + idx, "CC");
                result = result.replace("HC" + idx, "HC");
            }
        }

        result = result.toLowerCase();

        if (result.indexOf("history") >= 0) {
            for (int idx = 0; idx < 20; idx++) {
                result = result.replace("history " + idx, "history");
            }
        }

        result = result.replace(" mon", "");
        result = result.replace(" tue", "");
        result = result.replace(" wed", "");
        result = result.replace(" thu", "");
        result = result.replace(" fri", "");
        result = result.replace(" sat", "");
        result = result.replace(" sun", "");
        result = result.replace(" mo", "");
        result = result.replace(" tu", "");
        result = result.replace(" we", "");
        result = result.replace(" th", "");
        result = result.replace(" fr", "");
        result = result.replace(" sa", "");
        result = result.replace(" su", "");

        if (result.indexOf("holidays") >= 0) {
            if (result.indexOf("firstd") >= 0) {
                result = "holidays-hc-firstd";
            }
            if (result.indexOf("lastd") >= 0) {
                result = "holidays-hc-lastd";
            }
        }

        result = result.replace("---", "-");
        result = result.replace("--", "-");
        result = result.replace('\'', '-');
        result = result.replace('/', '-');
        result = result.replace(' ', '-');
        result = result.replace("+", "-");

        result = result.replace("standard-tsp-hc", "time-switch-program-standard");
        result = result.replace("standard-tsp-4", "time-switch-program-standard");
        result = result.replace("tsp-3", "time-switch-program-day");
        result = result.replace("tsp-4", "time-switch-program-day");
        result = result.replace("setpointtemp", "setpoint-temp-");
        result = result.replace("rmtmp", "roomtemp");
        result = result.replace("roomtempfrostprot", "room-temp-frostprot-");
        result = result.replace("-setp", "-setpoint");
        result = result.replace("optg", "operating-");
        result = result.replace("-comf", "-comfort");
        result = result.replace("-red", "-reduce");
        result = result.replace("setp-", "-setpoint");
        result = result.replace("roomtemp-", "room-temp-");
        result = result.replace("-setpointhc", "-setpoint-hc");
        result = result.replace("setphc", "-setpoint-hc");

        return result;
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(SiemensHvacMetadataDataPoint dpt) throws SiemensHvacException {
        String type = dpt.getDptType();
        String shortDesc = dpt.getShortDescEn();
        String result = normalizeDescriptor(shortDesc);

        try {
            TypeConverter tp = ConverterFactory.getConverter(type);
            if (!tp.hasVariant()) {
                result = tp.getChannelType(dpt);
            }
        } catch (ConverterTypeException ex) {
            throw new SiemensHvacException(String.format("Can't find converter for type: %s", type), ex);
        }

        return new ChannelTypeUID(SiemensHvacBindingConstants.BINDING_ID, result);
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType and channelNumber.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(SiemensHvacMetadataMenu menu) {
        return new ChannelGroupTypeUID(SiemensHvacBindingConstants.BINDING_ID, String.valueOf(menu.getId()));
    }
}

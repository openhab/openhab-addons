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
package org.openhab.binding.smartthings.internal.converter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Special converter for Smartthings hue values in the 0-100% range. OH2 uses 0-360 degrees
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsHue100Converter extends SmartthingsConverter {

    private Logger logger = LoggerFactory.getLogger(SmartthingsHue100Converter.class);

    public SmartthingsHue100Converter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String jsonMsg;

        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            double hue = hsb.getHue().doubleValue() / 3.60;
            String value = String.format("[%.0f, %d, %d ]", hue, hsb.getSaturation().intValue(),
                    hsb.getBrightness().intValue());
            // debug message
            String logMsg = String.format("OpenHAB HSB = %s, Smartthings HSB = %s, RGB = #%6X (%.0f, %.0f, %.0f)",
                    hsb.toString(), value, hsb.getRGB(), hsb.getRed().doubleValue() * 3.6,
                    hsb.getGreen().doubleValue() * 3.6, hsb.getBlue().doubleValue() * 3.6);
            logger.debug("{}", logMsg);

            jsonMsg = String.format(
                    "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\", \"value\": %s}",
                    thingTypeId, smartthingsName, channelUid.getId(), value);

        } else {
            jsonMsg = defaultConvertToSmartthings(channelUid, command);
        }

        return jsonMsg;
    }

    @Override
    public State convertToOpenHab(@Nullable String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        // Here we have to multiply the value from Smartthings by 3.6 to convert from 0-100 to 0-360
        String deviceType = dataFromSmartthings.capabilityAttribute;
        Object deviceValue = dataFromSmartthings.value;

        if (deviceValue == null) {
            logger.warn("Failed to convert Number {} because Smartthings returned a null value.", deviceType);
            return UnDefType.UNDEF;
        }

        if ("Number".contentEquals(acceptedChannelType)) {
            if (deviceValue instanceof String) {
                double d = Double.parseDouble((String) deviceValue);
                d *= 3.6;
                return new DecimalType(d);
            } else if (deviceValue instanceof Double) {
                double d = ((Double) deviceValue).doubleValue();
                d *= 3.6;
                return new DecimalType(d);
            } else if (deviceValue instanceof Long) {
                double d = ((Long) deviceValue).longValue();
                d *= 3.6;
                return new DecimalType(d);
            } else {
                logger.warn("Failed to convert Number {} with a value of {} from class {} to an appropriate type.",
                        deviceType, deviceValue, deviceValue.getClass().getName());
                return UnDefType.UNDEF;
            }
        } else {
            return defaultConvertToOpenHab(acceptedChannelType, dataFromSmartthings);
        }
    }

}

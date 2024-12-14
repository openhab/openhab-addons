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
package org.openhab.binding.smartthings.internal.converter;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter class for Smartthings capability "Color Control".
 * The Smartthings "Color Control" capability represents the hue values in the 0-100% range. OH2 uses 0-360 degrees
 * For this converter only the hue is coming into openHAB and it is a number
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsHue100Converter extends SmartthingsConverter {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsHue100Converter.class);

    public SmartthingsHue100Converter(SmartthingsTypeRegistry typeRegistry, Thing thing) {
        super(typeRegistry, thing);
    }

    @Override
    public String convertToSmartthings(Thing thing, ChannelUID channelUid, Command command) {
        String jsonMsg;

        if (command instanceof HSBType hsbCommand) {
            double hue = hsbCommand.getHue().doubleValue() / 3.60;
            String value = String.format("[%.0f, %d, %d ]", hue, hsbCommand.getSaturation().intValue(),
                    hsbCommand.getBrightness().intValue());
            jsonMsg = String.format(
                    "{\"capabilityKey\": \"%s\", \"deviceDisplayName\": \"%s\", \"capabilityAttribute\": \"%s\", \"value\": %s}",
                    thingTypeId, "smartthingsName", channelUid.getId(), value);
        } else {
            jsonMsg = defaultConvertToSmartthings(thing, channelUid, command);
        }

        return jsonMsg;
    }

    @Override
    public State convertToOpenHab(Thing thing, ChannelUID channelUid, Object dataFromSmartthings) {
        // Here we have to multiply the value from Smartthings by 3.6 to convert from 0-100 to 0-360
        if (dataFromSmartthings == null) {
            logger.warn("Failed to convert Number because Smartthings returned a null value.");
            return UnDefType.UNDEF;
        }

        Channel channel = thing.getChannel(channelUid);
        String acceptedChannelType = channel.getAcceptedItemType();
        if (acceptedChannelType == null) {
            return UnDefType.NULL;
        }

        if (acceptedChannelType != null && "Number".contentEquals(acceptedChannelType)) {
            if (dataFromSmartthings instanceof String stringCommand) {
                double d = Double.parseDouble(stringCommand);
                d *= 3.6;
                return new DecimalType(d);
            } else if (dataFromSmartthings instanceof Long) {
                double d = ((Long) dataFromSmartthings).longValue();
                d *= 3.6;
                return new DecimalType(d);
            } else if (dataFromSmartthings instanceof BigDecimal decimalValue) {
                double d = decimalValue.doubleValue();
                d *= 3.6;
                return new DecimalType(d);
            } else if (dataFromSmartthings instanceof Number numberValue) {
                double d = numberValue.doubleValue();
                d *= 3.6;
                return new DecimalType(d);
            } else {
                logger.warn("Failed to convert Number with a value of {} from class {} to an appropriate type.",
                        dataFromSmartthings, dataFromSmartthings.getClass().getName());
                return UnDefType.UNDEF;
            }
        } else {
            return defaultConvertToOpenHab(thing, channelUid, dataFromSmartthings);
        }
    }
}

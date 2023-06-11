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
package org.openhab.binding.smartthings.internal.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStateData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter class for Smartthings capability "Color Control".
 * In this case the color being delivered by Smartthings is in the for #hhssbb where hh=hue in hex, ss=saturation in hex
 * and bb=brightness in hex
 * And, the hue is a value from 0 to 100% but openHAB expects the hue in 0 to 360
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsColor100Converter extends SmartthingsConverter {

    private Pattern rgbInputPattern = Pattern.compile("^#[0-9a-fA-F]{6}");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsColor100Converter.class);

    public SmartthingsColor100Converter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String jsonMsg;
        // The command should be of HSBType. The hue component needs to be divided by 3.6 to convert 0-360 degrees to
        // 0-100 percent
        // The easiest way to do this is to create a new HSBType with the hue component changed.
        if (command instanceof HSBType) {
            HSBType hsb = (HSBType) command;
            double hue = Math.round((hsb.getHue().doubleValue() / 3.60)); // add .5 to round
            long hueInt = (long) hue;
            HSBType hsb100 = new HSBType(new DecimalType(hueInt), hsb.getSaturation(), hsb.getBrightness());
            // now use the default converter to convert to a JSON string
            jsonMsg = defaultConvertToSmartthings(channelUid, hsb100);
        } else {
            jsonMsg = defaultConvertToSmartthings(channelUid, command);
        }
        return jsonMsg;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.smartthings.internal.converter.SmartthingsConverter#convertToOpenHab(java.lang.String,
     * org.openhab.binding.smartthings.internal.SmartthingsStateData)
     */
    @Override
    public State convertToOpenHab(@Nullable String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        // The color value from Smartthings will look like "#123456" which is the RGB color
        // This needs to be converted into HSB type
        String value = dataFromSmartthings.value;
        if (value == null) {
            logger.warn("Failed to convert color {} because Smartthings returned a null value.",
                    dataFromSmartthings.deviceDisplayName);
            return UnDefType.UNDEF;
        }

        // If the bulb is off the value maybe null, so better check
        State state;
        // First verify the format the string is valid
        Matcher matcher = rgbInputPattern.matcher(value);
        if (!matcher.matches()) {
            logger.warn(
                    "The \"value\" in the following message is not a valid color. Expected a value like \"#123456\" instead of {}",
                    dataFromSmartthings.toString());
            return UnDefType.UNDEF;
        }

        // Get the RGB colors
        int rgb[] = new int[3];
        for (int i = 0, pos = 1; i < 3; i++, pos += 2) {
            String c = value.substring(pos, pos + 2);
            rgb[i] = Integer.parseInt(c, 16);
        }

        // Convert to state
        state = HSBType.fromRGB(rgb[0], rgb[1], rgb[2]);
        return state;
    }
}

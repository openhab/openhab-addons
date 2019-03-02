/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.smartthings.internal.SmartthingsStateData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter class for Door Control.
 * This can't use the default because when closing the door the command that comes in is "closed" but "close" need to be
 * sent to Smartthings
 *
 * @author Bob Raker - Initial contribution
 *
 */
public class SmartthingsColorConverter extends SmartthingsConverter {

    private Pattern rgbInputPattern = Pattern.compile("^#[0-9a-fA-F]{6}");

    private Logger logger = LoggerFactory.getLogger(SmartthingsConverter.class);

    SmartthingsColorConverter(String name) {
        super(name);
    }

    public SmartthingsColorConverter(Thing thing) {
        super(thing);
    }

    @Override
    public String convertToSmartthings(ChannelUID channelUid, Command command) {
        String jsonMsg = defaultConvertToSmartthings(channelUid, command);
        return jsonMsg;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.smartthings.internal.converter.SmartthingsConverter#convertToOpenHab(java.lang.String,
     * org.openhab.binding.smartthings.internal.SmartthingsStateData)
     */
    @Override
    public State convertToOpenHab(String acceptedChannelType, SmartthingsStateData dataFromSmartthings) {
        // The color value from Smartthings will look like "#123456" which is the RGB color
        // This needs to be converted into HSB type
        String value = dataFromSmartthings.getValue();

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
        State state = HSBType.fromRGB(rgb[0], rgb[1], rgb[2]);
        return state;
    }

}

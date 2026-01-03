/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
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
 * Converter class for SmartThings capability "Color Control".
 * The SmartThings "Color Control" capability represents the hue values in the 0-100% range. OH2 uses 0-360 degrees
 * For this converter only the hue is coming into openHAB and it is a number
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsHue100Converter extends SmartThingsConverter {

    private final Logger logger = LoggerFactory.getLogger(SmartThingsHue100Converter.class);
    private final double conversionFactor = 3.60;

    public SmartThingsHue100Converter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command) {
        if (command instanceof HSBType hsbCommand) {
            double hue = hsbCommand.getHue().doubleValue() / conversionFactor;

            String componentKey = SmartThingsBindingConstants.GROUPD_ID_MAIN;
            String capaKey = SmartThingsBindingConstants.CAPA_COLOR_CONTROL;
            String cmdName = SmartThingsBindingConstants.CMD_SET_HUE;
            Object[] arguments = new Object[1];
            arguments[0] = hue;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);
        } else if (command instanceof DecimalType dec) {
            double hue = dec.doubleValue() / conversionFactor;

            String componentKey = SmartThingsBindingConstants.GROUPD_ID_MAIN;
            String capaKey = SmartThingsBindingConstants.CAPA_COLOR_CONTROL;
            String cmdName = SmartThingsBindingConstants.CMD_SET_HUE;
            Object[] arguments = new Object[1];
            arguments[0] = hue;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);
        } else {
            logger.info("");
        }
    }

    // <!-- The SmartThings colorControl:hue has a range of 0-100% where OH2 uses the normal 0-360 degrees -->
    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartThings) {
        // Here we have to multiply the value from SmartThings by 3.6 to convert from 0-100 to 0-360

        if (dataFromSmartThings instanceof Double value) {
            value *= conversionFactor;

            return new DecimalType(value);
        } else if (dataFromSmartThings instanceof String value) {
            double d = Double.parseDouble(value);
            d *= conversionFactor;

            return new DecimalType(d);
        } else if (dataFromSmartThings instanceof Long value) {
            double d = value.longValue();
            d *= conversionFactor;

            return new DecimalType(d);
        } else if (dataFromSmartThings instanceof BigDecimal decimalValue) {
            double d = decimalValue.doubleValue();
            d *= conversionFactor;

            return new DecimalType(d);
        } else if (dataFromSmartThings instanceof Number numberValue) {
            double d = numberValue.doubleValue();
            d *= conversionFactor;

            return new DecimalType(d);
        }

        return UnDefType.UNDEF;
    }
}

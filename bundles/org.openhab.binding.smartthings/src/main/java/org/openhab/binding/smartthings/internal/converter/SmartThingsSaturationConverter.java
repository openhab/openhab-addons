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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.dto.ColorObject;
import org.openhab.binding.smartthings.internal.stateHandler.SmartThingsStateHandler;
import org.openhab.binding.smartthings.internal.stateHandler.SmartThingsStateHandlerFactory;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Converter class for SmartThings capability "Color Control".
 * The SmartThings "Color Control" capability represents the hue values in the 0-100% range. OH2 uses 0-360 degrees
 * For this converter only the hue is coming into openHAB and it is a number
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsSaturationConverter extends SmartThingsConverter {
    private final double conversionFactor = 3.60;

    public SmartThingsSaturationConverter(SmartThingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartThingsInternal(Thing thing, ChannelUID channelUid, Command command)
            throws SmartThingsException {
        if (command instanceof HSBType hsbCommand) {
            double hue = hsbCommand.getHue().doubleValue() / conversionFactor;
            double sat = hsbCommand.getSaturation().doubleValue();

            String componentKey = SmartThingsBindingConstants.GROUPD_ID_MAIN;
            String capaKey = SmartThingsBindingConstants.CAPA_COLOR_CONTROL;
            String cmdName = SmartThingsBindingConstants.CMD_SET_COLOR;
            Object[] arguments = new Object[1];
            ColorObject colorObj = new ColorObject();
            colorObj.hue = hue;
            colorObj.saturation = sat;
            arguments[0] = colorObj;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);

        } else if (command instanceof DecimalType dec) {
            double hue = 0;
            double sat = dec.doubleValue();

            SmartThingsStateHandler stateHandler = SmartThingsStateHandlerFactory
                    .getStateHandler(SmartThingsBindingConstants.THING_LIGHT);
            if (stateHandler != null) {
                State stateHue = stateHandler.getState(SmartThingsBindingConstants.CHANNEL_NAME_HUE);

                if (stateHue instanceof DecimalType decHue) {
                    hue = decHue.doubleValue();
                    hue /= conversionFactor;
                }
            }

            String componentKey = SmartThingsBindingConstants.GROUPD_ID_MAIN;
            String capaKey = SmartThingsBindingConstants.CAPA_COLOR_CONTROL;
            String cmdName = SmartThingsBindingConstants.CMD_SET_COLOR;
            Object[] arguments = new Object[1];
            ColorObject colorObj = new ColorObject();
            colorObj.hue = hue;
            colorObj.saturation = sat;
            arguments[0] = colorObj;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);

        } else {
            throw new SmartThingsException(
                    String.format("Unsupported command received {}, please contact developper", command.getClass()));
        }
    }

    // <!-- The SmartThings colorControl:hue has a range of 0-100% where OH2 uses the normal 0-360 degrees -->
    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartThings) {
        if (dataFromSmartThings instanceof Double value) {
            return new DecimalType(checkValue(value));
        } else if (dataFromSmartThings instanceof String value) {
            double d = Double.parseDouble(value);
            return new DecimalType(checkValue(d));
        }

        return UnDefType.UNDEF;
    }

    public double checkValue(double val) {
        if (val <= 0) {
            return 0;
        }
        if (val > 100) {
            return 100;
        }
        return val;
    }
}

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
package org.openhab.binding.e3dc.internal.handler;

import static org.openhab.binding.e3dc.internal.E3DCBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.dto.StringBlock;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCStringHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCStringHandler extends BaseHandler {
    private final Logger logger = LoggerFactory.getLogger(E3DCStringHandler.class);

    public E3DCStringHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands possible for Strings Details
    }

    @Override
    public void initialize() {
        super.initialize(DataType.DATA);
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        StringBlock block = (StringBlock) provider.getData(DataType.STRINGS);
        if (block != null) {
            updateState(STRING1_DC_VOLTAGE_CHANNEL, block.string1Volt);
            updateState(STRING2_DC_VOLTAGE_CHANNEL, block.string2Volt);
            updateState(STRING3_DC_VOLTAGE_CHANNEL, block.string3Volt);
            updateState(STRING1_DC_CURRENT_CHANNEL, block.string1Ampere);
            updateState(STRING2_DC_CURRENT_CHANNEL, block.string2Ampere);
            updateState(STRING3_DC_CURRENT_CHANNEL, block.string3Ampere);
            updateState(STRING1_DC_OUTPUT_CHANNEL, block.string1Watt);
            updateState(STRING2_DC_OUTPUT_CHANNEL, block.string2Watt);
            updateState(STRING3_DC_OUTPUT_CHANNEL, block.string3Watt);
        } else {
            logger.debug("Unable to get {} from provider {}", DataType.STRINGS, provider.toString());
        }
    }
}

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
import org.openhab.binding.e3dc.internal.dto.InfoBlock;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCInfoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCInfoHandler extends BaseHandler {

    private final Logger logger = LoggerFactory.getLogger(E3DCInfoHandler.class);

    public E3DCInfoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands possible for Info block
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        InfoBlock block = (InfoBlock) provider.getData(DataType.INFO);
        updateState(MODBUS_ID_CHANNEL, block.modbusId);
        updateState(MODBUS_FIRMWARE_CHANNEL, block.modbusVersion);
        updateState(SUPPORTED_REGSITERS_CHANNEL, block.supportedRegisters);
        updateState(MANUFACTURER_NAME_CHANNEL, block.manufacturer);
        updateState(MODEL_NAME_CHANNEL, block.modelName);
        updateState(SERIAL_NUMBER_CHANNEL, block.serialNumber);
        updateState(FIRMWARE_RELEASE_CHANNEL, block.firmware);
        updateState(INFO_CHANNEL, block.allInfo);
    }
}

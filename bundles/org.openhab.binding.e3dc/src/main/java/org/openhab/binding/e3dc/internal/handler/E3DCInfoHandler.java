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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.E3DCConfiguration;
import org.openhab.binding.e3dc.internal.dto.E3DCInfoBlock;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.InfoBlockCallback;
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCInfoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCInfoHandler extends BaseHandler implements DataListener {

    private final Logger logger = LoggerFactory.getLogger(E3DCInfoHandler.class);
    private final InfoBlockCallback callback = new InfoBlockCallback();
    private @Nullable E3DCConfiguration config;

    public E3DCInfoHandler(Thing thing, ModbusManager ref) {
        super(thing, ref);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands possible for Info block
    }

    @Override
    public void initialize() {
        setStatus(ThingStatus.UNKNOWN);
        // Example for background initialization:
        scheduler.execute(() -> {
            config = getConfigAs(E3DCConfiguration.class);
            if (checkConfig(config)) {
                ModbusTCPSlaveEndpoint slaveEndpoint = new ModbusTCPSlaveEndpoint(config.ipAddress, config.port);
                EndpointPoolConfiguration epc = modbusManagerRef.getEndpointPoolConfiguration(slaveEndpoint);
                BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 0, 68, 3);
                callback.addDataListener(this);
                BasicPollTaskImpl poller = new BasicPollTaskImpl(slaveEndpoint, request, callback);
                modbusManagerRef.registerRegularPoll(poller, config.refreshInterval_sec * 1000, 0);
            } else {
                setStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        callback.removeDataListener(this);
    }

    @Override
    public void newDataREceived() {
        if (myStatus != ThingStatus.ONLINE) {
            setStatus(ThingStatus.ONLINE);
        }
        E3DCInfoBlock block = callback.getData();
        updateState(MODBUS_ID_CHANNEL, block.modbusId);
        updateState(MODBUS_FIRMWARE_CHANNEL, block.modbusVersion);
        updateState(SUPPORTED_REGSITERS_CHANNEL, block.supportedRegisters);
        updateState(MANUFACTURER_NAME_CHANNEL, block.manufacturer);
        updateState(MODEL_NAME_CHANNEL, block.modelName);
        updateState(SERIAL_NUMBER_CHANNEL, block.serialNumber);
        updateState(FIRMWARE_RELEASE_CHANNEL, block.firmware);
    }
}

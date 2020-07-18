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

import static org.openhab.binding.e3dc.internal.modbus.E3DCModbusConstans.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.E3DCDeviceConfiguration;
import org.openhab.binding.e3dc.internal.modbus.Data.DataType;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.ModbusCallback;
import org.openhab.binding.e3dc.internal.modbus.ModbusDataProvider;
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.BasicWriteTask;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusResponse;
import org.openhab.io.transport.modbus.ModbusWriteCallback;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCDeviceThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
public class E3DCDeviceThingHandler extends BaseBridgeHandler implements DataListener, ModbusWriteCallback {
    private final Logger logger = LoggerFactory.getLogger(E3DCDeviceThingHandler.class);
    private ModbusManager modbusManagerRef;
    private final ModbusCallback modbusInfoCallback = new ModbusCallback(DataType.INFO);
    private final ModbusCallback modbusDataCallback = new ModbusCallback(DataType.DATA);
    private BasicPollTaskImpl infoPoller;
    private BasicPollTaskImpl dataPoller;
    private ModbusTCPSlaveEndpoint slaveEndpoint;
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private @Nullable E3DCDeviceConfiguration config;

    public E3DCDeviceThingHandler(Bridge bridge, ModbusManager ref) {
        super(bridge);
        modbusManagerRef = ref;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        setStatus(ThingStatus.UNKNOWN);
        // Example for background initialization:
        scheduler.execute(() -> {
            config = getConfigAs(E3DCDeviceConfiguration.class);
            if (checkConfig(config)) {
                slaveEndpoint = new ModbusTCPSlaveEndpoint(config.host, config.port);
                // register low speed info poller
                BasicModbusReadRequestBlueprint infoRequest = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, INFO_REG_START, INFO_REG_SIZE, 3);
                infoPoller = new BasicPollTaskImpl(slaveEndpoint, infoRequest, modbusInfoCallback);
                modbusManagerRef.registerRegularPoll(infoPoller, INFO_POLL_REFRESH_TIME_MS, 0);
                // register high speed data poller
                BasicModbusReadRequestBlueprint dataRequest = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                        REGISTER_LENGTH - INFO_REG_SIZE, 3);
                dataPoller = new BasicPollTaskImpl(slaveEndpoint, dataRequest, modbusDataCallback);
                modbusManagerRef.registerRegularPoll(dataPoller, config.refresh, 0);
                // listen for data to get ONLINE
                modbusDataCallback.addDataListener(this);
            } else {
                setStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void dispose() {
        modbusDataCallback.removeDataListener(this);
        if (modbusManagerRef != null) {
            if (infoPoller != null) {
                modbusManagerRef.unregisterRegularPoll(infoPoller);
            }
            if (dataPoller != null) {
                modbusManagerRef.unregisterRegularPoll(dataPoller);
            }
        }
    }

    private boolean checkConfig(@Nullable E3DCDeviceConfiguration config) {
        if (config != null) {
            if (config.host != null && config.port > 1) {
                if (config.refresh < 1) {
                    config.refresh = 2;
                }
                return true;
            }
        }
        return false;
    }

    private void setStatus(ThingStatus status) {
        myStatus = status;
        updateStatus(myStatus);
    }

    public void wallboxSet(int wallboxId, int writeValue) {
        ModbusRegisterArray regArray = new BasicModbusRegisterArray(writeValue);
        BasicModbusWriteRegisterRequestBlueprint writeBluePrint = new BasicModbusWriteRegisterRequestBlueprint(1,
                WALLBOX_REG_START + wallboxId, regArray, false, 3);
        modbusManagerRef.submitOneTimeWrite(new BasicWriteTask(slaveEndpoint, writeBluePrint, this));
    }

    @Override
    public void dataAvailable(ModbusDataProvider provider) {
        if (myStatus != ThingStatus.ONLINE) {
            setStatus(ThingStatus.ONLINE);
        }
    }

    public ModbusDataProvider getInfoDataProvider() {
        return modbusInfoCallback;
    }

    public ModbusDataProvider getDataProvider() {
        return modbusDataCallback;
    }

    @Override
    public void onError(@NonNull ModbusWriteRequestBlueprint request, @NonNull Exception error) {
        logger.info("Modbus write error");
    }

    @Override
    public void onWriteResponse(@NonNull ModbusWriteRequestBlueprint request, @NonNull ModbusResponse response) {
        logger.info("Modbus write response = {}", response.getFunctionCode());
    }

}

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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
public class E3DCDeviceThingHandler extends BaseBridgeHandler implements DataListener, ModbusWriteCallback {
    private final Logger logger = LoggerFactory.getLogger(E3DCDeviceThingHandler.class);
    private ModbusManager modbusManagerRef;
    private final ModbusCallback modbusInfoCallback = new ModbusCallback(DataType.INFO);
    private final ModbusCallback modbusDataCallback = new ModbusCallback(DataType.DATA);
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private @Nullable BasicPollTaskImpl infoPoller;
    private @Nullable BasicPollTaskImpl dataPoller;
    private @Nullable ModbusTCPSlaveEndpoint slaveEndpoint;
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
            E3DCDeviceConfiguration localConfig = config;
            if (localConfig != null && checkConfig(localConfig)) {
                ModbusTCPSlaveEndpoint slaveEndpoint = new ModbusTCPSlaveEndpoint(localConfig.host, localConfig.port);
                this.slaveEndpoint = slaveEndpoint;
                // register low speed info poller
                BasicModbusReadRequestBlueprint infoRequest = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, INFO_REG_START, INFO_REG_SIZE, 3);

                BasicPollTaskImpl localInfoPoller = new BasicPollTaskImpl(slaveEndpoint, infoRequest,
                        modbusInfoCallback);
                infoPoller = localInfoPoller;
                modbusManagerRef.registerRegularPoll(localInfoPoller, INFO_POLL_REFRESH_TIME_MS, 0);
                // register high speed data poller
                BasicModbusReadRequestBlueprint dataRequest = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                        REGISTER_LENGTH - INFO_REG_SIZE, 3);
                BasicPollTaskImpl localDataPoller = new BasicPollTaskImpl(slaveEndpoint, dataRequest,
                        modbusDataCallback);
                dataPoller = localDataPoller;
                modbusManagerRef.registerRegularPoll(localDataPoller, localConfig.refresh, 0);
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
        BasicPollTaskImpl localInfoPoller = infoPoller;
        if (localInfoPoller != null) {
            modbusManagerRef.unregisterRegularPoll(localInfoPoller);
        }
        BasicPollTaskImpl localDataPoller = dataPoller;
        if (localDataPoller != null) {
            modbusManagerRef.unregisterRegularPoll(localDataPoller);
        }
    }

    private boolean checkConfig(@Nullable E3DCDeviceConfiguration c) {
        if (c != null) {
            if (c.port > 1) {
                if (c.refresh < 1) {
                    c.refresh = 2;
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

    /**
     * Wallbox Settings can be changed with one Integer
     *
     * @param wallboxId needed to calculate right register
     * @param writeValue integer to be written
     */
    public void wallboxSet(int wallboxId, int writeValue) {
        ModbusRegisterArray regArray = new BasicModbusRegisterArray(writeValue);
        BasicModbusWriteRegisterRequestBlueprint writeBluePrint = new BasicModbusWriteRegisterRequestBlueprint(1,
                WALLBOX_REG_START + wallboxId, regArray, false, 3);
        ModbusTCPSlaveEndpoint localSlaveEndpoint = slaveEndpoint;
        if (localSlaveEndpoint != null) {
            modbusManagerRef.submitOneTimeWrite(new BasicWriteTask(localSlaveEndpoint, writeBluePrint, this));
        }
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
    public void onError(ModbusWriteRequestBlueprint request, Exception error) {
        logger.info("Modbus write error. Request {} Message {}", request.toString(), error.getMessage());
    }

    @Override
    public void onWriteResponse(ModbusWriteRequestBlueprint request, ModbusResponse response) {
        logger.debug("Modbus write response = {}", response.getFunctionCode());
    }
}

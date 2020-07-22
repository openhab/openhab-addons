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
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusWriteResult;
import org.openhab.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.io.transport.modbus.ModbusFailureCallback;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusWriteCallback;
import org.openhab.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCDeviceThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCDeviceThingHandler extends BaseBridgeHandler
        implements DataListener, ModbusWriteCallback, ModbusFailureCallback<ModbusWriteRequestBlueprint> {
    private final Logger logger = LoggerFactory.getLogger(E3DCDeviceThingHandler.class);
    private ModbusManager modbusManagerRef;
    private final ModbusCallback modbusInfoCallback = new ModbusCallback(DataType.INFO);
    private final ModbusCallback modbusDataCallback = new ModbusCallback(DataType.DATA);
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private @Nullable ModbusCommunicationInterface modbusCom;
    private @Nullable PollTask infoPoller;
    private @Nullable PollTask dataPoller;
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

                ModbusCommunicationInterface localModbusCom = modbusManagerRef.newModbusCommunicationInterface(
                        slaveEndpoint, modbusManagerRef.getEndpointPoolConfiguration(slaveEndpoint));
                // register low speed info poller
                ModbusReadRequestBlueprint infoRequest = new ModbusReadRequestBlueprint(localConfig.deviceid,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, INFO_REG_START, INFO_REG_SIZE, 3);
                infoPoller = localModbusCom.registerRegularPoll(infoRequest, INFO_POLL_REFRESH_TIME_MS, 0,
                        modbusInfoCallback, modbusInfoCallback);

                ModbusReadRequestBlueprint dataRequest = new ModbusReadRequestBlueprint(localConfig.deviceid,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, POWER_REG_START,
                        REGISTER_LENGTH - INFO_REG_SIZE, 3);
                dataPoller = localModbusCom.registerRegularPoll(dataRequest, localConfig.refresh, 0, modbusDataCallback,
                        modbusDataCallback);
                modbusCom = localModbusCom;
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
        ModbusCommunicationInterface localCom = modbusCom;
        if (localCom != null) {
            PollTask localInfoPoller = infoPoller;
            if (localInfoPoller != null) {
                localCom.unregisterRegularPoll(localInfoPoller);
            }
            PollTask localDataPoller = dataPoller;
            if (localDataPoller != null) {
                localCom.unregisterRegularPoll(localDataPoller);
            }
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
        E3DCDeviceConfiguration localConfig = config;
        ModbusCommunicationInterface localCom = modbusCom;
        if (localConfig != null && localCom != null) {
            ModbusRegisterArray regArray = new ModbusRegisterArray(writeValue);
            ModbusWriteRegisterRequestBlueprint writeBluePrint = new ModbusWriteRegisterRequestBlueprint(
                    localConfig.deviceid, WALLBOX_REG_START + wallboxId, regArray, false, 3);
            localCom.submitOneTimeWrite(writeBluePrint, this, this);
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
    public void handle(AsyncModbusWriteResult result) {
        logger.debug("E3DC Modbus write response! {}", result.getResponse().toString());
    }

    @Override
    public void handle(AsyncModbusFailure<ModbusWriteRequestBlueprint> failure) {
        logger.warn("E3DC Modbus write error! {}", failure.getRequest().toString());
    }
}

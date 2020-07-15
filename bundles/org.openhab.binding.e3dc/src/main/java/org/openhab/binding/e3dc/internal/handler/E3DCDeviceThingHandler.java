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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.e3dc.internal.E3DCDeviceConfiguration;
import org.openhab.binding.e3dc.internal.modbus.DataListener;
import org.openhab.binding.e3dc.internal.modbus.ModbusCallback;
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link E3DCDeviceThingHandler} Basic modbus connection towards the E3DC device
 *
 * @author Bernd Weymann - Initial contribution
 */
public class E3DCDeviceThingHandler extends BaseBridgeHandler implements DataListener {
    private final Logger logger = LoggerFactory.getLogger(E3DCDeviceThingHandler.class);
    private ModbusManager modbusManagerRef;
    private final ModbusCallback modbusCallback = new ModbusCallback();
    private ThingHandlerCallback thingHandlerCallback;
    private ThingStatus myStatus = ThingStatus.UNKNOWN;
    private @Nullable E3DCDeviceConfiguration config;
    BasicPollTaskImpl poller;

    public E3DCDeviceThingHandler(Bridge bridge, ModbusManager ref) {
        super(bridge);
        modbusManagerRef = ref;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // [todo]
    }

    @Override
    public Bridge getBridge() {
        ThingUID bridgeUID = thing.getBridgeUID();
        synchronized (this) {
            if (thingHandlerCallback != null) {
                return bridgeUID != null ? thingHandlerCallback.getBridge(bridgeUID) : null;
            } else {
                logger.warn(
                        "Handler {} of thing {} tried accessing its bridge although the handler was already disposed.",
                        getClass().getSimpleName(), thing.getUID());
                return null;
            }
        }
    }

    @Override
    public void initialize() {
        setStatus(ThingStatus.UNKNOWN);
        // Example for background initialization:
        scheduler.execute(() -> {
            config = getConfigAs(E3DCDeviceConfiguration.class);
            if (checkConfig(config)) {
                ModbusTCPSlaveEndpoint slaveEndpoint = new ModbusTCPSlaveEndpoint(config.host, config.port);
                EndpointPoolConfiguration epc = modbusManagerRef.getEndpointPoolConfiguration(slaveEndpoint);
                BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(1,
                        ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 0, 68, 3);
                modbusCallback.addDataListener(this);
                poller = new BasicPollTaskImpl(slaveEndpoint, request, modbusCallback);
                modbusManagerRef.registerRegularPoll(poller, config.refresh, 0);
            } else {
                setStatus(ThingStatus.OFFLINE);
            }
        });
    }

    @Override
    public void setCallback(@Nullable ThingHandlerCallback thingHandlerCallback) {
        // TODO Auto-generated method stub
        super.setCallback(thingHandlerCallback);
    }

    @Override
    public void dispose() {
        modbusCallback.removeDataListener(this);
        if (poller != null && modbusManagerRef != null) {
            modbusManagerRef.unregisterRegularPoll(poller);
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

    @Override
    public void newDataREceived() {
        if (myStatus != ThingStatus.ONLINE) {
            setStatus(ThingStatus.ONLINE);
        }
        logger.info("newDataREceived");
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.foxinverter.internal;

import static org.openhab.binding.modbus.foxinverter.internal.ModbusFoxInverterBindingConstants.THING_TYPE_INVERTER;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryParticipant;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service for FoxInverter devices
 *
 * @author Holger Friedrich - initial contribution
 *
 */
@Component
@NonNullByDefault
public class ModbusFoxInverterDiscoveryParticipant implements ModbusDiscoveryParticipant {

    private final Logger logger = LoggerFactory.getLogger(ModbusFoxInverterDiscoveryParticipant.class);

    private class FoxInverterDiscoveryProcess {
        private static final String PROPERTY_UNIQUE_ADDRESS = "uniqueAddress";
        private static final String MQ2200_IDENTIFIER = "MQ2200";
        private static final int MQ2200_START_ADDRESS = 30000;
        private final ModbusEndpointThingHandler handler;
        private final ModbusDiscoveryListener listener;

        public FoxInverterDiscoveryProcess(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener) {
            this.handler = handler;
            this.listener = listener;
        }

        public void detectModel() throws EndpointNotInitializedException {
            logger.trace("Beginning scan for FoxESS MQ2200 device at address {}", MQ2200_START_ADDRESS);
            ModbusCommunicationInterface comms = handler.getCommunicationInterface();
            if (comms == null) {
                throw new EndpointNotInitializedException();
            }
            // Initial poll of static info
            // manufacturer, model, serial number
            comms.submitOneTimePoll(new ModbusReadRequestBlueprint(handler.getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, MQ2200_START_ADDRESS, 16 * 3, 3),
                    (AsyncModbusReadResult result) -> {
                        logger.trace("Initial poll successful {}", result);
                        byte[] res = result.getRegisters().get().getBytes();
                        String modelInfo = new String(res, 0, 16).trim();
                        if (modelInfo.startsWith(MQ2200_IDENTIFIER)) {
                            // Handle MQ2200 specific logic

                            String serialNo = new String(res, 16, 16).trim();
                            String manufacturerId = new String(res, 32, 16).trim();

                            logger.debug("Detected Inverter Model: {}, S/N: {}, Manufacturer ID: {}", modelInfo,
                                    serialNo, manufacturerId);
                            // construct an ID, only alphanumeric characters, lower case
                            String deviceId = (serialNo + manufacturerId).replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                            if (deviceId.isBlank()) {
                                logger.debug("Device ID is blank, cannot create discovery result");
                            } else {
                                ThingTypeUID thingTypeUID = THING_TYPE_INVERTER;
                                ThingUID thingUID = new ThingUID(thingTypeUID, handler.getUID(), deviceId);

                                Map<String, Object> properties = new HashMap<>();
                                properties.put(PROPERTY_UNIQUE_ADDRESS,
                                        handler.getUID().getAsString() + ":" + deviceId);

                                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                        .withProperties(properties).withRepresentationProperty(PROPERTY_UNIQUE_ADDRESS)
                                        .withBridge(handler.getUID()).withLabel("FoxESS MQ2200 / Solakon ONE").build();

                                listener.thingDiscovered(discoveryResult);
                            }
                        }
                        listener.discoveryFinished();
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                        logger.info("Initial poll failed", error.getCause());
                        listener.discoveryFinished();
                    });
        }
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_INVERTER);
    }

    @Override
    public void startDiscovery(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener) {
        logger.debug("Starting fox inverter discovery");
        try {
            new FoxInverterDiscoveryProcess(handler, listener).detectModel();
        } catch (EndpointNotInitializedException ex) {
            logger.debug("Could not start discovery process");
            listener.discoveryFinished();
        }
    }
}

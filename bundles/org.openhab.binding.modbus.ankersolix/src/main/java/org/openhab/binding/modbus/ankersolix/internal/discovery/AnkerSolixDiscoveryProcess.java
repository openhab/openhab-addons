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
package org.openhab.binding.modbus.ankersolix.internal.discovery;

import static org.openhab.binding.modbus.ankersolix.internal.AnkerSolixBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.discovery.ModbusDiscoveryListener;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only discovery process for Anker SOLIX Modbus devices.
 *
 * @author Thorben Grove - Initial contribution
 */
@NonNullByDefault
public class AnkerSolixDiscoveryProcess {

    private static final int MAX_TRIES = 3;

    private static final int SMART_METER_SERIAL_START = 10702;
    private static final int SMART_METER_SERIAL_LENGTH = 10;
    private static final int SMART_METER_MODEL_START = 10620;
    private static final int SMART_METER_MODEL_LENGTH = 10;

    private static final int SMART_PLUG_SERIAL_START = 30005;
    private static final int SMART_PLUG_SERIAL_LENGTH = 12;
    private static final int SMART_PLUG_MODEL_START = 32768;
    private static final int SMART_PLUG_MODEL_LENGTH = 5;

    private static final int SOLARBANK_SERIAL_START = 10100;
    private static final int SOLARBANK_SERIAL_LENGTH = 12;
    private static final int SOLARBANK_MODEL_START = 32768;
    private static final int SOLARBANK_MODEL_LENGTH = 5;

    private static final int WALLBOX_SERIAL_START = 20011;
    private static final int WALLBOX_SERIAL_LENGTH = 12;
    private static final int WALLBOX_MODEL_START = 20001;
    private static final int WALLBOX_MODEL_LENGTH = 10;

    private final Logger logger = LoggerFactory.getLogger(AnkerSolixDiscoveryProcess.class);

    private final ModbusEndpointThingHandler handler;
    private final ModbusDiscoveryListener listener;
    private final ModbusCommunicationInterface communication;
    private final int slaveId;

    public AnkerSolixDiscoveryProcess(ModbusEndpointThingHandler handler, ModbusDiscoveryListener listener)
            throws EndpointNotInitializedException {
        this.handler = handler;
        this.listener = listener;

        ModbusCommunicationInterface comms = handler.getCommunicationInterface();
        if (comms == null) {
            throw new EndpointNotInitializedException();
        }
        this.communication = comms;
        this.slaveId = handler.getSlaveId();
    }

    public void start() {
        probeSmartMeter();
    }

    private void probeSmartMeter() {
        readString(SMART_METER_SERIAL_START, SMART_METER_SERIAL_LENGTH, serial -> {
            if (serial == null) {
                probeSmartPlug();
                return;
            }
            readString(SMART_METER_MODEL_START, SMART_METER_MODEL_LENGTH, model -> {
                String resolvedModel = resolveModel(serial, model);
                if (resolvedModel != null && resolvedModel.contains("Smart Meter")) {
                    emitDiscovery(THING_TYPE_SMART_METER_GEN2, DEVICE_FAMILY_SMART_METER_GEN2, serial, resolvedModel);
                    finish();
                } else {
                    probeSmartPlug();
                }
            }, failure -> probeSmartPlug());
        }, failure -> probeSmartPlug());
    }

    private void probeSmartPlug() {
        readString(SMART_PLUG_SERIAL_START, SMART_PLUG_SERIAL_LENGTH, serial -> {
            if (serial == null) {
                probeWallbox();
                return;
            }
            readString(SMART_PLUG_MODEL_START, SMART_PLUG_MODEL_LENGTH, model -> {
                String resolvedModel = resolveModel(serial, model);
                if (resolvedModel != null && resolvedModel.contains("Smart Plug")) {
                    emitDiscovery(THING_TYPE_SMART_PLUG, DEVICE_FAMILY_SMART_PLUG, serial, resolvedModel);
                    finish();
                } else {
                    probeWallbox();
                }
            }, failure -> probeWallbox());
        }, failure -> probeWallbox());
    }

    private void probeWallbox() {
        readString(WALLBOX_SERIAL_START, WALLBOX_SERIAL_LENGTH, serial -> {
            if (serial == null) {
                probeSolarbank();
                return;
            }
            readString(WALLBOX_MODEL_START, WALLBOX_MODEL_LENGTH, model -> {
                String resolvedModel = resolveModel(serial, model);
                if (resolvedModel != null && resolvedModel.contains("EV Charger")) {
                    emitDiscovery(THING_TYPE_EV_CHARGER, DEVICE_FAMILY_EV_CHARGER, serial, resolvedModel);
                    finish();
                } else {
                    probeSolarbank();
                }
            }, failure -> probeSolarbank());
        }, failure -> probeSolarbank());
    }

    private void probeSolarbank() {
        readString(SOLARBANK_SERIAL_START, SOLARBANK_SERIAL_LENGTH, serial -> {
            if (serial == null) {
                finish();
                return;
            }
            readString(SOLARBANK_MODEL_START, SOLARBANK_MODEL_LENGTH, model -> {
                String resolvedModel = resolveModel(serial, model);
                if (resolvedModel == null || !resolvedModel.contains("Anker SOLIX")) {
                    finish();
                    return;
                }

                ThingTypeUID thingType = resolvedModel.contains("Solarbank 4") ? THING_TYPE_SOLARBANK_4
                        : THING_TYPE_SOLARBANK_AC;
                emitDiscovery(thingType, DEVICE_FAMILY_SOLARBANK, serial, resolvedModel);
                finish();
            }, failure -> finish());
        }, failure -> finish());
    }

    private void readString(int startAddress, int registerCount, Consumer<@Nullable String> success,
            Consumer<Throwable> failure) {
        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(slaveId,
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, startAddress, registerCount, MAX_TRIES);

        communication.submitOneTimePoll(request, result -> {
            @Nullable
            String value = null;
            if (result.getRegisters().isPresent()) {
                value = decodeUtf8String(result.getRegisters().get());
            }
            success.accept(value);
        }, asyncFailure -> {
            logger.trace("Discovery read failed at address {}", startAddress, asyncFailure.getCause());
            failure.accept(asyncFailure.getCause());
        });
    }

    private String decodeUtf8String(ModbusRegisterArray registers) {
        String decoded = new String(registers.getBytes(), StandardCharsets.UTF_8).replace("\u0000", "").trim();
        return decoded;
    }

    private @Nullable String resolveModel(@Nullable String serialNumber, @Nullable String rawModel) {
        String modelFromSerial = resolveModelFromSerial(serialNumber);
        if (modelFromSerial != null) {
            return modelFromSerial;
        }
        if (rawModel == null || rawModel.isBlank()) {
            return null;
        }
        return rawModel;
    }

    private void emitDiscovery(ThingTypeUID thingType, String deviceFamily, @Nullable String serialNumber,
            String model) {
        String normalizedSerial = normalizeIdentifierPart(serialNumber);
        String idPart = !normalizedSerial.isBlank() ? normalizedSerial
                : (deviceFamily + "-slave" + slaveId).replaceAll("[^a-zA-Z0-9-]", "").toLowerCase(Locale.ROOT);

        ThingUID thingUID = new ThingUID(thingType, handler.getUID(), idPart);
        String uniqueAddress = handler.getUID().getAsString() + ":" + idPart;

        Map<String, Object> properties = new HashMap<>();
        properties.put(DISCOVERY_PROPERTY_UNIQUE_ADDRESS, uniqueAddress);
        properties.put(DISCOVERY_PROPERTY_MODEL, model);
        properties.put(DISCOVERY_PROPERTY_DEVICE_FAMILY, deviceFamily);
        if (serialNumber != null && !serialNumber.isBlank()) {
            properties.put(DISCOVERY_PROPERTY_SERIAL_NUMBER, serialNumber);
        }

        String label = serialNumber != null && !serialNumber.isBlank() ? model + " (SN: " + serialNumber + ")" : model;

        DiscoveryResult result = DiscoveryResultBuilder.create(thingUID).withBridge(handler.getUID())
                .withRepresentationProperty(DISCOVERY_PROPERTY_UNIQUE_ADDRESS).withProperties(properties)
                .withLabel(label).build();

        listener.thingDiscovered(result);
    }

    static String normalizeIdentifierPart(@Nullable String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private void finish() {
        listener.discoveryFinished();
    }
}

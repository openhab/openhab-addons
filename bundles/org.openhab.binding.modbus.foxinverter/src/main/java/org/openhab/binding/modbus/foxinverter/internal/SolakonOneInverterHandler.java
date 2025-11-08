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

import static org.openhab.binding.modbus.foxinverter.internal.ModbusFoxInverterBindingConstants.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.handler.BaseModbusThingHandler;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusBitUtilities;
import org.openhab.core.io.transport.modbus.ModbusConstants;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolakonOneInverterHandler} is responsible for reading the Modbus values of the
 * sungrow inverter.
 *
 * @author Holger Friedrich - Initial contribution
 */
@NonNullByDefault
public class SolakonOneInverterHandler extends BaseModbusThingHandler {

    @NonNullByDefault
    private static final class ModbusRequest {

        private final Deque<SolakonOneInverterRegisters> registers;
        private final ModbusReadRequestBlueprint blueprint;

        public ModbusRequest(Deque<SolakonOneInverterRegisters> registers, int slaveId, int tries) {
            this.registers = registers;
            this.blueprint = initReadRequest(registers, slaveId, tries);
        }

        private ModbusReadRequestBlueprint initReadRequest(Deque<SolakonOneInverterRegisters> registers, int slaveId,
                int tries) {
            int firstRegister = registers.getFirst().getRegisterNumber();
            int lastRegister = registers.getLast().getRegisterNumber();
            int length = lastRegister - firstRegister + registers.getLast().getRegisterCount();
            assert length <= ModbusConstants.MAX_REGISTERS_READ_COUNT;

            return new ModbusReadRequestBlueprint(slaveId, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS,
                    firstRegister, length, tries);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(SolakonOneInverterHandler.class);

    private List<ModbusRequest> modbusRequests = new ArrayList<>();

    public SolakonOneInverterHandler(Thing thing) {
        super(thing);
    }

    /**
     * Splits the SungrowInverterRegisters into multiple ModbusRequest, to ensure the max request size.
     */
    private List<ModbusRequest> buildRequests(int tries) {
        final List<ModbusRequest> requests = new ArrayList<>();
        Deque<SolakonOneInverterRegisters> currentRequest = new ArrayDeque<>();
        int currentRequestFirstRegister = 0;

        for (SolakonOneInverterRegisters channel : SolakonOneInverterRegisters.values()) {
            logger.warn("Evaluating register {}", channel.name());

            if (currentRequest.isEmpty()) {
                currentRequest.add(channel);
                currentRequestFirstRegister = channel.getRegisterNumber();
            } else {
                int sizeWithRegisterAdded = channel.getRegisterNumber() - currentRequestFirstRegister
                        + channel.getRegisterCount();
                if (sizeWithRegisterAdded > ModbusConstants.MAX_REGISTERS_READ_COUNT) {
                    requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
                    currentRequest = new ArrayDeque<>();

                    currentRequest.add(channel);
                    currentRequestFirstRegister = channel.getRegisterNumber();

                    logger.debug("Starting new modbus request template due to size limit, first register {} ({})",
                            channel.name(), currentRequestFirstRegister);
                } else {
                    currentRequest.add(channel);
                }
            }
        }

        if (!currentRequest.isEmpty()) {
            requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
        }
        logger.debug("Created {} modbus request templates.", requests.size());
        return requests;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && !this.modbusRequests.isEmpty()) {
            logger.warn("REFRESH command received, submitting one-time polls for all registers.");

            readStaticData();

            for (ModbusRequest request : this.modbusRequests) {
                submitOneTimePoll(request.blueprint,
                        (AsyncModbusReadResult result) -> this.readSuccessful(request, result), this::readError);
            }
        }
    }

    @Override
    public void modbusInitialize() {
        final SolakonOneInverterConfiguration config = getConfigAs(SolakonOneInverterConfiguration.class);

        if (config.pollInterval <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid poll interval: " + config.pollInterval);
            return;
        }

        if (config.maxTries <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Invalid Maximum Tries When Reading: " + config.maxTries);
            return;
        }

        this.updateStatus(ThingStatus.UNKNOWN);
        // read static data, store into properties
        getThing().setProperties(readStaticData());

        // setup regular polling
        this.modbusRequests = this.buildRequests(config.maxTries);

        for (ModbusRequest request : modbusRequests) {
            registerRegularPoll(request.blueprint, config.pollInterval, 0,
                    (AsyncModbusReadResult result) -> this.readSuccessful(request, result), this::readError);
        }
    }

    private Map<String, String> readStaticData() {
        Map<String, String> properties = new java.util.concurrent.ConcurrentHashMap<>();
        try {
            // Initial poll of static info
            // a) manufacturer, model, serial number
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 30000, 16 * 3, 3),
                    (AsyncModbusReadResult result) -> {
                        logger.trace("Initial poll successful {}", result);
                        byte[] res = result.getRegisters().get().getBytes();
                        String modelInfo = new String(res, 0, 16).trim();
                        String serialNo = new String(res, 16, 16).trim();
                        String manufacturerId = new String(res, 32, 16).trim();

                        logger.debug("Inverter Model: {}, S/N: {}, Manufacturer ID: {}", modelInfo, serialNo,
                                manufacturerId);
                        if (!modelInfo.isEmpty()) {
                            properties.put(MODEL_NAME, modelInfo);
                        }
                        if (!serialNo.isEmpty()) {
                            properties.put(SERIAL_NO, serialNo);
                        }
                        if (!manufacturerId.isEmpty()) {
                            properties.put(MANUFACTURER_ID, manufacturerId);
                        }
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                        logger.info("Initial poll failed", error.getCause());
                    });
            Thread.sleep(1000);
            // b) firmware versions
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 36001, 3, 3), (AsyncModbusReadResult result) -> {
                        byte[] res = result.getRegisters().get().getBytes();
                        properties.put(FIRMWARE_WR, String.format("%d.%03d", res[0], res[1]));
                        properties.put(FIRMWARE_PV, String.format("%d.%03d", res[2], res[3])); // not sure, could also
                                                                                               // be 4,5

                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
            // c) firmware versions
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 37003, 1, 3), (AsyncModbusReadResult result) -> {
                        byte[] res = result.getRegisters().get().getBytes();
                        properties.put(FIRMWARE_BMS, String.format("%d.%03d", res[0], res[1]));
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
            // d) rated power, max active power
            submitOneTimePoll(new ModbusReadRequestBlueprint(getSlaveId(),
                    ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, 39053, 2 * 2, 3),
                    (AsyncModbusReadResult result) -> {
                        ModbusBitUtilities.extractStateFromRegisters(result.getRegisters().get(), 0,
                                ModbusConstants.ValueType.UINT32).ifPresent(v -> {
                                    properties.put(RATED_POWER, v.toBigDecimal().toString() + " W");
                                });
                        ModbusBitUtilities.extractStateFromRegisters(result.getRegisters().get(), 2,
                                ModbusConstants.ValueType.UINT32).ifPresent(v -> {
                                    properties.put(MAX_ACTIVE_POWER, v.toBigDecimal().toString() + " W");
                                });
                    }, (AsyncModbusFailure<ModbusReadRequestBlueprint> error) -> {
                    });
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        return properties;
    }

    private void readSuccessful(ModbusRequest request, AsyncModbusReadResult result) {
        logger.debug("readSuccessful {}: {}", request, result);
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            int firstRegister = request.registers.getFirst().getRegisterNumber();

            for (SolakonOneInverterRegisters channel : request.registers) {
                int index = channel.getRegisterNumber() - firstRegister;
                logger.debug("{} {}", channel.toString(), ModbusBitUtilities
                        .extractStateFromRegisters(registers, index, channel.getType()).map(channel::createState));
                ModbusBitUtilities.extractStateFromRegisters(registers, index, channel.getType())
                        .map(channel::createState).ifPresentOrElse(v -> {
                            updateState(createChannelUid(channel), v);
                            if (channel.getChannelName().startsWith("hidden-")) {
                                logger.warn("Updated internal channel {} to {}", channel.getChannelName(), v);
                            }
                        }, () -> {
                            logger.warn("Could not extract state for channel {}", channel.getChannelName());
                        });
            }
        });
    }

    private void readError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        this.logger.debug("Failed to get modbus data", error.getCause());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Failed to retrieve data: " + error.getCause().getMessage());
    }

    private ChannelUID createChannelUid(SolakonOneInverterRegisters register) {
        return new ChannelUID(thing.getUID(), "fi-" + register.getChannelGroup(), "fi-" + register.getChannelName());
    }
}

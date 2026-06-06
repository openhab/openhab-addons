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
package org.openhab.binding.modbus.sungrow.internal;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

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
 * The {@link SungrowIHomeManagerHandler} is responsible for reading the modbus values of the
 * Sungrow iHomeManager.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class SungrowIHomeManagerHandler extends BaseModbusThingHandler {

    private static final class ModbusRequest {

        private final Deque<SungrowIHomeManagerRegisters> registers;
        private final ModbusReadRequestBlueprint blueprint;

        public ModbusRequest(Deque<SungrowIHomeManagerRegisters> registers, int slaveId, int tries) {
            this.registers = registers;
            this.blueprint = initReadRequest(registers, slaveId, tries);
        }

        private ModbusReadRequestBlueprint initReadRequest(Deque<SungrowIHomeManagerRegisters> registers, int slaveId,
                int tries) {
            int firstRegister = registers.getFirst().getRegisterNumber();
            int lastRegister = registers.getLast().getRegisterNumber();
            int length = lastRegister - firstRegister + registers.getLast().getRegisterCount();
            assert length <= ModbusConstants.MAX_REGISTERS_READ_COUNT;

            return new ModbusReadRequestBlueprint( //
                    slaveId, //
                    ModbusReadFunctionCode.READ_INPUT_REGISTERS, //
                    firstRegister - 1, //
                    length, //
                    tries //
            );
        }

        @Override
        public String toString() {
            return "first: " + registers.getFirst().getRegisterNumber() + ", last: "
                    + registers.getLast().getRegisterNumber();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(SungrowIHomeManagerHandler.class);

    private List<ModbusRequest> modbusRequests = new ArrayList<>();

    public SungrowIHomeManagerHandler(Thing thing) {
        super(thing);
    }

    /**
     * Splits the SungrowIHomeManagerRegisters into multiple ModbusRequest, to ensure the max request size.
     */
    private List<ModbusRequest> buildRequests(int tries) {
        final List<ModbusRequest> requests = new ArrayList<>();
        Deque<SungrowIHomeManagerRegisters> currentRequest = new ArrayDeque<>();
        int currentRequestFirstRegister = 0;

        for (SungrowIHomeManagerRegisters channel : SungrowIHomeManagerRegisters.values()) {
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
                } else {
                    currentRequest.add(channel);
                }
            }
        }

        if (!currentRequest.isEmpty()) {
            requests.add(new ModbusRequest(currentRequest, getSlaveId(), tries));
        }
        logger.debug("Created {} modbus request templates:\n\t{}", requests.size(),
                requests.stream().map(ModbusRequest::toString).collect(Collectors.joining("\n\t")));
        return requests;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType && !this.modbusRequests.isEmpty()) {
            for (ModbusRequest request : this.modbusRequests) {
                submitOneTimePoll( //
                        request.blueprint, //
                        (AsyncModbusReadResult result) -> this.readSuccessful(request, result), //
                        this::readError //
                );
            }
        }
    }

    @Override
    public void modbusInitialize() {
        final SungrowInverterConfiguration config = getConfigAs(SungrowInverterConfiguration.class);

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

        this.updateProperties(config);

        this.modbusRequests = this.buildRequests(config.maxTries);

        for (ModbusRequest request : modbusRequests) {
            registerRegularPoll( //
                    request.blueprint, //
                    config.pollInterval, //
                    0, //
                    (AsyncModbusReadResult result) -> this.readSuccessful(request, result), //
                    this::readError //
            );
        }
    }

    private void readSuccessful(ModbusRequest request, AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }

            int firstRegister = request.registers.getFirst().getRegisterNumber();

            for (SungrowIHomeManagerRegisters channel : request.registers) {
                int index = channel.getRegisterNumber() - firstRegister;

                ModbusBitUtilities.extractStateFromRegisters(registers, index, channel.getType())
                        .map(channel::createState).ifPresent(v -> updateState(createChannelUid(channel), v));
            }
        });
    }

    private void updateProperties(SungrowInverterConfiguration config) {
        ModbusReadRequestBlueprint getDeviceInfoRequest = new ModbusReadRequestBlueprint(this.getSlaveId(),
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, //
                7999, //
                8, //
                config.maxTries //
        );
        this.submitOneTimePoll(getDeviceInfoRequest, this::updateDeviceInfoProperties, this::readError);

        ModbusReadRequestBlueprint getApplicationVersionRequest = new ModbusReadRequestBlueprint(this.getSlaveId(),
                ModbusReadFunctionCode.READ_INPUT_REGISTERS, //
                8317, //
                15, //
                config.maxTries //
        );
        this.submitOneTimePoll(getApplicationVersionRequest, this::updateApplicationVersionProperties, this::readError);
    }

    private void updateDeviceInfoProperties(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            int deviceTypeCode = ModbusBitUtilities.extractUInt16(registers.getBytes(), 0);
            getThing().setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_DEVICE_TYPE_CODE,
                    String.valueOf(deviceTypeCode));

            long protocolNumber = ModbusBitUtilities.extractUInt32(registers.getBytes(), 2);
            getThing().setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_PROTOCOL_NUMBER,
                    String.valueOf(protocolNumber));

            long protocolVersion = ModbusBitUtilities.extractUInt32(registers.getBytes(), 6);
            getThing().setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_PROTOCOL_VERSION,
                    String.valueOf(protocolVersion));
        });
    }

    private void updateApplicationVersionProperties(AsyncModbusReadResult result) {
        result.getRegisters().ifPresent(registers -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            String softwareVersion = ModbusBitUtilities
                    .extractStringFromRegisters(registers, 0, 30, StandardCharsets.UTF_8).trim();
            getThing().setProperty(ModbusSungrowBindingConstants.PROP_KEY_IHM_APPLICATION_SOFTWARE_VERSION,
                    softwareVersion);
        });
    }

    private void readError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        this.logger.debug("Failed to get modbus data", error.getCause());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Failed to retrieve data: " + error.getCause().getMessage());
    }

    private ChannelUID createChannelUid(SungrowIHomeManagerRegisters register) {
        return new ChannelUID( //
                thing.getUID(), //
                "sg-" + register.getChannelGroup(), //
                "sg-" + register.getChannelName() //
        );
    }
}

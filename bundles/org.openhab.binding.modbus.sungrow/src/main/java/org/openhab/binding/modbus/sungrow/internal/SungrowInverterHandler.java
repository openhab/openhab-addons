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
package org.openhab.binding.modbus.sungrow.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

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
 * The {@link SungrowInverterHandler} is responsible for reading the modbus values of the
 * sungrow inverter.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class SungrowInverterHandler extends BaseModbusThingHandler {

    @NonNullByDefault
    private static final class ModbusRequest {

        private final Deque<SungrowInverterRegisters> registers;
        private final ModbusReadRequestBlueprint blueprint;

        public ModbusRequest(Deque<SungrowInverterRegisters> registers, int slaveId, int tries) {
            this.registers = registers;
            this.blueprint = initReadRequest(registers, slaveId, tries);
        }

        private ModbusReadRequestBlueprint initReadRequest(Deque<SungrowInverterRegisters> registers, int slaveId,
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
    }

    private final Logger logger = LoggerFactory.getLogger(SungrowInverterHandler.class);

    private List<ModbusRequest> modbusRequests = new ArrayList<>();

    public SungrowInverterHandler(Thing thing) {
        super(thing);
    }

    /**
     * Splits the SungrowInverterRegisters into multiple ModbusRequest, to ensure the max request size.
     */
    private List<ModbusRequest> buildRequests(int tries) {
        final List<ModbusRequest> requests = new ArrayList<>();
        Deque<SungrowInverterRegisters> currentRequest = new ArrayDeque<>();
        int currentRequestFirstRegister = 0;

        for (SungrowInverterRegisters channel : SungrowInverterRegisters.values()) {

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
        logger.debug("Created {} modbus request templates.", requests.size());
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

            for (SungrowInverterRegisters channel : request.registers) {
                int index = channel.getRegisterNumber() - firstRegister;

                ModbusBitUtilities.extractStateFromRegisters(registers, index, channel.getType())
                        .map(channel::createState).ifPresent(v -> updateState(createChannelUid(channel), v));
            }
        });
    }

    private void readError(AsyncModbusFailure<ModbusReadRequestBlueprint> error) {
        this.logger.debug("Failed to get modbus data", error.getCause());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Failed to retrieve data: " + error.getCause().getMessage());
    }

    private ChannelUID createChannelUid(SungrowInverterRegisters register) {
        return new ChannelUID( //
                thing.getUID(), //
                "sg-" + register.getChannelGroup(), //
                "sg-" + register.getChannelName() //
        );
    }
}

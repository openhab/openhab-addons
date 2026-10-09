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
package org.openhab.binding.modbus.hager.internal.modbus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RegisterPoller} reads the registers of one Thing with regular polls, decodes the answers and reports the
 * resulting channel states and the communication status to its {@link Listener}.
 * Individual read failures are logged at trace level, the poll status is logged when it changes. Every message carries
 * the Thing UID, so log files of installations with several Hager Flow devices stay attributable.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class RegisterPoller {

    /**
     * Communication status of the regular polls.
     */
    public enum PollStatus {
        /** Not every read block has answered yet. */
        PENDING,
        /** The last read of every block succeeded. */
        OK,
        /** At least one read block did not answer. */
        FAILED
    }

    /**
     * Receives the decoded register values and the communication status.
     */
    public interface Listener {

        /**
         * Called with the states decoded from one read block.
         *
         * @param updates the decoded channel states
         */
        void onUpdates(List<ChannelUpdate> updates);

        /**
         * Called when the communication status of the polls changes.
         *
         * @param status the new status
         */
        void onStatus(PollStatus status);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterPoller.class);

    private static final int MAX_TRIES = 3;

    private final String thingUid;
    private final int slaveId;
    private final long refreshMillis;
    private final Listener listener;
    private final Map<ReadRange, List<Register>> blocks = new LinkedHashMap<>();
    private final Map<ReadRange, Boolean> outcomes = new LinkedHashMap<>();
    private final List<PollTask> pollTasks = new ArrayList<>();
    private final List<Future<?>> oneTimeRequests = new ArrayList<>();
    private final Object statusLock = new Object();

    private volatile boolean running;
    private volatile @Nullable ModbusCommunicationInterface comms;
    private volatile @Nullable String lastFailure;
    private PollStatus status = PollStatus.PENDING;

    /**
     * @param thingUid UID of the Thing this poller belongs to, used to attribute the log messages
     * @param slaveId Modbus unit id to read from
     * @param refreshMillis poll interval in milliseconds
     * @param registers all registers of the Thing
     * @param listener receiver of the decoded values and the communication status
     */
    public RegisterPoller(String thingUid, int slaveId, long refreshMillis, List<Register> registers,
            Listener listener) {
        this.thingUid = thingUid;
        this.slaveId = slaveId;
        this.refreshMillis = refreshMillis;
        this.listener = listener;
        for (ReadRange range : ReadRange.of(registers)) {
            blocks.put(range, registers.stream().filter(range::contains).toList());
        }
    }

    /**
     * Starts the regular polls. Does nothing when the polls are already running.
     *
     * @param communicationInterface the communication interface to poll on
     */
    public void start(ModbusCommunicationInterface communicationInterface) {
        if (running) {
            LOGGER.debug("[{}] Polls for slave {} are already running", thingUid, slaveId);
            return;
        }
        comms = communicationInterface;
        running = true;
        for (Map.Entry<ReadRange, List<Register>> block : blocks.entrySet()) {
            ReadRange range = block.getKey();
            pollTasks.add(communicationInterface.registerRegularPoll(readRequest(range), refreshMillis, 0,
                    result -> handleResult(range, block.getValue(), result), failure -> handleFailure(range, failure)));
        }
        LOGGER.debug("[{}] Polling slave {} every {} ms with {} read blocks: {}", thingUid, slaveId, refreshMillis,
                blocks.size(), blocks.keySet());
    }

    /**
     * Stops the regular polls and prevents further state updates.
     */
    public void stop() {
        running = false;
        ModbusCommunicationInterface communicationInterface = comms;
        if (communicationInterface != null) {
            for (PollTask task : pollTasks) {
                try {
                    communicationInterface.unregisterRegularPoll(task);
                } catch (IllegalStateException e) {
                    LOGGER.debug("[{}] Communication interface is closed already: {}", thingUid, e.getMessage());
                }
            }
        }
        pollTasks.clear();
        oneTimeRequests.forEach(future -> future.cancel(true));
        oneTimeRequests.clear();
        synchronized (statusLock) {
            outcomes.clear();
            status = PollStatus.PENDING;
        }
        comms = null;
        LOGGER.debug("[{}] Stopped polling slave {}", thingUid, slaveId);
    }

    /**
     * Reads the block containing the given register once, without waiting for the next regular poll.
     *
     * @param register the register to refresh
     */
    public void refresh(Register register) {
        ModbusCommunicationInterface communicationInterface = comms;
        if (!running || communicationInterface == null) {
            LOGGER.debug("[{}] Not refreshing {}, polls are not running", thingUid, register.channelId());
            return;
        }
        for (Map.Entry<ReadRange, List<Register>> block : blocks.entrySet()) {
            ReadRange range = block.getKey();
            if (range.contains(register)) {
                LOGGER.debug("[{}] Refreshing {} on request of channel {}", thingUid, range, register.channelId());
                oneTimeRequests.add(communicationInterface.submitOneTimePoll(readRequest(range),
                        result -> handleResult(range, block.getValue(), result),
                        failure -> handleFailure(range, failure)));
                oneTimeRequests.removeIf(Future::isDone);
                return;
            }
        }
        LOGGER.debug("[{}] No read block contains register {}", thingUid, register.channelId());
    }

    /**
     * Writes a single register.
     *
     * @param register the register to write
     * @param value the raw value to write
     */
    public void write(Register register, int value) {
        ModbusCommunicationInterface communicationInterface = comms;
        if (!running || communicationInterface == null) {
            LOGGER.debug("[{}] Not writing {}, polls are not running", thingUid, register.channelId());
            return;
        }
        LOGGER.debug("[{}] Writing {} to {} at address {}", thingUid, value, register.channelId(), register.address());
        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId,
                register.address(), new ModbusRegisterArray(value), false, MAX_TRIES);
        oneTimeRequests.add(communicationInterface.submitOneTimeWrite(request,
                result -> LOGGER.debug("[{}] Wrote {} to {}", thingUid, value, register.channelId()),
                failure -> LOGGER.debug("[{}] Writing {} to {} failed: {}", thingUid, value, register.channelId(),
                        failure.getCause().getMessage())));
        oneTimeRequests.removeIf(Future::isDone);
    }

    private ModbusReadRequestBlueprint readRequest(ReadRange range) {
        return new ModbusReadRequestBlueprint(slaveId, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, range.start(),
                range.length(), MAX_TRIES);
    }

    private void handleResult(ReadRange range, List<Register> blockRegisters, AsyncModbusReadResult result) {
        if (!running) {
            return;
        }
        Optional<ModbusRegisterArray> registers = result.getRegisters();
        if (registers.isEmpty()) {
            LOGGER.debug("[{}] Slave {} returned no registers for {}", thingUid, slaveId, range);
            setOutcome(range, false);
            return;
        }
        ModbusRegisterArray array = registers.get();
        if (array.size() < range.length()) {
            LOGGER.debug("[{}] Slave {} returned {} of {} registers for {}", thingUid, slaveId, array.size(),
                    range.length(), range);
            setOutcome(range, false);
            return;
        }
        LOGGER.trace("[{}] Slave {} answered {} with {}", thingUid, slaveId, range, array.toHexString());
        List<ChannelUpdate> updates = new ArrayList<>();
        for (Register register : blockRegisters) {
            updates.addAll(RegisterDecoder.decode(thingUid, register, array, range.start()));
        }
        if (!updates.isEmpty()) {
            listener.onUpdates(updates);
        }
        setOutcome(range, true);
    }

    private void handleFailure(ReadRange range, AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        if (!running) {
            return;
        }
        Exception cause = failure.getCause();
        lastFailure = cause.getMessage();
        LOGGER.trace("[{}] Slave {} failed to answer {}", thingUid, slaveId, range, cause);
        setOutcome(range, false);
    }

    private void setOutcome(ReadRange range, boolean success) {
        PollStatus newStatus;
        long failed;
        synchronized (statusLock) {
            outcomes.put(range, success);
            failed = outcomes.values().stream().filter(Boolean.FALSE::equals).count();
            if (outcomes.size() < blocks.size()) {
                newStatus = PollStatus.PENDING;
            } else {
                newStatus = failed > 0 ? PollStatus.FAILED : PollStatus.OK;
            }
            if (newStatus == status) {
                return;
            }
            status = newStatus;
        }
        switch (newStatus) {
            case PENDING -> LOGGER.debug("[{}] Waiting for the first answer of slave {}", thingUid, slaveId);
            case OK -> LOGGER.debug("[{}] Slave {} answered all {} read blocks", thingUid, slaveId, blocks.size());
            case FAILED -> LOGGER.debug("[{}] {} of {} read blocks of slave {} did not answer, last error: {}",
                    thingUid, failed, blocks.size(), slaveId, String.valueOf(lastFailure));
        }
        listener.onStatus(newStatus);
    }
}

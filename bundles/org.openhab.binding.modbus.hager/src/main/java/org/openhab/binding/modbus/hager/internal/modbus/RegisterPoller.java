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
 * <p>
 * A poller is used once: a handler creates a new instance for every start and stops the previous one. All lifecycle
 * operations are serialized by one lock, so that {@link #stop()} can neither miss a poll registered by a concurrent
 * {@link #start(ModbusCommunicationInterface)} nor let a late response publish its values. For that reason the decoded
 * values and the poll status are delivered while the lock is held: the listener callbacks are one-way state updates
 * which do not call back into the poller, and no Modbus request is submitted while the lock is held.
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
    private final List<PollTask> pollTasks = new ArrayList<>();
    private final List<Future<?>> oneTimeRequests = new ArrayList<>();
    private final Map<ReadRange, Boolean> outcomes = new LinkedHashMap<>();
    private final Object lock = new Object();

    private @Nullable ModbusCommunicationInterface comms;
    private @Nullable String lastFailure;
    private PollStatus status = PollStatus.PENDING;
    // guarded by lock, not volatile: every read happens while holding the lock so that a check and the action
    // which depends on it observe the same lifecycle state
    private boolean running;
    private boolean closed;

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
     * Starts the regular polls. Does nothing when the polls are already running or when the poller was stopped.
     * Registering the polls and {@link #stop()} are mutually exclusive, therefore a stop can never leave a poll of a
     * concurrent start behind.
     *
     * @param communicationInterface the communication interface to poll on
     */
    public void start(ModbusCommunicationInterface communicationInterface) {
        synchronized (lock) {
            if (running || closed) {
                LOGGER.debug("[{}] Polls for slave {} are already running or stopped already", thingUid, slaveId);
                return;
            }
            comms = communicationInterface;
            running = true;
            try {
                for (Map.Entry<ReadRange, List<Register>> block : blocks.entrySet()) {
                    ReadRange range = block.getKey();
                    pollTasks.add(communicationInterface.registerRegularPoll(readRequest(range), refreshMillis, 0,
                            result -> handleResult(range, block.getValue(), result),
                            failure -> handleFailure(range, failure)));
                }
            } catch (IllegalStateException e) {
                // the endpoint closed the interface before the polls were registered; close() unregisters them again
                LOGGER.debug("[{}] Communication interface is closed already: {}", thingUid, e.getMessage());
                running = false;
                comms = null;
                pollTasks.clear();
                return;
            }
        }
        LOGGER.debug("[{}] Polling slave {} every {} ms with {} read blocks: {}", thingUid, slaveId, refreshMillis,
                blocks.size(), blocks.keySet());
    }

    /**
     * Stops the regular polls and prevents any further state update. Responses which are still in flight are
     * discarded, so a stopped poller cannot publish values or a poll status any more.
     */
    public void stop() {
        List<PollTask> tasks;
        List<Future<?>> requests;
        ModbusCommunicationInterface communicationInterface;
        synchronized (lock) {
            if (closed) {
                return;
            }
            closed = true;
            running = false;
            communicationInterface = comms;
            comms = null;
            tasks = List.copyOf(pollTasks);
            pollTasks.clear();
            requests = List.copyOf(oneTimeRequests);
            oneTimeRequests.clear();
            outcomes.clear();
            status = PollStatus.PENDING;
        }
        if (communicationInterface != null) {
            for (PollTask task : tasks) {
                try {
                    communicationInterface.unregisterRegularPoll(task);
                } catch (IllegalStateException e) {
                    LOGGER.debug("[{}] Communication interface is closed already: {}", thingUid, e.getMessage());
                }
            }
        }
        requests.forEach(future -> future.cancel(true));
        LOGGER.debug("[{}] Stopped polling slave {}", thingUid, slaveId);
    }

    /**
     * Reads the block containing the given register once, without waiting for the next regular poll.
     *
     * @param register the register to refresh
     */
    public void refresh(Register register) {
        ModbusCommunicationInterface communicationInterface;
        Optional<Map.Entry<ReadRange, List<Register>>> target;
        synchronized (lock) {
            ModbusCommunicationInterface current = comms;
            if (!running || current == null) {
                LOGGER.debug("[{}] Not refreshing {}, polls are not running", thingUid, register.channelId());
                return;
            }
            communicationInterface = current;
            target = blocks.entrySet().stream().filter(block -> block.getKey().contains(register)).findFirst();
        }
        if (target.isEmpty()) {
            LOGGER.debug("[{}] No read block contains register {}", thingUid, register.channelId());
            return;
        }
        ReadRange range = target.get().getKey();
        List<Register> blockRegisters = target.get().getValue();
        LOGGER.debug("[{}] Refreshing {} on request of channel {}", thingUid, range, register.channelId());
        Future<?> future;
        try {
            future = communicationInterface.submitOneTimePoll(readRequest(range),
                    result -> handleResult(range, blockRegisters, result), failure -> handleFailure(range, failure));
        } catch (IllegalStateException e) {
            LOGGER.debug("[{}] Communication interface is closed already: {}", thingUid, e.getMessage());
            return;
        }
        track(future);
    }

    /**
     * Writes a single register.
     *
     * @param register the register to write
     * @param value the raw value to write
     */
    public void write(Register register, int value) {
        ModbusCommunicationInterface communicationInterface;
        synchronized (lock) {
            ModbusCommunicationInterface current = comms;
            if (!running || current == null) {
                LOGGER.debug("[{}] Not writing {}, polls are not running", thingUid, register.channelId());
                return;
            }
            communicationInterface = current;
        }
        LOGGER.debug("[{}] Writing {} to {} at address {}", thingUid, value, register.channelId(), register.address());
        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId,
                register.address(), new ModbusRegisterArray(value), false, MAX_TRIES);
        Future<?> future;
        try {
            future = communicationInterface.submitOneTimeWrite(request,
                    result -> LOGGER.debug("[{}] Wrote {} to {}", thingUid, value, register.channelId()),
                    failure -> LOGGER.debug("[{}] Writing {} to {} failed: {}", thingUid, value, register.channelId(),
                            failure.getCause().getMessage()));
        } catch (IllegalStateException e) {
            LOGGER.debug("[{}] Communication interface is closed already: {}", thingUid, e.getMessage());
            return;
        }
        track(future);
    }

    /**
     * Registers a submitted one-time request for cancellation, unless the poller was stopped in the meantime. A
     * request which is submitted while {@link #stop()} runs would otherwise never be cancelled.
     *
     * @param future the request to track
     */
    private void track(Future<?> future) {
        boolean discard;
        synchronized (lock) {
            discard = closed;
            if (!discard) {
                oneTimeRequests.add(future);
                oneTimeRequests.removeIf(Future::isDone);
            }
        }
        if (discard) {
            future.cancel(true);
        }
    }

    private ModbusReadRequestBlueprint readRequest(ReadRange range) {
        return new ModbusReadRequestBlueprint(slaveId, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, range.start(),
                range.length(), MAX_TRIES);
    }

    private void handleResult(ReadRange range, List<Register> blockRegisters, AsyncModbusReadResult result) {
        synchronized (lock) {
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
    }

    private void handleFailure(ReadRange range, AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        synchronized (lock) {
            if (!running) {
                return;
            }
            Exception cause = failure.getCause();
            lastFailure = cause.getMessage();
            LOGGER.trace("[{}] Slave {} failed to answer {}", thingUid, slaveId, range, cause);
            setOutcome(range, false);
        }
    }

    /**
     * Aggregates the outcome of the read block and reports the poll status when it changed. Called with the lock held.
     *
     * @param range the read block the outcome belongs to
     * @param success whether the read block answered
     */
    private void setOutcome(ReadRange range, boolean success) {
        outcomes.put(range, success);
        long failed = outcomes.values().stream().filter(Boolean.FALSE::equals).count();
        PollStatus newStatus;
        if (outcomes.size() < blocks.size()) {
            newStatus = PollStatus.PENDING;
        } else {
            newStatus = failed > 0 ? PollStatus.FAILED : PollStatus.OK;
        }
        if (newStatus == status) {
            return;
        }
        status = newStatus;
        switch (newStatus) {
            case PENDING -> LOGGER.debug("[{}] Waiting for the first answer of slave {}", thingUid, slaveId);
            case OK -> LOGGER.debug("[{}] Slave {} answered all {} read blocks", thingUid, slaveId, blocks.size());
            case FAILED -> LOGGER.debug("[{}] {} of {} read blocks of slave {} did not answer, last error: {}",
                    thingUid, failed, blocks.size(), slaveId, String.valueOf(lastFailure));
        }
        listener.onStatus(newStatus);
    }
}

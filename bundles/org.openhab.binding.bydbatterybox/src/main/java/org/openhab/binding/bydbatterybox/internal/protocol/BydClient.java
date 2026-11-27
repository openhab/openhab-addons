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
package org.openhab.binding.bydbatterybox.internal.protocol;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;
import org.openhab.binding.bydbatterybox.internal.dto.SystemStatus;
import org.openhab.binding.bydbatterybox.internal.dto.TowerData;
import org.openhab.binding.bydbatterybox.internal.protocol.BydCommunicationException.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The request sequences of the battery protocol, executed on one open {@link BydConnection}.
 * <p>
 * Cell details and event logs are requested by writing {@code [unit, 0x8100]} to a command register. The battery then
 * sets the following register to {@code 0x88xx} once the data is ready; reading the data earlier is answered with
 * exception 0x04. The data is fetched by reading the same register block repeatedly; each read returns the next
 * 130-byte chunk, whose first register holds the payload length (128).
 * <p>
 * A measurement returns at most 128 cell voltages. Writing the text {@code DEBUG} to register 0x0100 makes the next
 * measurement of a tower a second pass that returns the cells beyond 128; the following measurement is a normal
 * first pass again.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BydClient {

    /** Pauses the calling thread; replaced in tests. */
    @FunctionalInterface
    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    static final int CHUNK_PAYLOAD_LENGTH = 128;

    private static final int REG_IDENTITY = 0x0000;
    private static final int REG_IDENTITY_COUNT = 102;
    private static final int REG_STATUS = 0x0500;
    private static final int REG_STATUS_COUNT = 25;
    private static final int REG_MEASUREMENT_COMMAND = 0x0550;
    private static final int REG_MEASUREMENT_STATE = 0x0551;
    private static final int REG_MEASUREMENT_DATA = 0x0558;
    private static final int REG_EVENT_COMMAND = 0x05A0;
    private static final int REG_EVENT_STATE = 0x05A1;
    private static final int REG_EVENT_DATA = 0x05A8;
    private static final int REG_SECOND_PASS_COMMAND = 0x0100;
    private static final int CHUNK_REGISTER_COUNT = 0x41;
    private static final int MEASUREMENT_CHUNKS = 4;
    /** The second pass holds data only in the first two chunks. */
    private static final int SECOND_PASS_CHUNKS = 2;
    private static final int EVENT_LOG_CHUNKS = 5;
    /** ASCII {@code "DEBUG\0"}. */
    private static final int[] SECOND_PASS_COMMAND = { 0x4445, 0x4255, 0x4700 };

    private static final int COMMAND_START = 0x8100;
    private static final int STATE_READY_MASK = 0xFF00;
    private static final int STATE_READY = 0x8800;

    /**
     * Gap before every request. All hardware tests were done with this gap (as used by ioBroker.bydhvs); it is short
     * compared to a cycle.
     */
    private static final long INTER_FRAME_DELAY_MILLIS = 200;
    /**
     * A measurement was observed to complete after 0.8 to 1.3 s; the second pass of a tower with 160 cells after 1 to
     * 2 s.
     */
    private static final long READY_FIRST_CHECK_MILLIS = 1000;
    private static final long READY_POLL_MILLIS = 500;
    private static final long READY_TIMEOUT_MILLIS = 10_000;

    private final Logger logger = LoggerFactory.getLogger(BydClient.class);
    private final BydConnection connection;
    private final Sleeper sleeper;

    public BydClient(BydConnection connection) {
        this(connection, Thread::sleep);
    }

    public BydClient(BydConnection connection, Sleeper sleeper) {
        this.connection = connection;
        this.sleeper = sleeper;
    }

    public SystemInfo readIdentity() throws BydCommunicationException, InterruptedException {
        return BydDecoder.decodeIdentity(transact(ModbusFrame.readHoldingRegisters(REG_IDENTITY, REG_IDENTITY_COUNT)));
    }

    public SystemStatus readStatus() throws BydCommunicationException, InterruptedException {
        return BydDecoder.decodeStatus(transact(ModbusFrame.readHoldingRegisters(REG_STATUS, REG_STATUS_COUNT)));
    }

    /**
     * Triggers a cell measurement of one tower, waits for it to complete and reads the result.
     *
     * @param extendedCells also read the cells beyond 128 in a second pass, if the tower has any
     */
    public TowerData readTower(int tower, SystemInfo info, boolean extendedCells)
            throws BydCommunicationException, InterruptedException {
        TowerData data = BydDecoder.decodeMeasurement(tower, measure(tower, MEASUREMENT_CHUNKS), info);
        if (!extendedCells || info.secondPassCellsPerTower() == 0) {
            return data;
        }
        transact(ModbusFrame.writeMultipleRegisters(REG_SECOND_PASS_COMMAND, SECOND_PASS_COMMAND));
        byte[] buffer = measure(tower, SECOND_PASS_CHUNKS);
        try {
            return BydDecoder.decodeSecondPass(data, buffer, info);
        } catch (BydCommunicationException e) {
            // keep the first pass: the cells beyond 128 are reported as unknown, the other values are valid
            logger.debug("Ignoring the second measurement of tower {}: {}", tower, e.getMessage());
            return data;
        }
    }

    private byte[] measure(int tower, int chunks) throws BydCommunicationException, InterruptedException {
        transact(ModbusFrame.writeMultipleRegisters(REG_MEASUREMENT_COMMAND, tower, COMMAND_START));
        awaitReady(REG_MEASUREMENT_STATE);
        return readChunks(REG_MEASUREMENT_DATA, chunks);
    }

    /**
     * Loads and reads the event log of one unit ({@link EventRecord#UNIT_BMU} or a tower number).
     *
     * @return the entries, newest first; empty if the unit has no log data
     */
    public List<EventRecord> readEventLog(int unit) throws BydCommunicationException, InterruptedException {
        transact(ModbusFrame.writeMultipleRegisters(REG_EVENT_COMMAND, unit, COMMAND_START));
        int state = awaitReady(REG_EVENT_STATE);
        if ((state & 0xFF) == 0) {
            return List.of();
        }
        return BydDecoder.decodeEventLog(unit, readChunks(REG_EVENT_DATA, EVENT_LOG_CHUNKS));
    }

    private int awaitReady(int stateRegister) throws BydCommunicationException, InterruptedException {
        byte[] request = ModbusFrame.readHoldingRegisters(stateRegister, 1);
        long waited = READY_FIRST_CHECK_MILLIS;
        sleeper.sleep(READY_FIRST_CHECK_MILLIS);
        while (true) {
            int state = BydDecoder.u16(transact(request), 3);
            if ((state & STATE_READY_MASK) == STATE_READY) {
                return state;
            }
            if (waited >= READY_TIMEOUT_MILLIS) {
                throw new BydCommunicationException(Reason.NOT_READY,
                        String.format("register 0x%04x still 0x%04x after %d ms", stateRegister, state, waited));
            }
            sleeper.sleep(READY_POLL_MILLIS);
            waited += READY_POLL_MILLIS;
        }
    }

    private byte[] readChunks(int register, int chunks) throws BydCommunicationException, InterruptedException {
        byte[] request = ModbusFrame.readHoldingRegisters(register, CHUNK_REGISTER_COUNT);
        byte[] buffer = new byte[chunks * CHUNK_PAYLOAD_LENGTH];
        for (int c = 0; c < chunks; c++) {
            byte[] reply = transact(request);
            // skip address, function code, byte count and the leading length register
            System.arraycopy(reply, 5, buffer, c * CHUNK_PAYLOAD_LENGTH, CHUNK_PAYLOAD_LENGTH);
        }
        return buffer;
    }

    private byte[] transact(byte[] request) throws BydCommunicationException, InterruptedException {
        sleeper.sleep(INTER_FRAME_DELAY_MILLIS);
        return connection.transact(request);
    }
}

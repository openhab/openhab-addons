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
package org.openhab.binding.insteon.internal.device.database;

import java.io.ByteArrayInputStream;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.message.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkDBWriter} manages all-link database write requests
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBWriter implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(LinkDBWriter.class);

    private InsteonDevice device = new InsteonDevice();
    private InsteonModem modem;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private ByteArrayInputStream stream = new ByteArrayInputStream(new byte[0]);
    private volatile boolean done = true;
    private volatile long lastMsgReceived;
    private volatile int location;
    private volatile int lastMSB;

    public LinkDBWriter(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;
    }

    public void write(InsteonDevice device) {
        logger.debug("starting link database writer for {}", device.getAddress());

        this.device = device;

        applyChanges();
    }

    private void applyChanges() {
        done = false;
        modem.getPort().registerListener(this);

        switch (device.getLinkDB().getReadWriteMode()) {
            case STANDARD:
                setNextAllLinkRecord();
                break;
            case PEEK_POKE:
                setNextPokeRecord();
                break;
            case UNKNOWN:
                logger.debug("unsupported database read/write mode for {}, aborting", device.getAddress());
                done();
        }
    }

    public void stop() {
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        modem.getPort().unregisterListener(this);
    }

    private void done() {
        logger.debug("link database writer finished for {}", device.getAddress());
        done = true;
        stop();
        device.getLinkDB().load();
        modem.getDBM().operationCompleted();
    }

    private void setNextAllLinkRecord() {
        LinkDBChange change = device.getLinkDB().pollNextChange();
        if (change == null) {
            logger.debug("all link db changes written using standard mode for {}", device.getAddress());
            done();
        } else {
            setAllLinkRecord(change.getRecord());
        }
    }

    private void setNextPokeRecord() {
        LinkDBChange change = device.getLinkDB().pollNextChange();
        if (change == null) {
            logger.debug("all link db changes written using peek/poke mode for {}", device.getAddress());
            done();
        } else {
            setPokeRecord(change.getRecord());
        }
    }

    private void setPokeRecord(LinkDBRecord record) {
        stream = new ByteArrayInputStream(record.getBytes());
        location = record.getLocation();
        lastMSB = -1;
        setNextPokeByte();
    }

    private void setNextPokeByte() {
        int address = location - stream.available() + 1;
        int msb = address >> 8;
        int lsb = address & 0xFF;

        if (stream.available() == 0) {
            setNextPokeRecord();
        } else if (msb != lastMSB) {
            setMSBAddress(msb);
            lastMSB = msb;
        } else {
            getPeekByte(lsb);
        }
    }

    private void setMSBAddress(int msb) {
        try {
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x28, (byte) msb);
            msg.setPriority(Priority.DATABASE);
            modem.writeMessage(msg);
        } catch (FieldException | InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void setPokeByte(int value) {
        try {
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x29, (byte) value);
            msg.setPriority(Priority.DATABASE);
            modem.writeMessage(msg);
        } catch (FieldException | InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void getPeekByte(int lsb) {
        try {
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x2B, (byte) lsb);
            msg.setPriority(Priority.DATABASE);
            modem.writeMessage(msg);
        } catch (FieldException | InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void setAllLinkRecord(LinkDBRecord record) {
        try {
            Msg msg = Msg.makeExtendedMessage(device.getAddress(), (byte) 0x2F, (byte) 0x00, false);
            msg.setByte("userData1", (byte) 0x00);
            msg.setByte("userData2", (byte) 0x02);
            msg.setByte("userData3", (byte) (record.getLocation() >> 8));
            msg.setByte("userData4", (byte) (record.getLocation() & 0xFF));
            msg.setByte("userData5", (byte) 0x08);
            msg.setByte("userData6", (byte) record.getFlags());
            msg.setByte("userData7", (byte) record.getGroup());
            msg.setBytes("userData8", record.getAddress().getBytes());
            msg.setBytes("userData11", record.getData());
            if (device.getInsteonEngine().supportsChecksum()) {
                msg.setCRC();
            }
            msg.setPriority(Priority.DATABASE);
            modem.writeMessage(msg);
        } catch (FieldException | InvalidMessageTypeException e) {
            logger.warn("error creating message", e);
        }
    }

    private void startAbortTimer() {
        logger.trace("starting abort timer for {}", device.getAddress());

        lastMsgReceived = System.currentTimeMillis();

        job = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastMsgReceived > DatabaseManager.MESSAGE_TIMEOUT) {
                logger.debug("link database writer timed out for {}, aborting", device.getAddress());
                done();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnected() {
        if (!done) {
            logger.debug("port disconnected, aborting");
            done();
        }
    }

    @Override
    public void messageReceived(Msg msg) {
        if (!msg.isFromAddress(device.getAddress())) {
            return;
        }
        lastMsgReceived = msg.getTimestamp();

        try {
            if (msg.getCommand() == 0x50 && (msg.getByte("command1") == 0x28 || msg.getByte("command1") == 0x29)) {
                // we got a set msb address or poke byte response
                setNextPokeByte();
            } else if (msg.getCommand() == 0x50 && msg.getByte("command1") == 0x2B) {
                // we got a get peek byte response
                handlePeekByte(msg.getByte("command2"));
            } else if (msg.getCommand() == 0x50 && msg.getByte("command1") == 0x2F) {
                // we got a set aldb record response
                setNextAllLinkRecord();
            }
        } catch (FieldException e) {
            logger.warn("error parsing message", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        if (!msg.isToAddress(device.getAddress())) {
            return;
        }

        try {
            if (msg.getCommand() == 0x62 && (msg.getByte("command1") == 0x28 || msg.getByte("command1") == 0x2F)) {
                // we sent a set msb address or set aldb record message
                if (!done && job == null) {
                    startAbortTimer();
                }
            }
        } catch (FieldException e) {
            logger.warn("error parsing message", e);
        }
    }

    private void handlePeekByte(byte b) {
        // read next record stream byte
        int value = stream.read();
        // set poke byte if value defined and different from existing one, otherise set next poke byte
        if (value != -1 && value != b) {
            setPokeByte(value);
        } else {
            setNextPokeByte();
        }
    }
}

/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkDBReader} manages all-link database read requests
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBReader implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(LinkDBReader.class);

    private InsteonDevice device = new InsteonDevice();
    private InsteonModem modem;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private boolean done = true;
    private long lastMsgReceived;
    private int location;
    private int lastMSB;

    public LinkDBReader(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;
    }

    public boolean isRunning() {
        return job != null;
    }

    public void read(InsteonDevice device) {
        logger.debug("starting link database reader for {}", device.getAddress());

        this.device = device;

        getAllRecords();

        job = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastMsgReceived > DatabaseManager.MESSAGE_TIMEOUT) {
                logger.debug("link database reader timed out for {}, aborting", device.getAddress());
                done();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void getAllRecords() {
        lastMsgReceived = System.currentTimeMillis();
        done = false;

        modem.getPort().registerListener(this);

        switch (device.getLinkDB().getReadWriteMode()) {
            case STANDARD:
                getAllLinkRecords();
                break;
            case PEEK_POKE:
                getPeekRecords();
                break;
            case UNKNOWN:
                logger.debug("unsupported database read/write mode for {}, aborting", device.getAddress());
                done();
        }
    }

    public void stop() {
        logger.debug("link database reader finished for {}", device.getAddress());

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        modem.getPort().unregisterListener(this);
        modem.getDBM().operationCompleted();
    }

    private void done() {
        device.getLinkDB().recordsLoaded();
        done = true;
        stop();
    }

    private void getPeekRecords() {
        location = device.getLinkDB().getFirstRecordLocation();
        lastMSB = -1;
        getNextPeekRecord();
    }

    private void getNextPeekRecord() {
        stream.reset();
        getNextPeekByte();
    }

    private void getNextPeekByte() {
        int address = location - stream.size();
        int msb = address >> 8;
        int lsb = address & 0xFF;

        if (msb != lastMSB) {
            setMSBAddress(msb);
            lastMSB = msb;
        } else {
            getPeekByte(lsb);
        }
    }

    private void setMSBAddress(int msb) {
        try {
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x28, (byte) msb);
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending set msb address query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
    }

    private void getPeekByte(int lsb) {
        try {
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x2B, (byte) lsb);
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending peek query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
    }

    private void getAllLinkRecords() {
        try {
            Msg msg = Msg.makeExtendedMessage(device.getAddress(), (byte) 0x2F, (byte) 0x00,
                    device.getInsteonEngine().supportsChecksum());
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending get all link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
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
        try {
            if (!msg.isFromAddress(device.getAddress())) {
                return;
            }
            lastMsgReceived = msg.getTimestamp();

            if (msg.getCommand() == 0x50 && msg.getByte("command1") == 0x28) {
                // we got a set msb address response
                getNextPeekByte();
            } else if (msg.getCommand() == 0x50 && msg.getByte("command1") == 0x2B) {
                // we got a get peek byte response
                handleRecordByte(msg.getByte("command2"));
            } else if (msg.getCommand() == 0x51 && msg.getByte("command1") == 0x2F) {
                // we got a get aldb record response
                handleRecordMsg(msg);
            }
        } catch (FieldException e) {
            logger.warn("error parsing link db info reply field ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }

    private void addRecord(LinkDBRecord record) {
        if (device.getLinkDB().addRecord(record) != null) {
            logger.trace("got duplicate link db record for {}", device.getAddress());
            return;
        }

        logger.trace("got link db record #{} for {}", device.getLinkDB().size(), device.getAddress());

        if (record.isLast()) {
            logger.trace("got last link db record for {}", device.getAddress());
            done();
        }
    }

    private void handleRecordByte(byte b) {
        // add byte to record stream
        stream.write(b);
        // get next peek byte if stream size below the record byte size
        // otherwise add record and get next peek record if not done
        if (stream.size() < LinkDB.RECORD_BYTE_SIZE) {
            getNextPeekByte();
        } else {
            addRecord(LinkDBRecord.fromRecordData(stream.toByteArray(), location));
            if (!done) {
                location -= LinkDB.RECORD_BYTE_SIZE;
                getNextPeekRecord();
            }
        }
    }

    private void handleRecordMsg(Msg msg) throws FieldException {
        // check if message crc is valid based on device insteon engine checksum support
        if (device.getInsteonEngine().supportsChecksum() && !msg.hasValidCRC()) {
            logger.debug("ignoring msg with invalid crc from {}: {}", device.getAddress(), msg);
        } else {
            addRecord(LinkDBRecord.fromRecordMsg(msg));
        }
    }
}

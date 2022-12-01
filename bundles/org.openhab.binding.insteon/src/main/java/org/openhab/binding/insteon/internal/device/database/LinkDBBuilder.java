/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.insteon.internal.device.InsteonEngine;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.PortListener;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the all-link database from incoming link record messages for a given device
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkDBBuilder implements PortListener {
    private static final int DOWNLOAD_TIMEOUT = 30000; // in milliseconds

    private final Logger logger = LoggerFactory.getLogger(LinkDBBuilder.class);

    private volatile boolean done;
    private Driver driver;
    private ScheduledExecutorService scheduler;
    private InsteonDevice device = new InsteonDevice();
    private @Nullable ScheduledFuture<?> job;
    private ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private int count;
    private int offset;

    public LinkDBBuilder(Driver driver, ScheduledExecutorService scheduler) {
        this.driver = driver;
        this.scheduler = scheduler;
    }

    public InsteonDevice getDevice() {
        return device;
    }

    private void setDevice(InsteonDevice device) {
        this.device = device;
    }

    private boolean isDone() {
        return done;
    }

    public boolean isRunning() {
        return job != null;
    }

    public void start(InsteonDevice device, long delay) {
        long startTime = System.currentTimeMillis() + delay;
        logger.debug("starting link db builder for {}", device.getAddress());
        driver.addPortListener(this);
        setDevice(device);
        startDownload(delay);
        job = scheduler.scheduleWithFixedDelay(() -> {
            if (isDone()) {
                stop();
            } else if (System.currentTimeMillis() - startTime > DOWNLOAD_TIMEOUT) {
                logger.debug("link database download timeout for {}, aborting", device.getAddress());
                done();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.debug("link db builder finished for {}", device.getAddress());
        driver.removePortListener(this);
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(false);
            this.job = null;
        }
    }

    private void startDownload(long delay) {
        logger.trace("starting link db download for {}", device.getAddress());
        device.getLinkDB().clearRecords();
        device.getLinkDB().setDatabaseDelta(-1);
        device.getLinkDB().setRefresh(false);
        done = false;
        count = 0;
        offset = device.getLinkDB().getFirstRecordOffset();
        scheduler.schedule(() -> {
            // pause request queue manager
            driver.getRequestQueueManager().pause();
            // request for records depending on the device insteon engine
            if (device.getInsteonEngine() == InsteonEngine.I1) {
                getPeekRecord();
            } else {
                getAllLinkRecords();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void getAllLinkRecords() {
        try {
            boolean setCRC = device.getInsteonEngine().supportsChecksum();
            Msg msg = Msg.makeExtendedMessage(device.getAddress(), (byte) 0x2F, (byte) 0x00, setCRC);
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending all link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
    }

    private void getPeekRecord() {
        // reset record stream
        stream.reset();
        // set msb address if current offset lsb is 0xFF, otherwise get peek byte
        if ((offset & 0xFF) == 0xFF) {
            setMSBAddress();
        } else {
            getPeekByte();
        }
    }

    private void getPeekByte() {
        try {
            int lsb = getByteAddress() & 0xFF;
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x2B, (byte) lsb);
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending peek extended query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
    }

    private void setMSBAddress() {
        try {
            int msb = getByteAddress() >> 8;
            Msg msg = Msg.makeStandardMessage(device.getAddress(), (byte) 0x28, (byte) msb);
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending set msb address query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        } catch (FieldException e) {
            logger.warn("error parsing message ", e);
        }
    }

    private int getByteAddress() {
        return offset - stream.size();
    }

    @Override
    public void disconnected() {
        if (!isDone()) {
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
            if (msg.getByte("Cmd") == 0x50 && msg.getByte("command1") == 0x28) {
                // we got a set msb address response
                getPeekByte();
            } else if (msg.getByte("Cmd") == 0x50 && msg.getByte("command1") == 0x2B) {
                // we got a peek byte response
                handleRecordByte(msg.getByte("command2"));
            } else if (msg.getByte("Cmd") == 0x51 && msg.getByte("command1") == 0x2F) {
                // we got an aldb record response
                handleRecordMsg(msg);
            } else if (msg.getByte("Cmd") == 0x5C && msg.getByte("command1") == 0x2F) {
                logger.debug("got a failure reply for {}, aborting", device.getAddress());
                done();
            }
        } catch (FieldException e) {
            logger.warn("error parsing link db info reply field ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }

    private void done() {
        driver.getRequestQueueManager().resume();
        device.getLinkDB().updateStatus();
        device.getLinkDB().logRecords();
        device.linkDBUpdated();
        done = true;
    }

    private void addRecord(LinkDBRecord record) {
        if (device.getLinkDB().addRecord(record)) {
            logger.trace("got link db record #{} for {}", ++count, device.getAddress());
            // complete download if last record
            if (record.isLast()) {
                logger.trace("got last link db record for {}", device.getAddress());
                done();
            }
        }
    }

    private void handleRecordByte(byte b) {
        // add byte to record stream
        stream.write(b);
        // get next peek byte if stream size below the record byte size
        // otherwise add record and get next peek record if not done
        if (stream.size() < LinkDB.RECORD_BYTE_SIZE) {
            getPeekByte();
        } else {
            addRecord(LinkDBRecord.fromRecordData(stream.toByteArray(), offset));
            if (!isDone()) {
                offset -= LinkDB.RECORD_BYTE_SIZE;
                getPeekRecord();
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

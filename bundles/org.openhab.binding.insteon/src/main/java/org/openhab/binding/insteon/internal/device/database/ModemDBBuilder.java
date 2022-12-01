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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.PortListener;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.utils.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the modem database from incoming link record messages
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class ModemDBBuilder implements PortListener {
    private static final int MESSAGE_TIMEOUT = 6000; // in milliseconds
    private static final int QUERY_TIMEOUT = 3000; // in milliseconds

    private final Logger logger = LoggerFactory.getLogger(ModemDBBuilder.class);

    private volatile boolean done;
    private volatile long lastMsgTimestamp;
    private volatile int messageCount;
    private Driver driver;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private Map<InsteonAddress, Long> lastProductQueryTimes = new HashMap<>();

    public ModemDBBuilder(Driver driver, ScheduledExecutorService scheduler) {
        this.driver = driver;
        this.scheduler = scheduler;

        driver.addPortListener(this);
    }

    public boolean isDone() {
        return done;
    }

    public boolean isRunning() {
        return job != null;
    }

    public void start() {
        logger.debug("starting modem db builder");
        startDownload();
        done = false;
        job = scheduler.scheduleWithFixedDelay(() -> {
            if (isDone()) {
                stop();
            } else if (System.currentTimeMillis() - lastMsgTimestamp > MESSAGE_TIMEOUT) {
                String s = "";
                if (messageCount == 0) {
                    s = " No messages were received, the PLM or hub might be broken. If this continues see "
                            + "'Known Limitations and Issues' in the Insteon binding documentation.";
                }
                logger.warn("Failed to download modem database, restarting!{}", s);
                driver.reconnect();
                startDownload();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.debug("modem db builder finished");
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(false);
            this.job = null;
        }
    }

    private void startDownload() {
        logger.trace("starting modem database download");
        driver.getModemDB().clearEntries();
        lastMsgTimestamp = System.currentTimeMillis();
        messageCount = 0;
        getFirstLinkRecord();
    }

    private void getFirstLinkRecord() {
        getLinkRecord(true);
    }

    private void getNextLinkRecord() {
        getLinkRecord(false);
    }

    private void getLinkRecord(boolean getFirst) {
        try {
            Msg msg = Msg.makeMessage(getFirst ? "GetFirstALLLinkRecord" : "GetNextALLLinkRecord");
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void getProductID(InsteonAddress address) {
        try {
            Msg msg = Msg.makeStandardMessage(address, (byte) 0x10, (byte) 0x00);
            driver.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending product id query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private boolean shouldRequestProductID(InsteonAddress address) {
        // no request if product if already known
        if (driver.getModemDB().hasProductData(address)) {
            return false;
        }
        // no request if last product id query time within timeout window
        synchronized (lastProductQueryTimes) {
            long currentTime = System.currentTimeMillis();
            long lastQueryTime = lastProductQueryTimes.getOrDefault(address, 0L);
            if (currentTime - lastQueryTime <= QUERY_TIMEOUT) {
                return false;
            }
            lastProductQueryTimes.put(address, currentTime);
            return true;
        }
    }

    @Override
    public void disconnected() {
        // do nothing
    }

    @Override
    public void messageReceived(Msg msg) {
        lastMsgTimestamp = System.currentTimeMillis();
        messageCount++;
        try {
            if (msg.isPureNack()) {
                return;
            }
            if (msg.getByte("Cmd") == 0x50 && msg.isBroadcast()) {
                // we got a standard broadcast message
                handleProductData(msg);
            } else if (msg.getByte("Cmd") == 0x53) {
                // we got a link completed message
                handleLinkUpdate(msg);
            } else if (msg.getByte("Cmd") == 0x55) {
                // we got a user reset detected message
                handleUserReset();
            } else if (msg.getByte("Cmd") == 0x57) {
                // we got a link record response
                handleLinkRecord(msg);
            } else if ((msg.getByte("Cmd") == 0x69 || msg.getByte("Cmd") == 0x6A) && msg.isReplyNack()) {
                // we got a link record request reply nack (0x15)
                if (!isDone()) {
                    logger.debug("got all link records");
                    done();
                }
            } else if (msg.getByte("Cmd") == 0x6F) {
                // we got a manage link record response
                handleControlUpdate(msg);
            }
        } catch (FieldException e) {
            logger.warn("error parsing modem link record field ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }

    private void done() {
        driver.getModemDB().setIsComplete(true);
        driver.getModemDB().logEntries();
        driver.modemDBCompleted();
        done = true;
    }

    private void handleLinkRecord(Msg msg) throws FieldException {
        InsteonAddress linkAddr = msg.getAddress("LinkAddr");
        if (shouldRequestProductID(linkAddr)) {
            getProductID(linkAddr);
        }
        if (isDone()) {
            logger.debug("modem db builder already completed, ignoring record");
            return;
        }
        driver.getModemDB().addRecord(msg);
        getNextLinkRecord();
    }

    private void handleLinkUpdate(Msg msg) throws FieldException {
        InsteonAddress linkAddr = msg.getAddress("LinkAddr");
        if (shouldRequestProductID(linkAddr)) {
            getProductID(linkAddr);
        }
        int linkCode = msg.getInt("LinkCode");
        if (linkCode == 0x00 || linkCode == 0x01) {
            driver.getModemDB().addRecord(msg);
        } else if (linkCode == 0xFF) {
            driver.getModemDB().deleteRecord(msg);
        } else {
            logger.debug("got invalid link code: {}", ByteUtils.getHexString(linkCode));
            return;
        }
        driver.getModemDB().logEntry(linkAddr);
        driver.getListener().modemDBUpdated(linkAddr, msg.getInt("ALLLinkGroup"));
    }

    private void handleControlUpdate(Msg msg) throws FieldException {
        InsteonAddress linkAddr = msg.getAddress("LinkAddr");
        int controlCode = msg.getInt("ControlCode");
        if (controlCode == 0x40 || controlCode == 0x41) {
            driver.getModemDB().addRecord(msg);
        } else if (controlCode == 0x80) {
            driver.getModemDB().deleteRecord(msg);
        } else {
            logger.debug("got invalid control code: {}", ByteUtils.getHexString(controlCode));
            return;
        }
        driver.getModemDB().logEntry(linkAddr);
        driver.getListener().modemDBUpdated(linkAddr, msg.getInt("ALLLinkGroup"));
    }

    private void handleProductData(Msg msg) throws FieldException {
        InsteonAddress fromAddr = msg.getAddress("fromAddress");
        if (!driver.getModemDB().hasEntry(fromAddr) || driver.getModemDB().hasProductData(fromAddr)) {
            // skip if source address not in modem db or has already product data
            return;
        }

        if (msg.getByte("command1") == 0x01 || msg.getByte("command1") == 0x02) {
            driver.getModemDB().setProductData(msg);
            driver.getListener().productDataUpdated(fromAddr);
        } else if (shouldRequestProductID(fromAddr)) {
            // request product id, for non-product data broadcast message,
            // with a 1400 ms delay to allow all-link cleanup msg to be processed beforehand,
            // and before the delayed polling (1500 ms) is triggered on an already defined battery powered device
            scheduler.schedule(() -> getProductID(fromAddr), 1400, TimeUnit.MILLISECONDS);
        }
    }

    private void handleUserReset() {
        driver.getModemDB().clearEntries();
        driver.getModemDB().setIsComplete(true);
        driver.getListener().modemReset();
    }
}

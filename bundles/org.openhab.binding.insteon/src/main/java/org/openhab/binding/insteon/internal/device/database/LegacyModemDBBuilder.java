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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.transport.LegacyPort;
import org.openhab.binding.insteon.internal.transport.LegacyPortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the modem database from incoming link record messages
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class LegacyModemDBBuilder implements LegacyPortListener {
    private static final int MESSAGE_TIMEOUT = 30000;

    private final Logger logger = LoggerFactory.getLogger(LegacyModemDBBuilder.class);

    private volatile boolean isComplete = false;
    private LegacyPort port;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job = null;
    private volatile long lastMessageTimestamp;
    private volatile int messageCount = 0;

    public LegacyModemDBBuilder(LegacyPort port, ScheduledExecutorService scheduler) {
        this.port = port;
        this.scheduler = scheduler;
    }

    public void start() {
        port.addListener(this);

        logger.trace("starting modem db builder");
        startDownload();
        job = scheduler.scheduleWithFixedDelay(() -> {
            if (isComplete()) {
                stop();
            } else {
                if (System.currentTimeMillis() - lastMessageTimestamp > MESSAGE_TIMEOUT) {
                    String s = "";
                    if (messageCount == 0) {
                        s = """
                                 No messages were received, the PLM or hub might be broken. If this continues see \
                                'Known Limitations and Issues' in the Insteon binding documentation.\
                                """;
                    }
                    logger.warn("Modem database download was unsuccessful, restarting!{}", s);
                    startDownload();
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void startDownload() {
        logger.trace("starting modem database download");
        port.clearModemDB();
        lastMessageTimestamp = System.currentTimeMillis();
        messageCount = 0;
        getFirstLinkRecord();
    }

    public void stop() {
        logger.trace("modem db builder finished");
        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }
    }

    public boolean isComplete() {
        return isComplete;
    }

    public boolean isRunning() {
        return job != null;
    }

    private void getFirstLinkRecord() {
        try {
            port.writeMessage(Msg.makeMessage("GetFirstALLLinkRecord"));
        } catch (IOException e) {
            logger.warn("error sending link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    /**
     * processes link record messages from the modem to build database
     * and request more link records if not finished.
     * {@inheritDoc}
     */
    @Override
    public void msg(Msg msg) {
        lastMessageTimestamp = System.currentTimeMillis();
        messageCount++;

        if (msg.isPureNack()) {
            return;
        }
        try {
            if (msg.getByte("Cmd") == 0x69 || msg.getByte("Cmd") == 0x6a) {
                // If the flag is "ACK/NACK", a record response
                // will follow, so we do nothing here.
                // If its "NACK", there are none
                if (msg.getByte("ACK/NACK") == 0x15) {
                    logger.debug("got all link records.");
                    done();
                }
            } else if (msg.getByte("Cmd") == 0x57) {
                // we got the link record response
                updateModemDB(msg.getInsteonAddress("LinkAddr"), port, msg, false);
                port.writeMessage(Msg.makeMessage("GetNextALLLinkRecord"));
            }
        } catch (FieldException e) {
            logger.debug("bad field handling link records {}", e.getMessage());
        } catch (IOException e) {
            logger.debug("got IO exception handling link records {}", e.getMessage());
        } catch (IllegalStateException e) {
            logger.debug("got exception requesting link records {}", e.getMessage());
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private synchronized void done() {
        isComplete = true;
        logModemDB();
        port.removeListener(this);
        port.modemDBComplete();
    }

    private void logModemDB() {
        try {
            logger.debug("MDB ------- start of modem link records ------------------");
            Map<InsteonAddress, LegacyModemDBEntry> dbes = port.getDriver().lockModemDBEntries();
            for (Entry<InsteonAddress, LegacyModemDBEntry> db : dbes.entrySet()) {
                List<Msg> records = db.getValue().getLinkRecords();
                for (Msg msg : records) {
                    int recordFlags = msg.getByte("RecordFlags") & 0xff;
                    String ms = ((recordFlags & (0x1 << 6)) != 0) ? "CTRL" : "RESP";
                    logger.debug("MDB {}: {} group: {} data1: {} data2: {} data3: {}", db.getKey(), ms,
                            toHex(msg.getByte("ALLLinkGroup")), toHex(msg.getByte("LinkData1")),
                            toHex(msg.getByte("LinkData2")), toHex(msg.getByte("LinkData2")));
                }
                logger.debug("MDB -----");
            }
            logger.debug("MDB ---------------- end of modem link records -----------");
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } finally {
            port.getDriver().unlockModemDBEntries();
        }
    }

    public static String toHex(byte b) {
        return HexUtils.getHexString(b);
    }

    public void updateModemDB(InsteonAddress linkAddr, LegacyPort port, @Nullable Msg msg, boolean isModem) {
        try {
            Map<InsteonAddress, LegacyModemDBEntry> dbes = port.getDriver().lockModemDBEntries();
            LegacyModemDBEntry dbe = dbes.get(linkAddr);
            if (dbe == null) {
                dbe = new LegacyModemDBEntry(linkAddr, isModem);
                dbes.put(linkAddr, dbe);
            }
            dbe.setPort(port);
            if (msg != null) {
                dbe.addLinkRecord(msg);
                try {
                    byte group = msg.getByte("ALLLinkGroup");
                    int recordFlags = msg.getByte("RecordFlags") & 0xff;
                    if ((recordFlags & (0x1 << 6)) != 0) {
                        dbe.addControls(group);
                    } else {
                        dbe.addRespondsTo(group);
                    }
                } catch (FieldException e) {
                    logger.warn("cannot access field:", e);
                }
            }
        } finally {
            port.getDriver().unlockModemDBEntries();
        }
    }
}

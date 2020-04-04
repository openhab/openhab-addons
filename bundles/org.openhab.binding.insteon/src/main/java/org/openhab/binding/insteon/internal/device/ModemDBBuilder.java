/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.driver.ModemDBEntry;
import org.openhab.binding.insteon.internal.driver.Port;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds the modem database from incoming link record messages
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ModemDBBuilder implements MsgListener, Runnable {
    private final Logger logger = LoggerFactory.getLogger(ModemDBBuilder.class);
    private boolean isComplete = false;
    private Port port;
    private @Nullable Thread writeThread = null;
    private int timeoutMillis = 120000;

    public ModemDBBuilder(Port port) {
        this.port = port;
    }

    public void setRetryTimeout(int timeout) {
        this.timeoutMillis = timeout;
    }

    public void start() {
        port.addListener(this);
        writeThread = new Thread(this);
        writeThread.setName("Insteon DBBuilder");
        writeThread.setDaemon(true);
        writeThread.start();
        logger.debug("querying port for first link record");
    }

    public void startDownload() {
        logger.trace("starting modem database download");
        port.clearModemDB();
        getFirstLinkRecord();
    }

    public synchronized boolean isComplete() {
        return (isComplete);
    }

    @Override
    public void run() {
        logger.trace("starting modem db builder thread");
        while (!isComplete()) {
            startDownload();
            try {
                Thread.sleep(timeoutMillis); // wait for download to complete
            } catch (InterruptedException e) {
                logger.warn("modem db builder thread interrupted");
                break;
            }
            if (!isComplete()) {
                logger.warn("modem database download unsuccessful, restarting!");
            }
        }
        logger.trace("exiting modem db builder thread");
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
    public void msg(Msg msg, String fromPort) {
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
                updateModemDB(msg.getAddress("LinkAddr"), port, msg);
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
            Map<InsteonAddress, @Nullable ModemDBEntry> dbes = port.getDriver().lockModemDBEntries();
            for (Entry<InsteonAddress, @Nullable ModemDBEntry> db : dbes.entrySet()) {
                List<Msg> lrs = db.getValue().getLinkRecords();
                for (Msg m : lrs) {
                    int recordFlags = m.getByte("RecordFlags") & 0xff;
                    String ms = ((recordFlags & (0x1 << 6)) != 0) ? "CTRL" : "RESP";
                    logger.debug("MDB {}: {} group: {} data1: {} data2: {} data3: {}", db.getKey(), ms,
                            toHex(m.getByte("ALLLinkGroup")), toHex(m.getByte("LinkData1")),
                            toHex(m.getByte("LinkData2")), toHex(m.getByte("LinkData2")));
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
        return Utils.getHexString(b);
    }

    public void updateModemDB(InsteonAddress linkAddr, Port port, @Nullable Msg m) {
        try {
            Map<InsteonAddress, @Nullable ModemDBEntry> dbes = port.getDriver().lockModemDBEntries();
            ModemDBEntry dbe = dbes.get(linkAddr);
            if (dbe == null) {
                dbe = new ModemDBEntry(linkAddr);
                dbes.put(linkAddr, dbe);
            }
            dbe.setPort(port);
            if (m != null) {
                dbe.addLinkRecord(m);
                try {
                    byte group = m.getByte("ALLLinkGroup");
                    int recordFlags = m.getByte("RecordFlags") & 0xff;
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

/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.insteonplm.internal.driver;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.MessageFactory;
import org.openhab.binding.insteonplm.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Builds the modem database from incoming link record messages
 *
 * @author Bernd Pfrommer
 * @since 1.5.0
 */
public class AllLinkModemDB implements MessageListener, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(AllLinkModemDB.class);
    private boolean isComplete = false;
    private Thread writeThread = null;
    private int timeoutMillis = 120000;
    private final Port port;
    private final MessageFactory factory;
    private Map<InsteonAddress, ModemDBEntry> modemDb = Maps.newHashMap();

    public AllLinkModemDB(Port port, MessageFactory factory) {
        this.port = port;
        this.factory = factory;
    }

    public void setRetryTimeout(int timeout) {
        timeoutMillis = timeout;
    }

    public void start() {
        writeThread = new Thread(this);
        writeThread.setName("DBBuilder");
        writeThread.start();
        logger.debug("querying port for first link record");
    }

    public void startDownload() {
        logger.trace("starting modem database download");
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
            port.writeMessage(factory.makeMessage("GetFirstALLLinkRecord"));
        } catch (IOException e) {
            logger.error("error sending link record query ", e);
        }

    }

    /**
     * processes link record messages from the modem to build database
     * and request more link records if not finished.
     * {@inheritDoc}
     */
    @Override
    public void processMessage(Message msg) {
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
                updateModemDB(msg.getAddress("LinkAddr"), msg);
                port.writeMessage(factory.makeMessage("GetNextALLLinkRecord"));
            }
        } catch (FieldException e) {
            logger.debug("bad field handling link records {}", e);
        } catch (IOException e) {
            logger.debug("got IO exception handling link records {}", e);
        } catch (IllegalStateException e) {
            logger.debug("got exception requesting link records {}", e);
        }
    }

    private synchronized void done() {
        isComplete = true;
        logModemDB();
    }

    private void logModemDB() {
        try {
            logger.debug("MDB ------- start of modem link records ------------------");
            for (Entry<InsteonAddress, ModemDBEntry> db : modemDb.entrySet()) {
                List<Message> lrs = db.getValue().getLinkRecords();
                for (Message m : lrs) {
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
            logger.error("cannot access field:", e);
        }
    }

    private static String toHex(byte b) {
        return Utils.getHexString(b);
    }

    private void updateModemDB(InsteonAddress linkAddr, Message m) {
        synchronized (modemDb) {
            ModemDBEntry dbe = modemDb.get(linkAddr);
            if (dbe == null) {
                dbe = new ModemDBEntry(linkAddr);
                modemDb.put(linkAddr, dbe);
            }
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
                    logger.error("cannot access field:", e);
                }
            }
        }
    }
}

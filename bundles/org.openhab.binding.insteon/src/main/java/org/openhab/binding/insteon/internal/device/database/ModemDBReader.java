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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.device.ProductData;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.utils.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModemDBReader} manages modem database read requests
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBReader implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(ModemDBReader.class);

    private InsteonModem modem;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private Set<InsteonAddress> productQueries = new HashSet<>();
    private boolean done = true;
    private long lastMsgReceived;
    private int messageCount;

    public ModemDBReader(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;

        modem.getPort().registerListener(this);
    }

    public boolean isRunning() {
        return job != null;
    }

    public void read() {
        logger.debug("starting modem database reader");

        getAllRecords();

        job = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastMsgReceived > DatabaseManager.MESSAGE_TIMEOUT) {
                String s = "";
                if (messageCount == 0) {
                    s = """
                            No messages were received, the PLM or hub might be broken. If this continues see \
                            'Known Limitations and Issues' in the Insteon binding documentation.\
                            """;
                }
                logger.warn("Failed to read modem database, restarting!{}", s);
                restart();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.debug("modem database reader finished");

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        modem.getDBM().operationCompleted();
    }

    private void restart() {
        modem.getDB().clear();
        modem.reconnect();
        getAllRecords();
    }

    private void getAllRecords() {
        lastMsgReceived = System.currentTimeMillis();
        messageCount = 0;
        done = false;
        getFirstLinkRecord();
    }

    private void done() {
        modem.getDB().recordsLoaded();
        done = true;
        stop();
    }

    private void getFirstLinkRecord() {
        try {
            Msg msg = Msg.makeMessage("GetFirstALLLinkRecord");
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending first link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void getNextLinkRecord() {
        try {
            Msg msg = Msg.makeMessage("GetNextALLLinkRecord");
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending next link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void getProductId(InsteonAddress address) {
        try {
            Msg msg = Msg.makeStandardMessage(address, (byte) 0x10, (byte) 0x00);
            modem.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending product id query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    @Override
    public void disconnected() {
        if (!done) {
            logger.debug("port disconnected, restarting");
            restart();
        }
    }

    @Override
    public void messageReceived(Msg msg) {
        if (isRunning()) {
            lastMsgReceived = msg.getTimestamp();
            messageCount++;
        }

        try {
            if (msg.getCommand() == 0x50 && (msg.isAllLinkCleanup() || msg.isAllLinkSuccessReport())) {
                // we got an all link cleanup or success report message
                handleAllLinkMessage(msg);
            } else if (msg.getCommand() == 0x50 && msg.isBroadcast()
                    && (msg.getByte("command1") == 0x01 || msg.getByte("command1") == 0x02)) {
                // we got a product data broadcast message
                handleProductData(msg);
            } else if ((msg.getCommand() == 0x50 || msg.getCommand() == 0x5C) && msg.getByte("command1") == 0x10) {
                // we got a product data request ack
                handleProductDataAck(msg);
            } else if (msg.getCommand() == 0x53) {
                // we got a linking completed message
                handleLinkingCompleted(msg);
            } else if (msg.getCommand() == 0x55 || msg.getCommand() == 0x67 && msg.isReplyAck()) {
                // we got a user reset detected message or im reset reply ack
                handleIMReset();
            } else if (msg.getCommand() == 0x57) {
                // we got a link record response
                handleLinkRecord(msg);
            } else if ((msg.getCommand() == 0x69 || msg.getCommand() == 0x6A) && msg.isReplyNack()) {
                // we got a get link record reply nack
                if (!done) {
                    logger.debug("got all link records");
                    done();
                }
            } else if (msg.getCommand() == 0x6F && msg.isReplyAck()) {
                // we got a manage link record reply ack
                handleLinkRecordUpdated(msg);
            }
        } catch (FieldException e) {
            logger.warn("error parsing modem link record field ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }

    private void getProductData(InsteonAddress address) {
        // skip if not in modem db or product data already known
        if (!modem.getDB().hasEntry(address) || modem.getDB().hasProductData(address)) {
            return;
        }
        // get product id if not already queried
        synchronized (productQueries) {
            if (productQueries.add(address)) {
                getProductId(address);
            }
        }
    }

    private void handleLinkRecord(Msg msg) throws FieldException {
        if (done) {
            logger.debug("unsolicited link record, ignoring");
            return;
        }
        ModemDBRecord record = ModemDBRecord.fromRecordMsg(msg);
        InsteonAddress address = msg.getInsteonAddress("LinkAddr");
        modem.getDB().addRecord(record);
        getProductData(address);
        getNextLinkRecord();
    }

    private void handleLinkRecordUpdated(Msg msg) throws FieldException {
        ModemDBRecord record = ModemDBRecord.fromRecordMsg(msg);
        InsteonAddress address = msg.getInsteonAddress("LinkAddr");
        int group = msg.getInt("ALLLinkGroup");
        int code = msg.getInt("ControlCode");
        ManageRecordAction action = ManageRecordAction.valueOf(code);
        switch (action) {
            case MODIFY_OR_ADD:
                modem.getDB().modifyOrAddRecord(record);
                break;
            case MODIFY_CONTROLLER_OR_ADD:
                modem.getDB().modifyOrAddControllerRecord(record);
                break;
            case MODIFY_RESPONDER_OR_ADD:
                modem.getDB().modifyOrAddResponderRecord(record);
                break;
            case DELETE:
                modem.getDB().deleteRecord(address, group);
                break;
            default:
                logger.debug("got invalid control code: {}", HexUtils.getHexString(code));
                return;
        }
        modem.getDB().linkUpdated(address, group, false);
        getProductData(address);
    }

    private void handleLinkingCompleted(Msg msg) throws FieldException {
        ModemDBRecord record = ModemDBRecord.fromLinkingMsg(msg);
        InsteonAddress address = msg.getInsteonAddress("LinkAddr");
        int group = msg.getInt("ALLLinkGroup");
        int code = msg.getInt("LinkCode");
        LinkMode mode = LinkMode.valueOf(code);
        switch (mode) {
            case CONTROLLER:
                modem.getDB().modifyOrAddControllerRecord(record);
                break;
            case RESPONDER:
                modem.getDB().modifyOrAddResponderRecord(record);
                break;
            case DELETE:
                modem.getDB().deleteRecord(address, group);
                break;
            default:
                logger.debug("got invalid link code: {}", HexUtils.getHexString(code));
                return;
        }
        modem.getDB().linkUpdated(address, group, true);
        getProductData(address);
    }

    private void handleAllLinkMessage(Msg msg) throws FieldException {
        InsteonAddress address = msg.getInsteonAddress("fromAddress");
        getProductData(address);
    }

    private void handleProductData(Msg msg) throws FieldException {
        InsteonAddress fromAddr = msg.getInsteonAddress("fromAddress");
        InsteonAddress toAddr = msg.getInsteonAddress("toAddress");
        int deviceCategory = Byte.toUnsignedInt(toAddr.getHighByte());
        int subCategory = Byte.toUnsignedInt(toAddr.getMiddleByte());
        int firmware = Byte.toUnsignedInt(toAddr.getLowByte());
        int hardware = msg.getInt("command2");
        ProductData productData = ProductData.makeInsteonProduct(deviceCategory, subCategory);
        productData.setFirmwareVersion(firmware);
        productData.setHardwareVersion(hardware);
        // set product data if in modem db
        if (modem.getDB().hasEntry(fromAddr)) {
            modem.getDB().setProductData(fromAddr, productData);
        }
    }

    private void handleProductDataAck(Msg msg) throws FieldException {
        InsteonAddress address = msg.getInsteonAddress("fromAddress");
        // remove address from product queries
        synchronized (productQueries) {
            productQueries.remove(address);
        }
    }

    private void handleIMReset() {
        modem.resetInitiated();
    }
}

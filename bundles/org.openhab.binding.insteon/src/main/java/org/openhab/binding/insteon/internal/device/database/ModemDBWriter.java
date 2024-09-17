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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonModem;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ModemDBWriter} manages modem database weite requests
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class ModemDBWriter implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(ModemDBWriter.class);

    private InsteonModem modem;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private boolean done = true;
    private long lastMsgReceived;

    public ModemDBWriter(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;
    }

    public boolean isRunning() {
        return job != null;
    }

    public void write() {
        logger.debug("starting modem database writer");

        applyChanges();

        job = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - lastMsgReceived > DatabaseManager.MESSAGE_TIMEOUT) {
                logger.debug("modem database writer timed out, aborting");
                done();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void applyChanges() {
        lastMsgReceived = System.currentTimeMillis();
        done = false;

        modem.getPort().registerListener(this);

        manageNextModemLinkRecord();
    }

    public void stop() {
        logger.debug("modem database writer finished");

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        modem.getPort().unregisterListener(this);
        modem.getDBM().operationCompleted();
    }

    private void done() {
        done = true;
        stop();
    }

    private void manageNextModemLinkRecord() {
        ModemDBChange change = modem.getDB().pollNextChange();
        if (change == null) {
            logger.trace("all modem database changes written");
            done();
        } else {
            ModemDBRecord record = change.getRecord();
            ManageRecordAction action;
            if (change.isDelete()) {
                action = ManageRecordAction.DELETE;
            } else if (record.isController()) {
                action = ManageRecordAction.MODIFY_CONTROLLER_OR_ADD;
            } else {
                action = ManageRecordAction.MODIFY_RESPONDER_OR_ADD;
            }
            manageModemLinkRecord(action, record);
        }
    }

    private void manageModemLinkRecord(ManageRecordAction action, ModemDBRecord record) {
        try {
            Msg msg = Msg.makeMessage("ManageALLLinkRecord");
            msg.setByte("ControlCode", (byte) action.getControlCode());
            msg.setByte("RecordFlags", (byte) record.getFlags());
            msg.setByte("ALLLinkGroup", (byte) record.getGroup());
            msg.setAddress("LinkAddr", record.getAddress());
            msg.setByte("LinkData1", (byte) record.getData1());
            msg.setByte("LinkData2", (byte) record.getData2());
            msg.setByte("LinkData3", (byte) record.getData3());
            modem.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending manage modem link record query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
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
        lastMsgReceived = msg.getTimestamp();

        if (msg.getCommand() == 0x6F) {
            // we got a manage link record response
            manageNextModemLinkRecord();
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }
}

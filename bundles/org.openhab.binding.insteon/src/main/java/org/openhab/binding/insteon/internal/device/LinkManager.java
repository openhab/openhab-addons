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
package org.openhab.binding.insteon.internal.device;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.LinkMode;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.transport.PortListener;
import org.openhab.binding.insteon.internal.transport.message.FieldException;
import org.openhab.binding.insteon.internal.transport.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LinkManager} manages linking/unlinking a device to/from modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class LinkManager implements PortListener {
    private static final int LINKING_TIMEOUT = 30000; // in milliseconds
    private static final int DEFAULT_CONTROLLER_GROUP = 0;
    private static final int DEFAULT_RESPONDER_GROUP = 1;

    private final Logger logger = LoggerFactory.getLogger(LinkManager.class);

    private InsteonModem modem;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private @Nullable InsteonAddress address;
    private Queue<LinkingRequest> requests = new LinkedList<>();
    private boolean buttonPressed = false;
    private boolean complete = false;
    private boolean done = true;
    private int group = -1;

    public LinkManager(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;
    }

    public boolean isRunning() {
        return job != null;
    }

    private void setAddress(@Nullable InsteonAddress address) {
        this.address = address;
    }

    private void setGroup(int group) {
        this.group = group;
    }

    private @Nullable LinkingRequest getNextLinkingRequest() {
        synchronized (requests) {
            return requests.poll();
        }
    }

    private void addLinkingRequest(LinkMode mode, int group) {
        synchronized (requests) {
            LinkingRequest request = new LinkingRequest(mode, group);
            if (!requests.contains(request)) {
                requests.add(request);
            }
        }
    }

    private void removeLinkingRequests(LinkMode mode) {
        synchronized (requests) {
            requests.removeIf(request -> request.getLinkMode() == mode);
        }
    }

    public void link(@Nullable InsteonAddress address) {
        addLinkingRequest(LinkMode.RESPONDER, DEFAULT_RESPONDER_GROUP);
        addLinkingRequest(LinkMode.CONTROLLER, DEFAULT_CONTROLLER_GROUP);
        start(address);
    }

    public void unlink(InsteonAddress address, boolean force) {
        ModemDBEntry dbe = modem.getDB().getEntry(address);
        if (dbe == null) {
            logger.debug("device {} not in modem database", address);
            return;
        }

        if (force) {
            dbe.getRecords().forEach(record -> modem.getDB().markRecordForDelete(record));
            modem.getDB().update();
        } else {
            dbe.getRecords().forEach(record -> addLinkingRequest(LinkMode.DELETE, record.getGroup()));
            start(address);
        }
    }

    private void start(@Nullable InsteonAddress address) {
        long startTime = System.currentTimeMillis();

        logger.debug("starting device linker for {}", address);

        modem.getPort().registerListener(this);
        modem.getRequestManager().pause();

        setAddress(address);
        setGroup(-1);
        buttonPressed = false;
        complete = false;
        done = false;

        cancelModemLinking();
        if (address != null) {
            cancelLinkingMode(address);
        }
        handleNextLinkingRequest();

        job = scheduler.scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - startTime > LINKING_TIMEOUT) {
                logger.debug("device linker timeout for {}, aborting", address);
                done();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.debug("device linker finished for {}", address);

        modem.getRequestManager().resume();
        modem.getPort().unregisterListener(this);

        if (!complete) {
            cancelModemLinking();
            InsteonAddress address = this.address;
            if (address != null) {
                cancelLinkingMode(address);
            }
        }

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }
    }

    private void done() {
        done = true;
        stop();
    }

    private void startModemLinking(int linkCode, int group) {
        try {
            Msg msg = Msg.makeMessage("StartALLLinking");
            msg.setByte("LinkCode", (byte) linkCode);
            msg.setByte("ALLLinkGroup", (byte) group);
            modem.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending start modem linking query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void cancelModemLinking() {
        try {
            Msg msg = Msg.makeMessage("CancelALLLinking");
            modem.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending cancel modem linking query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void startLinkingMode(InsteonAddress address, int group) {
        try {
            Msg msg = Msg.makeExtendedMessage(address, (byte) 0x09, (byte) group, true);
            modem.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending linking mode query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void cancelLinkingMode(InsteonAddress address) {
        try {
            Msg msg = Msg.makeStandardMessage(address, (byte) 0x08, (byte) 0x00);
            modem.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending cancel linking query ", e);
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
        try {
            if (msg.isPureNack()) {
                return;
            }
            if (msg.getCommand() == 0x50 && msg.isBroadcast()
                    && (msg.getByte("command1") == 0x01 || msg.getByte("command1") == 0x02)) {
                // we got a set button pressed message
                handleButtonPressed(msg);
            } else if (msg.getCommand() == 0x53) {
                // we got a linking completed message
                handleNextLinkingRequest();
            } else if (msg.getCommand() == 0x5C
                    && (msg.getByte("command1") == 0x08 || msg.getByte("command1") == 0x09)) {
                // we got a linking mode failure report message
                handleLinkingModeFailure(msg);
            } else if (msg.getCommand() == 0x64) {
                // we got a start linking response
                handleLinkingStarted();
            }
        } catch (FieldException e) {
            logger.warn("error parsing link db info reply field ", e);
        }
    }

    @Override
    public void messageSent(Msg msg) {
        // ignore outbound message
    }

    private void handleButtonPressed(Msg msg) throws FieldException {
        InsteonAddress address = this.address;
        if (address == null) {
            setAddress(msg.getInsteonAddress("fromAddress"));
        } else if (!msg.isFromAddress(address)) {
            return;
        }
        if (!buttonPressed && msg.getByte("command1") == 0x02) {
            buttonPressed = true;
            // remove modem controller linking requests if controller only device
            // cmd1 => 0x01: controller + responder; 0x02: controller only (e.g. sensors)
            removeLinkingRequests(LinkMode.CONTROLLER);
        }
    }

    private void handleLinkingModeFailure(Msg msg) throws FieldException {
        if (msg.isFromAddress(address)) {
            logger.debug("device {} not responding, aborting", address);
            setAddress(null);
            done();
        }
    }

    private void handleLinkingStarted() {
        InsteonAddress address = this.address;
        if (address != null && group != -1) {
            startLinkingMode(address, group);
            setGroup(-1);
        }
    }

    private void handleNextLinkingRequest() {
        LinkingRequest request = getNextLinkingRequest();
        if (request == null) {
            complete = true;
            done();
        } else {
            startModemLinking(request.getLinkCode(), request.getGroup());
            setGroup(request.getGroup());
        }
    }

    /**
     * Linking request class
     */
    private static class LinkingRequest {
        private LinkMode mode;
        private int group;

        public LinkingRequest(LinkMode mode, int group) {
            this.mode = mode;
            this.group = group;
        }

        public LinkMode getLinkMode() {
            return mode;
        }

        public int getLinkCode() {
            return mode.getLinkCode();
        }

        public int getGroup() {
            return group;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            LinkingRequest other = (LinkingRequest) obj;
            return mode == other.mode && group == other.group;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + mode.hashCode();
            result = prime * result + group;
            return result;
        }
    }
}

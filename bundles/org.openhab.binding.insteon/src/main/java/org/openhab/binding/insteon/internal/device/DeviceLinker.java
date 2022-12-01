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
package org.openhab.binding.insteon.internal.device;

import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.database.ModemDBEntry;
import org.openhab.binding.insteon.internal.driver.Driver;
import org.openhab.binding.insteon.internal.driver.PortListener;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.InvalidMessageTypeException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Links/unlinks a device to/from modem
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DeviceLinker implements PortListener {
    private static final int LINKING_DELAY = 1000; // in milliseconds
    private static final int LINKING_TIMEOUT = 30000; // in milliseconds
    private static final int CONTROLLER_GROUP = 1;
    private static final int RESPONDER_GROUP = 0;

    private final Logger logger = LoggerFactory.getLogger(DeviceLinker.class);

    private enum LinkingRequest {
        LINK,
        UNLINK
    }

    private enum LinkingStep {
        CONTROLLER,
        RESPONDER,
        DONE
    }

    private volatile boolean buttonPressed;
    private volatile boolean complete;
    private volatile boolean done;
    private LinkingRequest request = LinkingRequest.LINK;
    private LinkingStep step = LinkingStep.CONTROLLER;
    private Driver driver;
    private ScheduledExecutorService scheduler;
    private @Nullable InsteonAddress address;
    private @Nullable ScheduledFuture<?> job;

    public DeviceLinker(Driver driver, ScheduledExecutorService scheduler) {
        this.driver = driver;
        this.scheduler = scheduler;
    }

    private boolean isDone() {
        return done;
    }

    public boolean isRunning() {
        return job != null;
    }

    private void setAddress(@Nullable InsteonAddress address) {
        this.address = address;
    }

    private void setRequest(LinkingRequest request) {
        this.request = request;
    }

    private void setStep(LinkingStep step) {
        this.step = step;
    }

    public void link(@Nullable InsteonAddress address) {
        start(address, LinkingRequest.LINK);
    }

    public void unlink(InsteonAddress address) {
        start(address, LinkingRequest.UNLINK);
    }

    private void start(@Nullable InsteonAddress address, LinkingRequest request) {
        long startTime = System.currentTimeMillis();
        logger.debug("starting device linker for {}", address);
        driver.addPortListener(this);
        buttonPressed = false;
        complete = false;
        done = false;
        setRequest(request);
        setStep(LinkingStep.CONTROLLER);
        cancelModemLinking();
        if (address != null && checkModemDB(address)) {
            setAddress(address);
            cancelLinkingMode(address);
            startLinkingMode(address, getControllerGroup(address));
        }
        job = scheduler.scheduleWithFixedDelay(() -> {
            if (done) {
                stop();
            } else if (System.currentTimeMillis() - startTime > LINKING_TIMEOUT) {
                logger.debug("device linker timeout for {}, aborting", address);
                done();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void stop() {
        logger.debug("device linker finished for {}", address);
        driver.removePortListener(this);

        InsteonAddress address = this.address;
        if (address != null && !complete) {
            cancelLinkingMode(address);
            cancelModemLinking();
        }

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(false);
            this.job = null;
        }
    }

    private void done() {
        done = true;
    }

    private boolean checkModemDB(InsteonAddress address) {
        boolean hasModemEntry = driver.getModemDB().hasEntry(address);
        if (hasModemEntry && request == LinkingRequest.LINK || !hasModemEntry && request == LinkingRequest.UNLINK) {
            logger.debug("device {} already {}, aborting", address,
                    request == LinkingRequest.LINK ? "linked" : "unlinked");
            done();
            return false;
        }
        return true;
    }

    private int getControllerGroup(InsteonAddress address) {
        ModemDBEntry dbe = driver.getModemDB().getEntry(address);
        if (dbe == null) {
            return CONTROLLER_GROUP;
        } else {
            return dbe.getControllerGroups().stream().sorted().findFirst().orElse(CONTROLLER_GROUP);
        }
    }

    private int getResponderGroup(InsteonAddress address) {
        ModemDBEntry dbe = driver.getModemDB().getEntry(address);
        if (dbe == null) {
            return RESPONDER_GROUP;
        } else {
            return dbe.getResponderGroups().stream().sorted().findFirst().orElse(RESPONDER_GROUP);
        }
    }

    private void startLinkingMode(InsteonAddress address, int group) {
        if (request == LinkingRequest.LINK) {
            enterLinkingMode(address, group);
        } else {
            enterUnlinkingMode(address, group);
        }
    }

    private void enterLinkingMode(InsteonAddress address, int group) {
        try {
            Msg msg = Msg.makeExtendedMessage(address, (byte) 0x09, (byte) (group & 0xFF), true);
            driver.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending linking mode query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void enterUnlinkingMode(InsteonAddress address, int group) {
        try {
            Msg msg = Msg.makeStandardMessage(address, (byte) 0x0A, (byte) (group & 0xFF));
            driver.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending unlinking mode query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void cancelLinkingMode(InsteonAddress address) {
        try {
            Msg msg = Msg.makeStandardMessage(address, (byte) 0x08, (byte) 0x00);
            driver.writeMessage(msg);
        } catch (FieldException e) {
            logger.warn("cannot access field:", e);
        } catch (IOException e) {
            logger.warn("error sending cancel linking query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
    }

    private void startModemLinking(int linkCode, int group) {
        try {
            Msg msg = Msg.makeMessage("StartALLLinking");
            msg.setByte("LinkCode", (byte) (linkCode & 0xFF));
            msg.setByte("ALLLinkGroup", (byte) (group & 0xFF));
            driver.writeMessage(msg);
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
            driver.writeMessage(msg);
        } catch (IOException e) {
            logger.warn("error sending cancel modem linking query ", e);
        } catch (InvalidMessageTypeException e) {
            logger.warn("invalid message ", e);
        }
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
            if (msg.isPureNack()) {
                return;
            }
            if (msg.getByte("Cmd") == 0x50) {
                byte cmd1 = msg.getByte("command1");
                if ((cmd1 == 0x01 || cmd1 == 0x02) && msg.isBroadcast()) {
                    // we got a set button pressed message
                    handleButtonPressed(msg);
                }
            } else if (msg.getByte("Cmd") == 0x53) {
                // we got a linking completed message
                handleLinkingCompleted(msg);
            } else if (msg.getByte("Cmd") == 0x64) {
                // we got a start linking response
                handleLinkingStarted(msg);
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
            address = msg.getAddress("fromAddress");
            setAddress(address);
        } else if (!msg.isFromAddress(address)) {
            return;
        }
        // ignore if check modem db fails
        if (!checkModemDB(address)) {
            return;
        }
        // ignore duplicate button pressed broadcast messages
        if (!buttonPressed) {
            buttonPressed = true;
        } else {
            return;
        }

        if (step == LinkingStep.CONTROLLER) {
            int linkCode = request == LinkingRequest.LINK ? 0x00 : 0xFF;
            int group = getControllerGroup(address);
            scheduler.schedule(() -> startModemLinking(linkCode, group), LINKING_DELAY, TimeUnit.MILLISECONDS);
            // cmd1 => 0x01: controller + responder; 0x02: controller only (e.g. sensors)
            if (msg.getByte("command1") == 0x02) {
                setStep(LinkingStep.DONE);
            }
        }
    }

    private void handleLinkingCompleted(Msg msg) {
        InsteonAddress address = this.address;
        if (address == null) {
            return;
        }

        if (step == LinkingStep.CONTROLLER) {
            int linkCode = request == LinkingRequest.LINK ? 0x01 : 0xFF;
            int group = getResponderGroup(address);
            scheduler.schedule(() -> startModemLinking(linkCode, group), LINKING_DELAY, TimeUnit.MILLISECONDS);
            setStep(LinkingStep.RESPONDER);
        } else if (step == LinkingStep.DONE) {
            logger.debug("device {} successfully {}", address, request == LinkingRequest.LINK ? "linked" : "unlinked");
            complete = true;
            done();
        }
    }

    private void handleLinkingStarted(Msg msg) {
        InsteonAddress address = this.address;
        if (address == null) {
            return;
        }

        if (step == LinkingStep.RESPONDER) {
            int group = getResponderGroup(address);
            scheduler.schedule(() -> startLinkingMode(address, group), LINKING_DELAY, TimeUnit.MILLISECONDS);
            setStep(LinkingStep.DONE);
        }
    }
}

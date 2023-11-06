/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.internal.handler;

import static org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants.CHANNEL_SHUTTER;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.OpenWebNetBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.openwebnet4j.OpenGateway;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAutomationHandler} is responsible for handling
 * commands/messages for an Automation OpenWebNet
 * device. It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
@NonNullByDefault
public class OpenWebNetAutomationHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAutomationHandler.class);

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("ss.SSS");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUTOMATION_SUPPORTED_THING_TYPES;

    private static long lastAllDevicesRefreshTS = 0; // ts when last all device refresh was sent for this handler

    // moving states
    public static final int MOVING_STATE_STOPPED = 0;
    public static final int MOVING_STATE_MOVING_UP = 1;
    public static final int MOVING_STATE_MOVING_DOWN = 2;
    public static final int MOVING_STATE_UNKNOWN = -1;

    // calibration states
    public static final int CALIBRATION_INACTIVE = -1;
    public static final int CALIBRATION_ACTIVATED = 0;
    public static final int CALIBRATION_GOING_UP = 1;
    public static final int CALIBRATION_GOING_DOWN = 2;

    // positions
    public static final int POSITION_MAX_STEPS = 100;
    public static final int POSITION_DOWN = 100;
    public static final int POSITION_UP = 0;
    public static final int POSITION_UNKNOWN = -1;

    public static final int SHUTTER_RUN_UNDEFINED = -1;
    private int shutterRun = SHUTTER_RUN_UNDEFINED;
    private static final String AUTO_CALIBRATION = "AUTO";

    private long startedMovingAtTS = -1; // timestamp when device started moving UP/DOWN
    private int movingState = MOVING_STATE_UNKNOWN;
    private int positionEstimation = POSITION_UNKNOWN;
    private @Nullable ScheduledFuture<?> moveSchedule;
    private int positionRequested = POSITION_UNKNOWN;
    private int calibrating = CALIBRATION_INACTIVE;
    private static final int MIN_STEP_TIME_MSEC = 50;
    private @Nullable Command commandRequestedWhileMoving = null;

    public OpenWebNetAutomationHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        Object shutterRunConfig = getConfig().get(OpenWebNetBindingConstants.CONFIG_PROPERTY_SHUTTER_RUN);
        try {
            if (shutterRunConfig == null) {
                shutterRunConfig = AUTO_CALIBRATION;
                logger.debug("shutterRun null --> default to AUTO");
            } else if (shutterRunConfig instanceof String stringValue) {
                if (AUTO_CALIBRATION.equalsIgnoreCase(stringValue)) {
                    logger.debug("shutterRun set to AUTO via configuration");
                    shutterRun = SHUTTER_RUN_UNDEFINED; // reset shutterRun
                } else { // try to parse int>=1000
                    int shutterRunInt = Integer.parseInt(stringValue);
                    if (shutterRunInt < 1000) {
                        throw new NumberFormatException();
                    }
                    shutterRun = shutterRunInt;
                    logger.debug("shutterRun set to {} via configuration", shutterRun);
                }
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.debug("Wrong configuration: {} setting must be {} or an integer >= 1000",
                    OpenWebNetBindingConstants.CONFIG_PROPERTY_SHUTTER_RUN, AUTO_CALIBRATION);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.wrong-configuration");
            shutterRun = SHUTTER_RUN_UNDEFINED;
        }
        updateState(CHANNEL_SHUTTER, UnDefType.UNDEF);
        positionEstimation = POSITION_UNKNOWN;
    }

    @Override
    protected Where buildBusWhere(String wStr) throws IllegalArgumentException {
        return new WhereLightAutom(wStr);
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        super.requestChannelState(channel);
        Where w = deviceWhere;
        if (w != null) {
            try {
                send(Automation.requestStatus(w.value()));
            } catch (OWNException e) {
                logger.debug("Exception while requesting state for channel {}: {} ", channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    protected long getRefreshAllLastTS() {
        return lastAllDevicesRefreshTS;
    };

    @Override
    protected void refreshDevice(boolean refreshAll) {
        if (refreshAll) {
            logger.debug("--- refreshDevice() : refreshing GENERAL... ({})", thing.getUID());
            try {
                send(Automation.requestStatus(WhereLightAutom.GENERAL.value()));
                lastAllDevicesRefreshTS = System.currentTimeMillis();
            } catch (OWNException e) {
                logger.warn("Excpetion while requesting all devices refresh: {}", e.getMessage());
            }
        } else {
            logger.debug("--- refreshDevice() : refreshing SINGLE... ({})", thing.getUID());
            requestChannelState(new ChannelUID(thing.getUID(), CHANNEL_SHUTTER));
        }
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_SHUTTER:
                handleShutterCommand(command);
                break;
            default: {
                logger.info("Unsupported channel UID {}", channel);
            }
        }
    }

    /**
     * Handles Automation Roller shutter command (UP/DOWN, STOP/MOVE, PERCENT xx%)
     */
    private void handleShutterCommand(Command command) {
        Where w = deviceWhere;
        if (w != null) {
            calibrating = CALIBRATION_INACTIVE; // cancel calibration if we receive a command
            commandRequestedWhileMoving = null;
            try {
                if (StopMoveType.STOP.equals(command)) {
                    send(Automation.requestStop(w.value()));
                } else if (command instanceof UpDownType || command instanceof PercentType) {
                    if (movingState == MOVING_STATE_MOVING_UP || movingState == MOVING_STATE_MOVING_DOWN) { // already
                                                                                                            // moving
                        logger.debug("# {} # already moving, STOP then defer command", deviceWhere);
                        commandRequestedWhileMoving = command;
                        sendHighPriority(Automation.requestStop(w.value()));
                        return;
                    } else {
                        if (command instanceof UpDownType) {
                            if (UpDownType.UP.equals(command)) {
                                send(Automation.requestMoveUp(w.value()));
                            } else {
                                send(Automation.requestMoveDown(w.value()));
                            }
                        } else if (command instanceof PercentType percentCommand) {
                            handlePercentCommand(percentCommand, w.value());
                        }
                    }
                } else {
                    logger.debug("Unsupported command {} for thing {}", command, thing.getUID());
                }
            } catch (OWNException e) {
                logger.debug("Exception while sending request for command {}: {}", command, e.getMessage(), e);
            }
        }
    }

    /**
     * Handles Automation PERCENT xx% command
     */
    private void handlePercentCommand(PercentType command, String w) {
        int percent = command.intValue();
        if (percent == positionEstimation) {
            logger.debug("# {} # handlePercentCommand() Command {}% == positionEstimation -> nothing to do", w,
                    percent);
            return;
        }
        try {
            if (percent == POSITION_DOWN) { // GO TO 100%
                send(Automation.requestMoveDown(w));
            } else if (percent == POSITION_UP) { // GO TO 0%
                send(Automation.requestMoveUp(w));
            } else { // GO TO XX%
                logger.debug("# {} # {}% requested", deviceWhere, percent);
                if (shutterRun == SHUTTER_RUN_UNDEFINED) {
                    logger.debug("& {} & CALIBRATION - shutterRun not configured, starting CALIBRATION...",
                            deviceWhere);
                    calibrating = CALIBRATION_ACTIVATED;
                    send(Automation.requestMoveUp(w));
                    positionRequested = percent;
                } else if (shutterRun >= 1000 && positionEstimation != POSITION_UNKNOWN) {
                    // these two must be known to calculate moveTime.
                    // Calculate how much time we have to move and set a deadline to stop after that
                    // time
                    int moveTime = Math
                            .round(((float) Math.abs(percent - positionEstimation) / POSITION_MAX_STEPS * shutterRun));
                    logger.debug("# {} # target moveTime={}", deviceWhere, moveTime);
                    if (moveTime > MIN_STEP_TIME_MSEC) {
                        ScheduledFuture<?> mSch = moveSchedule;
                        if (mSch != null && !mSch.isDone()) {
                            // a moveSchedule was already scheduled and is not done... let's cancel the
                            // schedule
                            mSch.cancel(false);
                            logger.debug("# {} # new XX% requested, old moveSchedule cancelled", deviceWhere);
                        }
                        // send a requestFirmwareVersion message to BUS gateways to wake up the CMD
                        // connection before
                        // sending further cmds
                        OpenWebNetBridgeHandler h = bridgeHandler;
                        if (h != null && h.isBusGateway()) {
                            OpenGateway gw = h.gateway;
                            if (gw != null) {
                                if (!gw.isCmdConnectionReady()) {
                                    logger.debug("# {} # waking-up CMD connection...", deviceWhere);
                                    send(GatewayMgmt.requestFirmwareVersion());
                                }
                            }
                        }
                        // REMINDER: start the schedule BEFORE sending the command, because the synch
                        // command waits for
                        // ACK and can take some 300ms
                        logger.debug("# {} # Starting schedule...", deviceWhere);
                        moveSchedule = scheduler.schedule(() -> {
                            logger.debug("# {} # moveSchedule expired, sending STOP...", deviceWhere);
                            try {
                                sendHighPriority(Automation.requestStop(w));
                            } catch (OWNException ex) {
                                logger.debug("Exception while sending request for command {}: {}", command,
                                        ex.getMessage(), ex);
                            }
                        }, moveTime, TimeUnit.MILLISECONDS);
                        logger.debug("# {} # ...schedule started, now sending highPriority command...", deviceWhere);
                        if (percent < positionEstimation) {
                            sendHighPriority(Automation.requestMoveUp(w));
                        } else {
                            sendHighPriority(Automation.requestMoveDown(w));
                        }
                        logger.debug("# {} # ...gateway.sendHighPriority() returned", deviceWhere);
                    } else {
                        logger.debug("# {} # moveTime <= MIN_STEP_TIME_MSEC ---> do nothing", deviceWhere);
                    }
                } else {
                    logger.info(
                            "Command {} cannot be executed: UNDEF position or shutterRun configuration parameter not/wrongly set (thing={})",
                            command, thing.getUID());
                }
            }
        } catch (OWNException e) {
            logger.debug("Exception while sending request for command {}: {}", command, e.getMessage(), e);
        }
    }

    @Override
    protected String ownIdPrefix() {
        return Who.AUTOMATION.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        logger.debug("handleMessage({}) for thing: {}", msg, thing.getUID());
        updateAutomationState((Automation) msg);
        // REMINDER: update automation state, and only after update thing status using
        // the super method, to avoid delays
        super.handleMessage(msg);
    }

    /**
     * Updates automation device state based on the Automation message received from
     * OWN network
     *
     * @param msg the Automation message
     */
    private void updateAutomationState(Automation msg) {
        logger.debug("updateAutomationState() - msg={} what={}", msg, msg.getWhat());
        try {
            if (msg.isCommandTranslation()) {
                logger.debug("msg is command translation, ignoring it");
                return;
            }
        } catch (FrameException fe) {
            logger.warn("Exception while checking WHERE command translation for frame {}: {}, ignoring it", msg,
                    fe.getMessage());
        }
        if (msg.isUp()) {
            updateMovingState(MOVING_STATE_MOVING_UP);
            if (calibrating == CALIBRATION_ACTIVATED) {
                calibrating = CALIBRATION_GOING_UP;
                logger.debug("& {} & CALIBRATION - started going ALL UP...", deviceWhere);
            }
        } else if (msg.isDown()) {
            updateMovingState(MOVING_STATE_MOVING_DOWN);
            if (calibrating == CALIBRATION_ACTIVATED) {
                calibrating = CALIBRATION_GOING_DOWN;
                logger.debug("& {} & CALIBRATION - started going ALL DOWN...", deviceWhere);
            }
        } else if (msg.isStop()) {
            long measuredRuntime = System.currentTimeMillis() - startedMovingAtTS;
            if (calibrating == CALIBRATION_GOING_DOWN && shutterRun == SHUTTER_RUN_UNDEFINED) {
                // since there are transmission delays we set shutterRun slightly less (-500ms
                // and -2%) than measuredRuntime
                shutterRun = (int) ((measuredRuntime - 500) * 0.98);
                logger.debug("& {} & CALIBRATION - reached DOWN : measuredRuntime={}", deviceWhere, measuredRuntime);
                updateMovingState(MOVING_STATE_STOPPED);
                logger.debug("& {} & CALIBRATION - COMPLETED, now going to {}%", deviceWhere, positionRequested);
                handleShutterCommand(new PercentType(positionRequested));
                Configuration configuration = editConfiguration();
                configuration.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_SHUTTER_RUN, Integer.toString(shutterRun));
                updateConfiguration(configuration);
                logger.debug("& {} & CALIBRATION - configuration updated: shutterRun = {}ms", deviceWhere, shutterRun);
            } else if (calibrating == CALIBRATION_GOING_UP) {
                updateMovingState(MOVING_STATE_STOPPED);
                logger.debug("& {} & CALIBRATION - reached UP, now sending DOWN command...", deviceWhere);
                calibrating = CALIBRATION_ACTIVATED;
                Where dw = deviceWhere;
                if (dw != null) {
                    String w = dw.value();
                    try {
                        send(Automation.requestMoveDown(w));
                    } catch (OWNException e) {
                        logger.debug("Exception while sending DOWN command during calibration: {}", e.getMessage(), e);
                        calibrating = CALIBRATION_INACTIVE;
                    }
                }
            } else {
                updateMovingState(MOVING_STATE_STOPPED);
                // do deferred command, if present
                Command cmd = commandRequestedWhileMoving;
                if (cmd != null) {
                    handleShutterCommand(cmd);
                }
            }
        } else {
            logger.debug("Frame {} not supported for thing {}, ignoring it.", msg, thing.getUID());
        }
    }

    /**
     * Updates movingState to newState
     */
    private void updateMovingState(int newState) {
        if (movingState == MOVING_STATE_STOPPED) {
            if (newState != MOVING_STATE_STOPPED) { // moving after stop
                startedMovingAtTS = System.currentTimeMillis();
                synchronized (DATE_FORMATTER) {
                    logger.debug("# {} # MOVING {} - startedMovingAt={} - {}", deviceWhere, newState, startedMovingAtTS,
                            DATE_FORMATTER.format(new Date(startedMovingAtTS)));
                }
            }
        } else { // we were moving
            updatePosition();
            if (newState != MOVING_STATE_STOPPED) { // moving after moving, take new timestamp
                startedMovingAtTS = System.currentTimeMillis();
                synchronized (DATE_FORMATTER) {
                    logger.debug("# {} # MOVING {} - startedMovingAt={} - {}", deviceWhere, newState, startedMovingAtTS,
                            DATE_FORMATTER.format(new Date(startedMovingAtTS)));
                }
            }
            // cancel the schedule
            ScheduledFuture<?> mSc = moveSchedule;
            if (mSc != null && !mSc.isDone()) {
                mSc.cancel(false);
            }
        }
        movingState = newState;
        logger.debug("# {} # movingState={} positionEstimation={} - calibrating={} shutterRun={}", deviceWhere,
                movingState, positionEstimation, calibrating, shutterRun);
    }

    /**
     * Updates positionEstimation and then channel state based on movedTime and
     * current movingState
     */
    private void updatePosition() {
        int newPos = POSITION_UNKNOWN;
        if (shutterRun > 0 && startedMovingAtTS != -1) {// we have shutterRun and startedMovingAtTS defined, let's
                                                        // calculate new positionEstimation
            long movedTime = System.currentTimeMillis() - startedMovingAtTS;
            logger.debug("# {} # current positionEstimation={} movedTime={}", deviceWhere, positionEstimation,
                    movedTime);
            int movedSteps = Math.round((float) movedTime / shutterRun * POSITION_MAX_STEPS);
            logger.debug("# {} # movedSteps: {} {}", deviceWhere, movedSteps,
                    (movingState == MOVING_STATE_MOVING_DOWN) ? "DOWN(+)" : "UP(-)");
            if (positionEstimation == POSITION_UNKNOWN && movedSteps >= POSITION_MAX_STEPS) { // we did a full run
                newPos = (movingState == MOVING_STATE_MOVING_DOWN) ? POSITION_DOWN : POSITION_UP;
            } else if (positionEstimation != POSITION_UNKNOWN) {
                newPos = positionEstimation
                        + ((movingState == MOVING_STATE_MOVING_DOWN) ? movedSteps : -1 * movedSteps);
                logger.debug("# {} # {} {} {} = {}", deviceWhere, positionEstimation,
                        (movingState == MOVING_STATE_MOVING_DOWN) ? "+" : "-", movedSteps, newPos);
                if (newPos > POSITION_DOWN) {
                    newPos = POSITION_DOWN;
                } else if (newPos < POSITION_UP) {
                    newPos = POSITION_UP;
                }
            }
        }
        if (newPos != POSITION_UNKNOWN) {
            if (newPos != positionEstimation) {
                updateState(CHANNEL_SHUTTER, new PercentType(newPos));
            }
        } else {
            updateState(CHANNEL_SHUTTER, UnDefType.UNDEF);
        }
        positionEstimation = newPos;
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> mSc = moveSchedule;
        if (mSc != null) {
            mSc.cancel(true);
        }
        super.dispose();
    }
}

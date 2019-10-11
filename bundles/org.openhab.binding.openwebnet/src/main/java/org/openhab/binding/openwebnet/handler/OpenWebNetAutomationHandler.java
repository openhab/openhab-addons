/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.openwebnet.handler;

import static org.openhab.binding.openwebnet.OpenWebNetBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openwebnet.message.Automation;
import org.openwebnet.message.BaseOpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetAutomationHandler} is responsible for handling commands/messages for an Automation OpenWebNet
 * device. It extends the abstract {@link OpenWebNetThingHandler}.
 *
 * @author Massimo Valla - Initial contribution
 */
public class OpenWebNetAutomationHandler extends OpenWebNetThingHandler {

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetAutomationHandler.class);
    private static final SimpleDateFormat formatter = new SimpleDateFormat("ss.SSS");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.AUTOMATION_SUPPORTED_THING_TYPES;

    protected Automation.Type automationType = Automation.Type.ZIGBEE;

    // internal states
    public static final int STATE_STOPPED = 0;
    public static final int STATE_MOVING_UP = 1;
    public static final int STATE_MOVING_DOWN = 2;
    public static final int STATE_UNKNOWN = -1;

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

    private long startedMovingAt = SHUTTER_RUN_UNDEFINED;
    private int internalState = STATE_UNKNOWN;
    private int positionEst = POSITION_UNKNOWN;
    private ScheduledFuture<?> moveSchedule;
    private int positionRequested = POSITION_UNKNOWN;
    private int calibrating = CALIBRATION_INACTIVE;
    private static final int STEP_TIME_MIN = 50; // ms
    private Command commandRequestedWhileMoving = null;

    /// TODO consider making all Automation calls Asynch insted of Synch (blocking), as all the behavior is based on
    /// received
    /// state notifications and not on command responses

    public OpenWebNetAutomationHandler(@NonNull Thing thing) {
        super(thing);
        logger.debug("==OWN:AutomationHandler== constructor");
    }

    @Override
    public void initialize() {
        super.initialize();
        logger.debug("==OWN:AutomationHandler== initialize() thing={}", thing.getUID());
        if (!bridgeHandler.isBusGateway()) {
            deviceWhere = deviceWhere + BaseOpenMessage.UNIT_01;
        }
        if (bridgeHandler != null && bridgeHandler.isBusGateway()) {
            automationType = Automation.Type.POINT_TO_POINT;
        }
        Object shutterRunConfig = getConfig().get(CONFIG_PROPERTY_SHUTTER_RUN);
        try {
            if (shutterRunConfig == null) {
                shutterRunConfig = AUTO_CALIBRATION;
                logger.debug("==OWN:AutomationHandler== shutterRun null, default to AUTO");
            } else if (shutterRunConfig instanceof java.lang.String) {
                if (AUTO_CALIBRATION.equals(((String) shutterRunConfig).toUpperCase())) {
                    logger.debug("==OWN:AutomationHandler== shutterRun set to AUTO in configuration");
                    shutterRun = SHUTTER_RUN_UNDEFINED;
                } else { // try to parse int>=1000
                    int shutterRunInt = Integer.parseInt((String) shutterRunConfig);
                    if (shutterRunInt < 1000) {
                        throw new NumberFormatException();
                    }
                    shutterRun = shutterRunInt;
                    logger.debug("==OWN:AutomationHandler== shutterRun set to {}", shutterRun);
                }

            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            logger.warn("==OWN:AutomationHandler== Wrong configuration: {} must be AUTO or an integer >= 1000",
                    CONFIG_PROPERTY_SHUTTER_RUN);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.wrong-configuration");
            shutterRun = SHUTTER_RUN_UNDEFINED;
        }
        updateState(CHANNEL_SHUTTER, UnDefType.UNDEF);
        positionEst = POSITION_UNKNOWN;
    }

    @Override
    protected void requestChannelState(ChannelUID channel) {
        logger.debug("==OWN:AutomationHandler== requestChannelState() thingUID={} channel={}", thing.getUID(),
                channel.getId());
        bridgeHandler.gateway.send(Automation.requestStatus(deviceWhere, automationType));
        // TODO request shutter position, if natively supported by device
    }

    @Override
    protected void handleChannelCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_SHUTTER:
                handleShutterCommand(command);
                break;
            default: {
                logger.warn("==OWN:AutomationHandler== Unsupported channel UID {}", channel);
            }
        }
        // TODO if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    /**
     * Handles Automation Rollershutter command (UP/DOWN, STOP/MOVE, PERCENT xx%)
     *
     * @param command the command to handle
     */
    private void handleShutterCommand(Command command) {
        calibrating = CALIBRATION_INACTIVE; // cancel calibration if we receive a command
        commandRequestedWhileMoving = null;
        if (StopMoveType.STOP.equals(command)) { // STOP
            bridgeHandler.gateway.send(Automation.requestStop(deviceWhere, automationType));
        } else if (command instanceof UpDownType || command instanceof PercentType) {
            if (internalState == STATE_MOVING_UP || internalState == STATE_MOVING_DOWN) { // already moving
                logger.debug("==OWN:AutomationHandler==  # {} # already moving, STOP then defer command", deviceWhere);
                commandRequestedWhileMoving = command;
                bridgeHandler.gateway.sendHighPriority(Automation.requestStop(deviceWhere, automationType));
                return;
            } else {
                if (command instanceof UpDownType) {
                    if (UpDownType.UP.equals(command)) { // UP
                        bridgeHandler.gateway.send(Automation.requestMoveUp(deviceWhere, automationType));
                    } else { // DOWN
                        bridgeHandler.gateway.send(Automation.requestMoveDown(deviceWhere, automationType));
                    }
                } else if (command instanceof PercentType) { // PERCENT
                    handlePercentCommand((PercentType) command);
                }
            }
        } else {
            logger.warn("==OWN:AutomationHandler== Command {} is not supported for thing {}", command, thing.getUID());
        }
    }

    /**
     * Handles Automation Percent command
     *
     * @param command the command to handle
     */
    private void handlePercentCommand(PercentType command) {
        int percent = command.intValue();
        if (percent == positionEst) {
            logger.debug(
                    "==OWN:AutomationHandler== # {} # handleShutterCommand() Command {}% == positionEst, nothing to do",
                    deviceWhere, percent);
            return;
        }
        if (percent == POSITION_DOWN) { // GO TO 100%
            bridgeHandler.gateway.send(Automation.requestMoveDown(deviceWhere, automationType));
        } else if (percent == POSITION_UP) { // GO TO 0%
            bridgeHandler.gateway.send(Automation.requestMoveUp(deviceWhere, automationType));
        } else { // GO TO XX%
            logger.debug("==OWN:AutomationHandler== # {} # {}% requested", deviceWhere, percent);
            if (shutterRun == SHUTTER_RUN_UNDEFINED) {
                logger.debug("==OWN:AutomationHandler== & {} & shutterRun not configured, starting CALIBRATION...",
                        deviceWhere);
                calibrating = CALIBRATION_ACTIVATED;
                bridgeHandler.gateway.send(Automation.requestMoveUp(deviceWhere, automationType));
                positionRequested = percent;
            } else if (shutterRun > 0 && positionEst != POSITION_UNKNOWN) { // these two must be known to
                                                                            // calculate
                                                                            // moveTime
                // calculate how much time we have to move and set a deadline to stop after that time
                int moveTime = Math.round(((float) Math.abs(percent - positionEst) / POSITION_MAX_STEPS * shutterRun));
                logger.debug("==OWN:AutomationHandler== # {} # target moveTime={}", deviceWhere, moveTime);
                if (moveTime > STEP_TIME_MIN) { // TODO calibrate this
                    if (moveSchedule != null && !moveSchedule.isDone()) {
                        // a moveSchedule was already scheduled and is not done... let's cancel the schedule
                        moveSchedule.cancel(false);
                        logger.warn( // should not get here....
                                "==OWN:AutomationHandler== # {} # new XX% requested, old moveSchedule cancelled",
                                deviceWhere);
                    }
                    // IMPORTANT IMPORTANT
                    // start the schedule BEFORE sending the command, because the synch command waits for ACK
                    // and can take some 300ms --- IS THIS STILL NEEDED ??
                    logger.debug("==OWN:AutomationHandler== # {} # Starting schedule...", deviceWhere);
                    moveSchedule = scheduler.schedule(() -> {
                        logger.debug("==OWN:AutomationHandler== # {} # moveSchedule expired, sending STOP...",
                                deviceWhere);
                        bridgeHandler.gateway.sendHighPriority(Automation.requestStop(deviceWhere, automationType));
                    }, moveTime, TimeUnit.MILLISECONDS);
                    logger.debug(
                            "==OWN:AutomationHandler== # {} # ...schedule started, now sending highPriority command...",
                            deviceWhere);
                    if (percent < positionEst) {
                        bridgeHandler.gateway.sendHighPriority(Automation.requestMoveUp(deviceWhere, automationType));
                    } else {
                        bridgeHandler.gateway.sendHighPriority(Automation.requestMoveDown(deviceWhere, automationType));
                    }
                    logger.debug("==OWN:AutomationHandler== # {} # ...gateway.sendHighPriority() returned",
                            deviceWhere);
                } else {
                    logger.debug("==OWN:AutomationHandler== # {} # moveTime < STEP_TIME_MIN, do nothing", deviceWhere);
                }
            } else {
                logger.warn(
                        "==OWN:AutomationHandler== Command {} cannot be executed: unknown position or shutterRun configuration param not set (thing={})",
                        command, thing.getUID());
            }
        }
    }

    @Override
    protected String ownIdPrefix() {
        return org.openwebnet.message.Who.AUTOMATION.value().toString();
    }

    @Override
    protected void handleMessage(BaseOpenMessage msg) {
        updateAutomationState((Automation) msg);
        // IMPORTANT update state, then update thing status in the super method, to avoid delays
        super.handleMessage(msg);
    }

    /**
     * Updates automation device state based on an Automation message received from OWN network
     *
     * @param msg the Automation message
     */
    private void updateAutomationState(Automation msg) {
        logger.debug("==OWN:AutomationHandler== updateAutomationState() - msg={} what={}", msg, msg.getWhat());
        if (msg.isCommandTranslation()) {
            logger.debug("==OWN:AutomationHandler== msg is command translation, ignoring...");
            return;
        }
        if (msg.isUp()) {
            updateStateInt(STATE_MOVING_UP);
            if (calibrating == CALIBRATION_ACTIVATED) {
                calibrating = CALIBRATION_GOING_UP;
                logger.debug("==OWN:AutomationHandler== & {} & ...CALIBRATING: started going ALL UP...", deviceWhere);
            }
        } else if (msg.isDown()) {
            updateStateInt(STATE_MOVING_DOWN);
            if (calibrating == CALIBRATION_ACTIVATED) {
                calibrating = CALIBRATION_GOING_DOWN;
                logger.debug("==OWN:AutomationHandler== & {} & ...CALIBRATING: started going ALL DOWN...", deviceWhere);
            }
        } else if (msg.isStop()) {
            long stoppedAt = System.currentTimeMillis();
            if (calibrating == CALIBRATION_GOING_DOWN && shutterRun == SHUTTER_RUN_UNDEFINED) {
                shutterRun = (int) (stoppedAt - startedMovingAt);
                logger.debug("==OWN:AutomationHandler== & {} & ...CALIBRATING: reached DOWN ===> shutterRun={}",
                        deviceWhere, shutterRun);
                updateStateInt(STATE_STOPPED);
                logger.debug(
                        "==OWN:AutomationHandler== & {} & ---CALIBRATION COMPLETED, now going to {}%",deviceWhere,
                        positionRequested);
                handleShutterCommand(new PercentType(positionRequested));
                Configuration configuration = editConfiguration();
                configuration.put(CONFIG_PROPERTY_SHUTTER_RUN, Integer.toString(shutterRun));
                updateConfiguration(configuration);
                logger.debug("==OWN:AutomationHandler== & {} & configuration updated: shutterRun = {}ms", deviceWhere,
                        shutterRun);
            } else if (calibrating == CALIBRATION_GOING_UP) {
                updateStateInt(STATE_STOPPED);
                logger.debug("==OWN:AutomationHandler==  & {} & ..CALIBRATING: reached UP, now sending DOWN command...",
                        deviceWhere);
                calibrating = CALIBRATION_ACTIVATED;
                bridgeHandler.gateway.send(Automation.requestMoveDown(deviceWhere, automationType));
            } else {
                updateStateInt(STATE_STOPPED);
                // do deferred command, if present
                if (commandRequestedWhileMoving != null) {
                    handleShutterCommand(commandRequestedWhileMoving);
                }
            }
        } else {
            logger.warn(
                    "==OWN:AutomationHandler== updateAutomationState() FRAME {} NOT SUPPORTED for thing {}, ignoring it.",
                    msg, thing.getUID());
        }
    }

    /** Updates internal state: state and positionEst */
    private void updateStateInt(int newState) {
        if (internalState == STATE_STOPPED) {
            if (newState != STATE_STOPPED) { // moving after stop
                startedMovingAt = System.currentTimeMillis();
                logger.debug("==OWN:AutomationHandler== # {} # MOVING {} - startedMovingAt={} - {}", deviceWhere,
                        newState, startedMovingAt, formatter.format(new Date(startedMovingAt)));
            }
        } else { // we were moving
            updatePosition();
            if (newState != STATE_STOPPED) { // moving after moving, take new timestamp
                startedMovingAt = System.currentTimeMillis();
                logger.debug("==OWN:AutomationHandler== # {} # MOVING {} - startedMovingAt={} - {}", deviceWhere,
                        newState, startedMovingAt, formatter.format(new Date(startedMovingAt)));
            }
            // cancel the schedule
            if (moveSchedule != null && !moveSchedule.isDone()) {
                moveSchedule.cancel(false);
            }
        }
        internalState = newState;
        logger.debug(
                "==OWN:AutomationHandler== # {} # [[[ internalState={} positionEst={} - calibrating={} shutterRun={} ]]]", deviceWhere,
                internalState, positionEst, calibrating, shutterRun);
    }

    /**
     * Updates positionEst based on movement time and current internalState
     */
    private void updatePosition() {
        int newPos = POSITION_UNKNOWN;
        if (shutterRun > 0) {// we have shutterRun defined, let's calculate new positionEst
            long movedTime = System.currentTimeMillis() - startedMovingAt;
            logger.debug("==OWN:AutomationHandler== # {} # current positionEst={}", deviceWhere, positionEst);
            logger.debug("==OWN:AutomationHandler== # {} # movedTime={}", deviceWhere, movedTime);
            int movedSteps = Math.round((float) movedTime / shutterRun * POSITION_MAX_STEPS);
            logger.debug("==OWN:AutomationHandler== # {} # movedSteps: {} {}", deviceWhere, movedSteps,
                    (internalState == STATE_MOVING_DOWN) ? "DOWN(+)" : "UP(-)");
            if (positionEst == POSITION_UNKNOWN && movedSteps >= POSITION_MAX_STEPS) { // we did a full run
                newPos = (internalState == STATE_MOVING_DOWN) ? POSITION_DOWN : POSITION_UP;
            } else if (positionEst != POSITION_UNKNOWN) {
                newPos = positionEst + ((internalState == STATE_MOVING_DOWN) ? movedSteps : -movedSteps);
                logger.debug("==OWN:AutomationHandler== # {} # {} {} {} = {}", deviceWhere, positionEst,
                        (internalState == STATE_MOVING_DOWN) ? "+" : "-", movedSteps, newPos);
                if (newPos > POSITION_DOWN) {
                    newPos = POSITION_DOWN;
                }
                if (newPos < POSITION_UP) {
                    newPos = POSITION_UP;
                }
            }
        }
        if (newPos != POSITION_UNKNOWN) {
            if (newPos != positionEst) {
                updateState(CHANNEL_SHUTTER, new PercentType(newPos));
            }
        } else {
            updateState(CHANNEL_SHUTTER, UnDefType.UNDEF);
        }
        positionEst = newPos;
    }

    @Override
    public void thingUpdated(Thing thing) {
        super.thingUpdated(thing);
        logger.debug("==OWN:AutomationHandler== thingUpdated()");
    }
} /* class */

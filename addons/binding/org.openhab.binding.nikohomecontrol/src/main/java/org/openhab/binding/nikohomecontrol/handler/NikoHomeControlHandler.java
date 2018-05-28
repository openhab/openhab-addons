/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlHandler.class);

    // dimmer constants
    static final int NHCON = 254;
    static final int NHCOFF = 255;

    // rollershutter constants
    static final int NHCDOWN = 254;
    static final int NHCUP = 255;
    static final int NHCSTOP = 253;

    @Nullable
    private volatile Runnable rollershutterTask;
    @Nullable
    private volatile ScheduledFuture<?> rollershutterStopTask;
    @Nullable
    private volatile ScheduledFuture<?> rollershutterMovingFlagTask;

    private volatile boolean filterEvent; // flag to filter first event from rollershutter on percent move to
                                          // avoid wrong position update
    private volatile boolean rollershutterMoving; // flag to indicate if rollershutter is currently moving
    private volatile boolean waitForEvent; // flag to wait for position update rollershutter before doing next
                                           // move

    private volatile int prevActionState;

    public NikoHomeControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer actionId = ((Number) this.getConfig().get(CONFIG_ACTION_ID)).intValue();

        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute action " + actionId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute action " + actionId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: bridge communication not initialized when trying to execute action "
                            + actionId);
            return;
        }

        NhcAction nhcAction = nhcComm.getActions().get(actionId);
        if (nhcAction == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: actionId does not match an action in the controller " + actionId);
            return;
        }

        if (nhcComm.communicationActive()) {
            handleCommandSelection(nhcAction, channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                nhcComm.restartCommunication();
                // If still not active, take thing offline and return.
                if (!nhcComm.communicationActive()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Niko Home Control: communication socket error");
                    return;
                }
                // Also put the bridge back online
                nhcBridgeHandler.bridgeOnline();

                // And finally handle the command
                handleCommandSelection(nhcAction, channelUID, command);
            });
        }
    }

    private void handleCommandSelection(NhcAction nhcAction, ChannelUID channelUID, Command command) {
        logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                handleSwitchCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_BRIGHTNESS:
                handleBrightnessCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_ROLLERSHUTTER:
                handleRollershutterCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Niko Home Control: channel unknown " + channelUID.getId());
        }
    }

    private void handleSwitchCommand(NhcAction nhcAction, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcAction.execute(0);
            } else {
                nhcAction.execute(100);
            }
        }
    }

    private void handleBrightnessCommand(NhcAction nhcAction, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcAction.execute(NHCOFF);
            } else {
                nhcAction.execute(NHCON);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = nhcAction.getState();
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                nhcAction.execute(newValue > 100 ? 100 : newValue);
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                nhcAction.execute(newValue < 0 ? 0 : newValue);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            if (p == PercentType.ZERO) {
                nhcAction.execute(NHCOFF);
            } else {
                nhcAction.execute(p.intValue());
            }
        }
    }

    private void handleRollershutterCommand(NhcAction nhcAction, Command command) {
        Configuration config = this.getConfig();
        if (logger.isTraceEnabled()) {
            String actionId = (String) config.get(CONFIG_ACTION_ID);
            logger.trace("handleRollerShutterCommand: rollershutter {} command {}", actionId, command);
            logger.trace("handleRollerShutterCommand: rollershutter {}, current position {}", actionId,
                    nhcAction.getState());
        }

        // first stop all current movement of rollershutter and wait until exact position is known
        if (this.rollershutterMoving) {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} moving, therefore stop",
                        config.get(CONFIG_ACTION_ID));
            }
            rollershutterPositionStop(nhcAction);
        }

        // task to be executed once exact position received from Niko Home Control
        this.rollershutterTask = () -> {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} task running",
                        this.getConfig().get(CONFIG_ACTION_ID));
            }

            int currentValue = nhcAction.getState();

            if (command instanceof UpDownType) {
                UpDownType s = (UpDownType) command;
                if (s == UpDownType.UP) {
                    nhcAction.execute(NHCUP);
                } else {
                    nhcAction.execute(NHCDOWN);
                }
            } else if (command instanceof StopMoveType) {
                nhcAction.execute(NHCSTOP);
            } else if (command instanceof PercentType) {
                int newValue = 100 - ((PercentType) command).intValue();
                if (logger.isTraceEnabled()) {
                    logger.trace("handleRollerShutterCommand: rollershutter {} percent command, current {}, new {}",
                            config.get(CONFIG_ACTION_ID), currentValue, newValue);
                }
                if (currentValue == newValue) {
                    return;
                }
                if ((newValue > 0) && (newValue < 100)) {
                    scheduleRollershutterStop(nhcAction, currentValue, newValue);
                }
                if (newValue < currentValue) {
                    nhcAction.execute(NHCDOWN);
                } else if (newValue > currentValue) {
                    nhcAction.execute(NHCUP);
                }
            }
        };

        // execute immediately if not waiting for exact position
        if (!this.waitForEvent) {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} task executing immediately",
                        this.getConfig().get(CONFIG_ACTION_ID));
            }
            executeRollershutterTask();
        }
    }

    /**
     * Method used to stop rollershutter when moving. This will then result in an exact position to be received, so next
     * percentage movements could be done accurately.
     *
     * @param nhcAction Niko Home Control action
     *
     */
    private void rollershutterPositionStop(NhcAction nhcAction) {
        if (logger.isTraceEnabled()) {
            logger.trace("rollershutterPositionStop: rollershutter {} executing",
                    this.getConfig().get(CONFIG_ACTION_ID));
        }
        cancelRollershutterStop();
        this.rollershutterTask = null;
        this.filterEvent = false;
        this.waitForEvent = true;
        nhcAction.execute(NHCSTOP);
    }

    private void executeRollershutterTask() {
        if (logger.isTraceEnabled()) {
            logger.trace("executeRollershutterTask: rollershutter {} task triggered",
                    this.getConfig().get(CONFIG_ACTION_ID));
        }
        this.waitForEvent = false;

        if (this.rollershutterTask != null) {
            this.rollershutterTask.run();
            this.rollershutterTask = null;
        }
    }

    /**
     * Method used to schedule a rollershutter stop when moving. This allows stopping the rollershutter at a percent
     * position.
     *
     * @param nhcAction Niko Home Control action
     * @param currentValue current percent position
     * @param newValue new percent position
     *
     */
    private void scheduleRollershutterStop(NhcAction nhcAction, int currentValue, int newValue) {
        // filter first event for a rollershutter coming from Niko Home Control if moving to an intermediate
        // position to avoid updating state to full open or full close
        this.filterEvent = true;

        long duration = rollershutterMoveTime(nhcAction, currentValue, newValue);
        setRollershutterMovingTrue(nhcAction, duration);

        if (logger.isTraceEnabled()) {
            logger.trace("scheduleRollershutterStop: schedule rollershutter {} stop in {}ms",
                    this.getConfig().get(CONFIG_ACTION_ID), duration);
        }
        this.rollershutterStopTask = scheduler.schedule(() -> {
            logger.trace("scheduleRollershutterStop: run rollershutter {} stop",
                    this.getConfig().get(CONFIG_ACTION_ID));
            nhcAction.execute(NHCSTOP);
        }, duration, TimeUnit.MILLISECONDS);
    }

    private void cancelRollershutterStop() {
        ScheduledFuture<?> stopTask = this.rollershutterStopTask;
        if (stopTask != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("cancelRollershutterStop: cancel rollershutter {} stop",
                        this.getConfig().get(CONFIG_ACTION_ID));
            }
            stopTask.cancel(true);
        }
        this.rollershutterStopTask = null;

        this.filterEvent = false;
    }

    private void setRollershutterMovingTrue(NhcAction nhcAction, long duration) {
        if (logger.isTraceEnabled()) {
            logger.trace("setRollershutterMovingTrue: rollershutter {} moving", this.getConfig().get(CONFIG_ACTION_ID));
        }
        this.rollershutterMoving = true;
        this.rollershutterMovingFlagTask = scheduler.schedule(() -> {
            if (logger.isTraceEnabled()) {
                logger.trace("setRollershutterMovingTrue: rollershutter {} stopped moving",
                        this.getConfig().get(CONFIG_ACTION_ID));
            }
            this.rollershutterMoving = false;
        }, duration, TimeUnit.MILLISECONDS);
    }

    private void setRollershutterMovingFalse() {
        if (logger.isTraceEnabled()) {
            logger.trace("setRollershutterMovingFalse: rollershutter {} not moving",
                    this.getConfig().get(CONFIG_ACTION_ID));
        }
        this.rollershutterMoving = false;
        if (this.rollershutterMovingFlagTask != null) {
            this.rollershutterMovingFlagTask.cancel(true);
            this.rollershutterMovingFlagTask = null;
        }
    }

    private long rollershutterMoveTime(NhcAction nhcAction, int currentValue, int newValue) {
        int totalTime = (newValue > currentValue) ? nhcAction.getCloseTime() : nhcAction.getOpenTime();
        long duration = Math.abs(newValue - currentValue) * totalTime * 10;
        if (logger.isTraceEnabled()) {
            logger.trace("rollershutterMoveTime: rollershutter {} move time {}", this.getConfig().get(CONFIG_ACTION_ID),
                    duration);
        }
        return duration;
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer actionId = ((Number) config.get(CONFIG_ACTION_ID)).intValue();

        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for action " + actionId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for action " + actionId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();
        if (nhcComm == null || !nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: no connection with Niko Home Control, could not initialize action " + actionId);
            return;
        }

        NhcAction nhcAction = nhcComm.getActions().get(actionId);
        if (nhcAction == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: actionId does not match an action in the controller " + actionId);
            return;
        }

        int actionState = nhcAction.getState();
        int actionType = nhcAction.getType();
        String actionLocation = nhcAction.getLocation();

        this.prevActionState = actionState;
        nhcAction.setThingHandler(this);

        Map<String, String> properties = new HashMap<>();
        properties.put("type", String.valueOf(actionType));
        if (this.getThing().getThingTypeUID() == THING_TYPE_BLIND) {
            cancelRollershutterStop();
            this.waitForEvent = false;
            setRollershutterMovingFalse();

            properties.put("timeToOpen", String.valueOf(nhcAction.getOpenTime()));
            properties.put("timeToClose", String.valueOf(nhcAction.getCloseTime()));
        }
        thing.setProperties(properties);

        if (thing.getLocation() == null) {
            thing.setLocation(actionLocation);
        }

        handleStateUpdate(nhcAction);

        logger.debug("Niko Home Control: action intialized {}", actionId);
    }

    /**
     * Method to update state of channel, called from Niko Home Control action.
     *
     * @param nhcAction Niko Home Control action
     *
     */
    public void handleStateUpdate(NhcAction nhcAction) {
        Configuration config = this.getConfig();
        Integer actionId = ((Number) config.get(CONFIG_ACTION_ID)).intValue();

        int actionType = nhcAction.getType();
        int actionState = nhcAction.getState();

        if (this.filterEvent) {
            this.filterEvent = false;
            logger.debug("Niko Home Control: filtered event {} for {}", actionState, actionId);
            updateStatus(ThingStatus.ONLINE);
            return;
        }

        switch (actionType) {
            case 0:
            case 1:
                updateState(CHANNEL_SWITCH, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                updateStatus(ThingStatus.ONLINE);
                break;
            case 2:
                updateState(CHANNEL_BRIGHTNESS, new PercentType(actionState));
                updateStatus(ThingStatus.ONLINE);
                break;
            case 4:
            case 5:
                cancelRollershutterStop();

                int state = 100 - actionState;
                int prevState = 100 - this.prevActionState;
                if (((state == 0) || (state == 100)) && (state != prevState)) {
                    long duration = rollershutterMoveTime(nhcAction, prevState, state);
                    setRollershutterMovingTrue(nhcAction, duration);
                } else {
                    setRollershutterMovingFalse();
                }

                if (this.waitForEvent) {
                    logger.debug("Niko Home Control: received requested rollershutter {} position event {}", actionId,
                            actionState);
                    executeRollershutterTask();
                } else {
                    updateState(CHANNEL_ROLLERSHUTTER, new PercentType(state));
                    updateStatus(ThingStatus.ONLINE);
                }
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Niko Home Control: unknown action type " + actionType);
        }

        this.prevActionState = actionState;
    }
}

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
package org.openhab.binding.dmx.internal.multiverse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dmx.internal.DmxBindingConstants.ListenerType;
import org.openhab.binding.dmx.internal.DmxThingHandler;
import org.openhab.binding.dmx.internal.Util;
import org.openhab.binding.dmx.internal.action.ActionState;
import org.openhab.binding.dmx.internal.action.BaseAction;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DmxChannel} extends {@link BaseDmxChannel} with actions and values
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Davy Vanherbergen - Initial contribution
 */
@NonNullByDefault
public class DmxChannel extends BaseDmxChannel {
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 255;

    private final Logger logger = LoggerFactory.getLogger(DmxChannel.class);

    private int value = MIN_VALUE;
    private int suspendedValue = MIN_VALUE;
    private int lastStateValue = -1;

    private boolean isSuspended = false;
    private int refreshTime = 0;
    private long lastStateTimestamp = 0;

    private final List<BaseAction> actions = new ArrayList<>();
    private final List<BaseAction> suspendedActions = new ArrayList<>();
    private final List<Thing> registeredThings = new ArrayList<>();

    private final Map<ChannelUID, DmxThingHandler> onOffListeners = new HashMap<>();
    private final Map<ChannelUID, DmxThingHandler> valueListeners = new HashMap<>();
    private @Nullable Entry<ChannelUID, DmxThingHandler> actionListener = null;

    public DmxChannel(int universeId, int dmxChannelId, int refreshTime) {
        super(universeId, dmxChannelId);
        this.refreshTime = refreshTime;
    }

    public DmxChannel(BaseDmxChannel dmxChannel, int refreshTime) {
        super(dmxChannel);
        this.refreshTime = refreshTime;
    }

    /**
     * register a thing with this channel
     *
     * @param thing a Thing object
     */
    public void registerThing(Thing thing) {
        if (!registeredThings.contains(thing)) {
            logger.debug("registering {} for DMX Channel {}", thing, this);
            registeredThings.add(thing);
        }
    }

    /**
     * unregister a thing from this object
     *
     * @param thing a Thing object
     */
    public void unregisterThing(Thing thing) {
        if (registeredThings.contains(thing)) {
            logger.debug("removing {} from DMX Channel {}", thing, this);
            registeredThings.remove(thing);
        }
    }

    /**
     * check if DMX Channel has any registered objects
     *
     * @return true or false
     */
    public boolean hasRegisteredThings() {
        return !registeredThings.isEmpty();
    }

    /**
     * set a DMX channel value
     *
     * @param value Integer value (0-255)
     */
    public void setValue(int value) {
        this.value = Util.toDmxValue(value) << 8;
        logger.trace("set dmx channel {} to value {}", this, this.value >> 8);
    }

    /**
     * set a DMX channel value
     *
     * @param value PercentType (0-100)
     */
    public void setValue(PercentType value) {
        this.value = Util.toDmxValue(value) << 8;
        logger.trace("set dmx channel {} to value {}", this, this.value >> 8);
    }

    /**
     * get the value of this DMX channel
     *
     * @return value as Integer (0-255)
     */
    public int getValue() {
        return Util.toDmxValue(value >> 8);
    }

    /**
     * get the high resolution value of this DMX channel
     *
     * @return value as Integer (0-65535)
     */
    public int getHiResValue() {
        return value;
    }

    /**
     * suspends current value and actions
     */
    public synchronized void suspendAction() {
        if (isSuspended) {
            logger.info("second suspend for actions in DMX channel {}, previous will be lost", this);
        } else {
            logger.trace("suspending actions and value for channel {}", this);
        }

        suspendedValue = value;
        suspendedActions.clear();
        if (hasRunningActions()) {
            suspendedActions.addAll(actions);
        }

        isSuspended = true;
    }

    /**
     * resumes previously suspended actions
     */
    public synchronized void resumeAction() throws IllegalStateException {
        if (isSuspended) {
            clearAction();
            if (!suspendedActions.isEmpty()) {
                actions.addAll(suspendedActions);
                suspendedActions.clear();
                logger.trace("resuming suspended actions for DMX channel {}", this);
            } else {
                value = suspendedValue;
                logger.trace("resuming suspended value for DMX channel {}", this);
            }
            isSuspended = false;
        } else {
            throw new IllegalStateException("trying to resume actions in non-suspended DMX channel " + this.toString());
        }
    }

    /**
     * check suspended state
     *
     * @return true or false
     */
    public boolean isSuspended() {
        return isSuspended;
    }

    /**
     * clear all running actions
     */
    public synchronized void clearAction() {
        logger.trace("clearing all actions for DMX channel {}", this);
        actions.clear();
        // remove action listener
        Map.Entry<ChannelUID, DmxThingHandler> actionListener = this.actionListener;
        if (actionListener != null) {
            actionListener.getValue().updateSwitchState(actionListener.getKey(), OnOffType.OFF);
            this.actionListener = null;
        }
    }

    /**
     * Replace the current list of channel actions with the provided one.
     *
     * @param channelAction action for this channel.
     */
    public synchronized void setChannelAction(BaseAction channelAction) {
        clearAction();
        actions.add(channelAction);
        logger.trace("set action {} for DMX channel {}", channelAction, this);
    }

    /**
     * Add a channel action to the current list of channel actions.
     *
     * @param channelAction action for this channel.
     */
    public synchronized void addChannelAction(BaseAction channelAction) {
        actions.add(channelAction);
        logger.trace("added action {} to channel {} (total {} actions)", channelAction, this, actions.size());
    }

    /**
     * @return true if there are running actions
     */
    public boolean hasRunningActions() {
        return !actions.isEmpty();
    }

    /**
     * Move to the next action in the action chain. This method is used by
     * automatic chains and to manually move to the next action if actions are
     * set as indefinite (e.g. endless hold). This allows the user to toggle
     * through a fixed set of values.
     */
    public synchronized void switchToNextAction() {
        // push action to the back of the action list
        BaseAction action = actions.get(0);
        actions.remove(0);
        action.reset();
        actions.add(action);
        logger.trace("switching to next action {} on channel {}", actions.get(0), this);
    }

    /**
     * Get the new value for this channel as determined by active actions or the
     * current value.
     *
     * @param calculationTime UNIX timestamp
     * @return value 0-255
     */
    public synchronized Integer getNewValue(long calculationTime) {
        return (getNewHiResValue(calculationTime) >> 8);
    }

    /**
     * Get the new value for this channel as determined by active actions or the
     * current value.
     *
     * @param calculationTime UNIX timestamp
     * @return value 0-65535
     */
    public synchronized Integer getNewHiResValue(long calculationTime) {
        if (hasRunningActions()) {
            logger.trace("checking actions, list is {}", actions);
            BaseAction action = actions.get(0);
            value = action.getNewValue(this, calculationTime);
            if (action.getState() == ActionState.COMPLETED && hasRunningActions()) {
                switchToNextAction();
            } else if (action.getState() == ActionState.COMPLETEDFINAL) {
                clearAction();
            }
        }

        // send updates not more than once in a second, and only on value change
        if ((lastStateValue != value) && (calculationTime - lastStateTimestamp > refreshTime)) {
            // notify value listeners if value changed
            for (Entry<ChannelUID, DmxThingHandler> listener : valueListeners.entrySet()) {
                int dmxValue = Util.toDmxValue(value >> 8);
                (listener.getValue()).updateChannelValue(listener.getKey(), dmxValue);
                logger.trace("sending VALUE={} (raw={}) status update to listener {} ({})", dmxValue, value,
                        listener.getValue(), listener.getKey());
            }

            // notify on/off listeners if on/off state changed
            if ((lastStateValue == 0) || (value == 0)) {
                OnOffType state = (value == 0) ? OnOffType.OFF : OnOffType.ON;
                for (Entry<ChannelUID, DmxThingHandler> listener : onOffListeners.entrySet()) {
                    (listener.getValue()).updateSwitchState(listener.getKey(), state);
                    logger.trace("sending ONOFF={} (raw={}), status update to listener {}", state, value,
                            listener.getKey());
                }
            }

            lastStateValue = value;
            lastStateTimestamp = calculationTime;
        }

        return value;
    }

    /**
     * add a channel listener for state updates
     *
     * @param thingChannel the channel the listener is linked to
     * @param listener the listener itself
     */
    public void addListener(ChannelUID thingChannel, DmxThingHandler listener, ListenerType type) {
        switch (type) {
            case VALUE:
                if (valueListeners.containsKey(thingChannel)) {
                    logger.trace("VALUE listener {} already exists in channel {}", thingChannel, this);
                } else {
                    valueListeners.put(thingChannel, listener);
                    logger.debug("adding VALUE listener {} to channel {}", thingChannel, this);
                }
                break;
            case ACTION:
                Map.Entry<ChannelUID, DmxThingHandler> actionListener = this.actionListener;
                if (actionListener != null) {
                    logger.info("replacing ACTION listener {} with {} in channel {}", actionListener.getValue(),
                            listener, this);
                } else {
                    logger.debug("adding ACTION listener {} in channel {}", listener, this);
                }
                this.actionListener = Map.entry(thingChannel, listener);
            default:
        }
    }

    /**
     * remove listener from channel
     *
     * @param thingChannel the channel that shall no longer receive updates
     */
    public void removeListener(ChannelUID thingChannel) {
        boolean foundListener = false;
        if (onOffListeners.containsKey(thingChannel)) {
            onOffListeners.remove(thingChannel);
            foundListener = true;
            logger.debug("removing ONOFF listener {} from DMX channel {}", thingChannel, this);
        }
        if (valueListeners.containsKey(thingChannel)) {
            valueListeners.remove(thingChannel);
            foundListener = true;
            logger.debug("removing VALUE listener {} from DMX channel {}", thingChannel, this);
        }
        Map.Entry<ChannelUID, DmxThingHandler> actionListener = this.actionListener;
        if (actionListener != null && actionListener.getKey().equals(thingChannel)) {
            this.actionListener = null;
            foundListener = true;
            logger.debug("removing ACTION listener {} from DMX channel {}", thingChannel, this);
        }
        if (!foundListener) {
            logger.trace("listener {} not found in DMX channel {}", thingChannel, this);
        }
    }
}

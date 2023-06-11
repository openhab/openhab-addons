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
package org.openhab.binding.insteon.internal.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DeviceFeatureListener essentially represents an openHAB item that
 * listens to a particular feature of an Insteon device
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Bernd Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class DeviceFeatureListener {
    private final Logger logger = LoggerFactory.getLogger(DeviceFeatureListener.class);

    public enum StateChangeType {
        ALWAYS,
        CHANGED
    };

    private String itemName;
    private ChannelUID channelUID;
    private Map<String, String> parameters = new HashMap<>();
    private Map<Class<?>, State> state = new HashMap<>();
    private List<InsteonAddress> relatedDevices = new ArrayList<>();
    private InsteonBinding binding;
    private static final int TIME_DELAY_POLL_RELATED_MSEC = 5000;

    /**
     * Constructor
     *
     * @param item name of the item that is listening
     * @param channelUID channel associated with this item
     * @param eventPublisher the publisher to use for publishing on the openhab bus
     */
    public DeviceFeatureListener(InsteonBinding binding, ChannelUID channelUID, String item) {
        this.binding = binding;
        this.itemName = item;
        this.channelUID = channelUID;
    }

    /**
     * Gets item name
     *
     * @return item name
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Test if string parameter is present and has a given value
     *
     * @param key key to match
     * @param value value to match
     * @return true if key exists and value matches
     */
    private boolean parameterHasValue(String key, String value) {
        String v = parameters.get(key);
        return (v != null && v.equals(value));
    }

    /**
     * Set parameters for this feature listener
     *
     * @param p the parameters to set
     */
    public void setParameters(Map<String, String> p) {
        parameters = p;
        updateRelatedDevices();
    }

    /**
     * Publishes a state change on the openhab bus
     *
     * @param newState the new state to publish on the openhab bus
     * @param changeType whether to always publish or not
     */
    public void stateChanged(State newState, StateChangeType changeType) {
        State oldState = state.get(newState.getClass());
        if (oldState == null) {
            logger.trace("new state: {}:{}", newState.getClass().getSimpleName(), newState);
            // state has changed, must publish
            publishState(newState);
        } else {
            logger.trace("old state: {}:{}=?{}", newState.getClass().getSimpleName(), oldState, newState);
            // only publish if state has changed or it is requested explicitly
            if (changeType == StateChangeType.ALWAYS || !oldState.equals(newState)) {
                publishState(newState);
            }
        }
        state.put(newState.getClass(), newState);
    }

    /**
     * Call this function to inform about a state change for a given
     * parameter key and value. If dataKey and dataValue don't match,
     * the state change will be ignored.
     *
     * @param state the new state to which the feature has changed
     * @param changeType how to process the state change (always, or only when changed)
     * @param dataKey the data key on which to filter
     * @param dataValue the value that the data key must match for the state to be published
     */
    public void stateChanged(State state, StateChangeType changeType, String dataKey, String dataValue) {
        if (parameterHasValue(dataKey, dataValue)) {
            stateChanged(state, changeType);
        }
    }

    /**
     * Publish the state. In the case of PercentType, if the value is
     * 0, send an OnOffType.OFF and if the value is 100, send an OnOffType.ON.
     * That way an openHAB Switch will work properly with an Insteon dimmer,
     * as long it is used like a switch (On/Off). An openHAB DimmerItem will
     * internally convert the ON back to 100% and OFF back to 0, so there is
     * no need to send both 0/OFF and 100/ON.
     *
     * @param state the new state of the feature
     */
    private void publishState(State state) {
        State publishState = state;
        if (state instanceof PercentType) {
            if (state.equals(PercentType.ZERO)) {
                publishState = OnOffType.OFF;
            } else if (state.equals(PercentType.HUNDRED)) {
                publishState = OnOffType.ON;
            }
        }
        pollRelatedDevices();
        binding.updateFeatureState(channelUID, publishState);
    }

    /**
     * Extracts related devices from the parameter list and
     * stores them for faster access later.
     */

    private void updateRelatedDevices() {
        String d = parameters.get("related");
        if (d == null) {
            return;
        }
        String[] devs = d.split("\\+");
        for (String dev : devs) {
            InsteonAddress a = InsteonAddress.parseAddress(dev);
            relatedDevices.add(a);
        }
    }

    /**
     * polls all devices that are related to this item
     * by the "related" keyword
     */
    public void pollRelatedDevices() {
        for (InsteonAddress a : relatedDevices) {
            logger.debug("polling related device {} in {} ms", a, TIME_DELAY_POLL_RELATED_MSEC);
            InsteonDevice d = binding.getDevice(a);
            if (d != null) {
                d.doPoll(TIME_DELAY_POLL_RELATED_MSEC);
            } else {
                logger.warn("device {} related to item {} is not configured!", a, itemName);
            }
        }
    }
}

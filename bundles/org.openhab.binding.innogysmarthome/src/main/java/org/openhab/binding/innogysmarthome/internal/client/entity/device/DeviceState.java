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
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import java.util.HashMap;

import org.openhab.binding.innogysmarthome.internal.client.entity.Property;

/**
 * Defines the {@link DeviceState}, e.g. if the device is reachable.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class DeviceState {

    protected static final String DEVICE_INCLUSION_STATE_INCLUDED = "Included";
    protected static final String DEVICE_INCLUSION_STATE_PENDING = "InclusionPending";
    protected static final String DEVICE_UPDATE_STATE_UPTODATE = "UpToDate";

    protected static final String PROTOCOL_ID_WMBUS = "wMBus";
    protected static final String PROTOCOL_ID_VIRTUAL = "Virtual";
    protected static final String PROTOCOL_ID_COSIP = "Cosip";

    private String id;

    private State state;

    private HashMap<String, Property> stateMap;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the state
     */
    public State getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state) {
        this.state = state;
    }

    /**
     * Returns true if the device is reachable, false otherwise.
     *
     * @return true or false for "reachable" {@link Device}s, else null.
     */
    public Boolean isReachable() {
        return getState().getIsReachable().getValue();
    }

    /**
     * Returns if the {@link State} "isReachable" is available for the current {@link Device}.
     *
     * @return
     */
    public Boolean hasIsReachableState() {
        return getState().getIsReachable() != null;
    }

    /**
     * Sets if the {@link Device} is reachable.
     *
     * @param isReachable
     */
    public void setReachable(boolean isReachable) {
        getState().getIsReachable().setValue(isReachable);
    }

    /**
     * Returns the configuration state of the device.
     *
     * @return the configuration state
     */
    public String getDeviceConfigurationState() {
        return getState().getDeviceConfigurationState().getValue();
    }

    /**
     * Returns the device inclusion state.
     *
     * @return the device inclusion state
     */
    public String getDeviceInclusionState() {
        return getState().getDeviceInclusionState().getValue();
    }

    /**
     * Returns true, if the device is included.
     *
     * @return true, if the {@link Device} is "Included"
     */
    public boolean deviceIsIncluded() {
        return DEVICE_INCLUSION_STATE_INCLUDED.equals(getState().getDeviceInclusionState().getValue());
    }

    /**
     * @return the stateMap
     */
    public HashMap<String, Property> getStateMap() {
        return stateMap;
    }

    /**
     * @param stateMap the stateMap to set
     */
    public void setStateMap(HashMap<String, Property> stateMap) {
        this.stateMap = stateMap;
    }

    /**
     * Returns true, if the device inclusion state is "InclusionPending".
     *
     * @return true, if the inclusion state is "InclusionPending"
     */
    public Boolean deviceInclusionIsPending() {
        return DEVICE_INCLUSION_STATE_PENDING.equals(getDeviceInclusionState());
    }

    /**
     * Return the update state of the {@link Device}.
     *
     * @return the update state
     */
    public String getDeviceUpdateState() {
        return getState().getUpdateState().getValue();
    }

    /**
     * Returns true if the {@link Device} is up to date.
     *
     * @return true, if the deviceUpdateState is "UpToDate"
     */
    public Boolean deviceIsUpToDate() {
        return DEVICE_UPDATE_STATE_UPTODATE.equals(getDeviceUpdateState());
    }

    /**
     * Returns the firmware version of the {@link Device}.
     *
     * @return the firmware version
     */
    public String getFirmwareVersion() {
        return getState().getFirmwareVersion().getValue();
    }
}

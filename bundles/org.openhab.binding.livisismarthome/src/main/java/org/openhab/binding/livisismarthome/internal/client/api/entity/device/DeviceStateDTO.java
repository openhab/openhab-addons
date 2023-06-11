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
package org.openhab.binding.livisismarthome.internal.client.api.entity.device;

import java.util.HashMap;

import org.openhab.binding.livisismarthome.internal.client.api.entity.PropertyDTO;

/**
 * Defines the {@link DeviceStateDTO}, e.g. if the device is reachable.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class DeviceStateDTO {

    private static final String DEVICE_UPDATE_STATE_UPTODATE = "UpToDate";

    private String id;
    private StateDTO state;
    private HashMap<String, PropertyDTO> stateMap;

    public DeviceStateDTO() {
        state = new StateDTO();
        stateMap = new HashMap<>();
    }

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
    public StateDTO getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(StateDTO state) {
        this.state = state;
    }

    /**
     * Returns true if the device is reachable, false otherwise.
     *
     * @return true or false for "reachable" {@link DeviceDTO}s, else null.
     */
    public Boolean isReachable() {
        return getState().getIsReachable().getValue();
    }

    /**
     * Returns if the {@link StateDTO} "isReachable" is available for the current {@link DeviceDTO}.
     *
     * @return true if the reachable state is available, otherwise false
     */
    public Boolean hasIsReachableState() {
        return getState().getIsReachable() != null;
    }

    /**
     * Sets if the {@link DeviceDTO} is reachable.
     *
     * @param isReachable reachable (boolean)
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
     * @return the stateMap
     */
    public HashMap<String, PropertyDTO> getStateMap() {
        return stateMap;
    }

    /**
     * @param stateMap the stateMap to set
     */
    public void setStateMap(HashMap<String, PropertyDTO> stateMap) {
        this.stateMap = stateMap;
    }

    /**
     * Return the update state of the {@link DeviceDTO}.
     *
     * @return the update state
     */
    public String getDeviceUpdateState() {
        return getState().getUpdateState().getValue();
    }

    /**
     * Returns true if the {@link DeviceDTO} is up to date.
     *
     * @return true, if the deviceUpdateState is "UpToDate"
     */
    public Boolean deviceIsUpToDate() {
        return DEVICE_UPDATE_STATE_UPTODATE.equals(getDeviceUpdateState());
    }

    /**
     * Returns the firmware version of the {@link DeviceDTO}.
     *
     * @return the firmware version
     */
    public String getFirmwareVersion() {
        return getState().getFirmwareVersion().getValue();
    }
}

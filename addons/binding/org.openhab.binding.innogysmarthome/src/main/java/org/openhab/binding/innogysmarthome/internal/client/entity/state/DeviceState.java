/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.state;

import org.openhab.binding.innogysmarthome.internal.client.Util;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;

/**
 * Defines the {@link DeviceState}, e.g. if the device is reachable.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class DeviceState extends EntityState {

    /**
     * Returns true if the device is reachable, false otherwise.
     *
     * @return true or false for "reachable" {@link Device}s, else null.
     */
    public Boolean isReachable() {
        return getPropertyValueAsBoolean(STATE_NAME_ISREACHABLE);
    }

    /**
     * Sets if the {@link Device} is reachable.
     *
     * @param isReachable
     */
    public void setReachable(boolean isReachable) {
        setPropertyValueAsBoolean(STATE_NAME_ISREACHABLE, isReachable);
    }

    /**
     * Returns the configuration state of the device.
     *
     * @return the configuration state
     */
    public String getDeviceConfigurationState() {
        return getPropertyValueAsString(STATE_NAME_DEVICECONFIGURATIONSTATE);
    }

    /**
     * Returns the device inclusion state.
     *
     * @return the device inclusion state
     */
    public String getDeviceInclusionState() {
        return getPropertyValueAsString(STATE_NAME_DEVICEINCLUSIONSTATE);
    }

    /**
     * Returns true, if the device is included.
     *
     * @return true, if the {@link Device} is "Included"
     */
    public boolean deviceIsIncluded() {
        return Util.equalsIfPresent(getDeviceInclusionState(), DEVICE_INCLUSION_STATE_INCLUDED);
    }

    /**
     * Returns true, if the device inclusion state is "InclusionPending".
     *
     * @return true, if the inclusion state is "InclusionPending"
     */
    public Boolean deviceInclusionIsPending() {
        return Util.equalsIfPresent(getDeviceInclusionState(), DEVICE_INCLUSION_STATE_PENDING);
    }

    /**
     * Return the update state of the {@link Device}.
     *
     * @return the update state
     */
    public String getDeviceUpdateState() {
        return getPropertyValueAsString(STATE_NAME_UPDATESTATE);
    }

    /**
     * Returns true if the {@link Device} is up to date.
     *
     * @return true, if the deviceUpdateState is "UpToDate"
     */
    public Boolean deviceIsUpToDate() {
        return Util.equalsIfPresent(getDeviceUpdateState(), DEVICE_UPDATE_STATE_UPTODATE);
    }

    /**
     * Returns the firmware version of the {@link Device}.
     *
     * @return the firmware version
     */
    public String getFirmwareVersion() {
        return getPropertyValueAsString(STATE_NAME_FIRMWAREVERSION);
    }
}

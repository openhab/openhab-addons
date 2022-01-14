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
package org.openhab.binding.homematic.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openhab.binding.homematic.internal.misc.HomematicConstants;

/**
 * Object that represents a Homematic channel.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmChannel {
    public static final String TYPE_GATEWAY_EXTRAS = "GATEWAY-EXTRAS";
    public static final String TYPE_GATEWAY_VARIABLE = "GATEWAY-VARIABLE";
    public static final String TYPE_GATEWAY_SCRIPT = "GATEWAY-SCRIPT";

    public static final Integer CHANNEL_NUMBER_EXTRAS = 0;
    public static final Integer CHANNEL_NUMBER_VARIABLE = 1;
    public static final Integer CHANNEL_NUMBER_SCRIPT = 2;

    private final Integer number;
    private final String type;
    private HmDevice device;
    private boolean initialized;
    private Integer lastFunction;
    private Map<HmDatapointInfo, HmDatapoint> datapoints = new HashMap<>();

    public HmChannel(String type, Integer number) {
        this.type = type;
        this.number = number;
    }

    /**
     * Returns the channel number.
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * Returns the device of the channel.
     */
    public HmDevice getDevice() {
        return device;
    }

    /**
     * Sets the device of the channel.
     */
    public void setDevice(HmDevice device) {
        this.device = device;
    }

    /**
     * Sets the type of the channel.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the flag, if the values for all datapoints has been loaded.
     *
     * @param initialized
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * Returns true, if the values for all datapoints has been loaded.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns true, if the channel contains gateway scripts.
     */
    public boolean isGatewayScript() {
        return device.isGatewayExtras() && TYPE_GATEWAY_SCRIPT.equals(type);
    }

    /**
     * Returns true, if the channel contains gateway variables.
     */
    public boolean isGatewayVariable() {
        return device.isGatewayExtras() && TYPE_GATEWAY_VARIABLE.equals(type);
    }

    /**
     * Returns all datapoints.
     */
    public List<HmDatapoint> getDatapoints() {
        synchronized (datapoints) {
            return new ArrayList<>(datapoints.values());
        }
    }

    /**
     * Adds a datapoint to the channel.
     */
    public void addDatapoint(HmDatapoint dp) {
        dp.setChannel(this);
        synchronized (datapoints) {
            datapoints.put(new HmDatapointInfo(dp), dp);
        }
    }

    /**
     * Removes all datapoints with VALUES param set type from the channel.
     */
    public void removeValueDatapoints() {
        synchronized (datapoints) {
            Iterator<Map.Entry<HmDatapointInfo, HmDatapoint>> iterator = datapoints.entrySet().iterator();
            while (iterator.hasNext()) {
                if (iterator.next().getKey().getParamsetType() == HmParamsetType.VALUES) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Returns the HmDatapoint with the given HmDatapointInfo.
     */
    public HmDatapoint getDatapoint(HmDatapointInfo dpInfo) {
        synchronized (datapoints) {
            return datapoints.get(dpInfo);
        }
    }

    /**
     * Returns the HmDatapoint with the given datapoint name.
     */
    public HmDatapoint getDatapoint(HmParamsetType type, String datapointName) {
        return getDatapoint(new HmDatapointInfo(type, this, datapointName));
    }

    /**
     * Returns true, if the channel has the given datapoint.
     */
    public boolean hasDatapoint(HmDatapointInfo dpInfo) {
        return getDatapoint(dpInfo) != null;
    }

    /**
     * Returns true, if the channel's datapoint set contains a
     * channel function datapoint.
     */
    public boolean isReconfigurable() {
        return getDatapoint(HmParamsetType.MASTER, HomematicConstants.DATAPOINT_NAME_CHANNEL_FUNCTION) != null;
    }

    /**
     * Returns the numeric value of the function this channel is currently configured to.
     * Returns null if the channel is not yet initialized or does not support dynamic reconfiguration.
     */
    public Integer getCurrentFunction() {
        HmDatapoint functionDp = getDatapoint(HmParamsetType.MASTER,
                HomematicConstants.DATAPOINT_NAME_CHANNEL_FUNCTION);
        return functionDp == null ? null : (Integer) functionDp.getValue();
    }

    /**
     * Checks whether the function this channel is configured to changed since this method was last invoked.
     * Returns false if the channel is not reconfigurable or was not initialized yet.
     */
    public synchronized boolean checkForChannelFunctionChange() {
        Integer currentFunction = getCurrentFunction();
        if (currentFunction == null) {
            return false;
        }
        if (lastFunction == null) {
            // We were called from initialization, which was preceded by initial metadata fetch, so everything
            // should be fine by now
            lastFunction = currentFunction;
            return false;
        }
        if (lastFunction.equals(currentFunction)) {
            return false;
        }
        lastFunction = currentFunction;
        return true;
    }

    /**
     * Returns true, if the channel has at least one PRESS_ datapoint.
     */
    public boolean hasPressDatapoint() {
        for (HmDatapoint dp : getDatapoints()) {
            if (dp.isPressDatapoint()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s[number=%d,initialized=%b]", getClass().getSimpleName(), number, initialized);
    }
}

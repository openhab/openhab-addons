/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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

    private Integer number;
    private String type;
    private HmDevice device;
    private boolean initialized;
    private Map<HmDatapointInfo, HmDatapoint> datapoints = new HashMap<HmDatapointInfo, HmDatapoint>();

    /**
     * Returns the channel number.
     */
    public Integer getNumber() {
        return number;
    }

    /**
     * Sets the channel number.
     */
    public void setNumber(Integer number) {
        this.number = number;
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
     * Returns the type of the channel.
     */
    public void setType(String type) {
        this.type = type;
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
    public Map<HmDatapointInfo, HmDatapoint> getDatapoints() {
        return datapoints;
    }

    /**
     * Adds a datapoint to the channel.
     */
    public void addDatapoint(HmDatapoint dp) {
        dp.setChannel(this);
        datapoints.put(new HmDatapointInfo(dp), dp);
    }

    /**
     * Returns the HmDatapoint with the given HmDatapointInfo.
     */
    public HmDatapoint getDatapoint(HmDatapointInfo dpInfo) {
        return datapoints.get(dpInfo);
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
        return datapoints.get(dpInfo) != null;
    }

    /**
     * Returns true, if the channel has at least one PRESS_ datapoint.
     */
    public boolean hasPressDatapoint() {
        for (HmDatapoint dp : datapoints.values()) {
            if (dp.isPressDatapoint()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("number", number).append("type", type)
                .append("initialized", initialized).toString();
    }

}

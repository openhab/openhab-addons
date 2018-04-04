/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Simple representation of a datapoint.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmDatapointInfo {
    private String address;
    private Integer channel;
    private String name;
    private HmParamsetType paramsetType;

    public HmDatapointInfo(HmDatapoint dp) {
        this(dp.getParamsetType(), dp.getChannel(), dp.getName());
    }

    public HmDatapointInfo(HmParamsetType paramsetType, HmChannel channel, String name) {
        this(channel.getDevice().getAddress(), paramsetType, channel.getNumber(), name);
    }

    public HmDatapointInfo(String address, HmParamsetType paramsetType, Integer channel, String name) {
        this.address = address;
        this.channel = channel;
        this.paramsetType = paramsetType;
        this.name = name;
    }

    /**
     * Creates a values HmDatapointInfo.
     */
    public static HmDatapointInfo createValuesInfo(HmChannel channel, String name) {
        return new HmDatapointInfo(HmParamsetType.VALUES, channel, name);
    }

    /**
     * Returns the address of the device.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the channel number.
     */
    public Integer getChannel() {
        return channel;
    }

    /**
     * Returns the name of the datapoint.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the datapoint.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the paramset type.
     */
    public HmParamsetType getParamsetType() {
        return paramsetType;
    }

    /**
     * Return true, if this is a pong datapoint info.
     */
    public boolean isPong() {
        return "CENTRAL".equals(address) && "PONG".equals(name);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(address).append(paramsetType).append(channel).append(name).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HmDatapointInfo)) {
            return false;
        }
        HmDatapointInfo comp = (HmDatapointInfo) obj;
        return new EqualsBuilder().append(address, comp.getAddress()).append(paramsetType, comp.getParamsetType())
                .append(channel, comp.getChannel()).append(name, comp.getName()).isEquals();
    }

    @Override
    public String toString() {
        if (paramsetType == HmParamsetType.VALUES) {
            return String.format("%s:%s#%s", address, channel, name);
        }
        return String.format("%s:%s_%s#%s", address, paramsetType.getId(), channel, name);
    }
}

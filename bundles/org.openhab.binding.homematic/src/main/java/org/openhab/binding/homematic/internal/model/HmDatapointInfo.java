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
package org.openhab.binding.homematic.internal.model;

import java.util.Objects;

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
        return Objects.hash(address, paramsetType, channel, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HmDatapointInfo)) {
            return false;
        }
        HmDatapointInfo comp = (HmDatapointInfo) obj;
        return Objects.equals(address, comp.getAddress()) && Objects.equals(paramsetType, comp.getParamsetType())
                && Objects.equals(channel, comp.getChannel()) && Objects.equals(name, comp.getName());
    }

    @Override
    public String toString() {
        if (paramsetType == HmParamsetType.VALUES) {
            return String.format("%s:%s#%s", address, channel, name);
        }
        return String.format("%s:%s_%s#%s", address, paramsetType.getId(), channel, name);
    }
}

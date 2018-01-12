/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.model;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.openhab.binding.homematic.internal.misc.MiscUtils;

/**
 * Object that represents a Homematic device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmDevice {
    public static final String TYPE_GATEWAY_EXTRAS = "GATEWAY-EXTRAS";
    public static final String ADDRESS_GATEWAY_EXTRAS = "GWE00000000";

    private HmInterface hmInterface;
    private String address;
    private String type;
    private String name;
    private String firmware;
    private String gatewayId;
    private String homegearId;

    private List<HmChannel> channels = new ArrayList<HmChannel>();

    /**
     * Returns the address of the device.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address of the device.
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the interface of the device.
     */
    public HmInterface getHmInterface() {
        return hmInterface;
    }

    /**
     * Sets the interface of the device.
     */
    public void setHmInterface(HmInterface hmInterface) {
        this.hmInterface = hmInterface;
    }

    /**
     * Returns the name of the device.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the device.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the device.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the device.
     */
    public void setType(String type) {
        this.type = MiscUtils.validateCharacters(type, "Device type", "-");
    }

    /**
     * Returns all channels of the device.
     */
    public List<HmChannel> getChannels() {
        return channels;
    }

    /**
     * Returns the firmware of the device.
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Sets the firmware of the device.
     */
    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    /**
     * Returns the gatewayId of the device.
     */
    public String getGatewayId() {
        return gatewayId;
    }

    /**
     * Sets the gatewayId of the device.
     */
    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    /**
     * Returns the homegearId of the device.
     */
    public String getHomegearId() {
        return homegearId;
    }

    /**
     * Sets the homegearId of the device.
     */
    public void setHomegearId(String homegearId) {
        this.homegearId = homegearId;
    }

    /**
     * Adds a channel to this device.
     */
    public void addChannel(HmChannel channel) {
        channel.setDevice(this);
        channels.add(channel);
    }

    /**
     * Returns the channel with the given channelNumber.
     */
    public HmChannel getChannel(int channelNumber) {
        for (HmChannel hmChannel : channels) {
            if (hmChannel.getNumber() == channelNumber) {
                return hmChannel;
            }
        }
        return null;
    }

    /**
     * Returns the number of datapoints.
     */
    public int getDatapointCount() {
        int dpCounter = 0;
        for (HmChannel channel : channels) {
            dpCounter += channel.getDatapoints().size();
        }
        return dpCounter;
    }

    /**
     * Returns true, if the device is the Homematic gateway.
     */
    public boolean isGatewayExtras() {
        return ADDRESS_GATEWAY_EXTRAS.equals(address);
    }

    /**
     * Returns true, if the device can not be reached (offline).
     */
    public boolean isUnreach() {
        return isStatusDatapointEnabled(DATAPOINT_NAME_UNREACH);
    }

    /**
     * Returns true, if the gateway has a config to transfer to the device.
     */
    public boolean isConfigPending() {
        return isStatusDatapointEnabled(DATAPOINT_NAME_CONFIG_PENDING);
    }

    /**
     * Returns true, if the gateway has a update to transfer to the device.
     */
    public boolean isUpdatePending() {
        return isStatusDatapointEnabled(DATAPOINT_NAME_UPDATE_PENDING);
    }

    /**
     * Returns true, if the device is in firmware update mode.
     */
    public boolean isFirmwareUpdating() {
        return isStatusDatapointEnabled(DATAPOINT_NAME_DEVICE_IN_BOOTLOADER);
    }

    /**
     * Returns true, if the device is offline.
     */
    public boolean isOffline() {
        return isFirmwareUpdating() || isUnreach();
    }

    private boolean isStatusDatapointEnabled(String datapointName) {
        HmChannel channel = getChannel(0);
        if (channel != null && channel.isInitialized()) {
            HmDatapointInfo dpInfo = HmDatapointInfo.createValuesInfo(channel, datapointName);
            HmDatapoint dp = channel.getDatapoint(dpInfo);
            if (dp != null) {
                return MiscUtils.isTrueValue(dp.getValue());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(address).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HmDevice)) {
            return false;
        }
        HmDevice comp = (HmDevice) obj;
        return new EqualsBuilder().append(address, comp.getAddress()).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("hmInterface", hmInterface)
                .append("address", address).append("type", type).append("name", name).append("firmware", firmware)
                .append("gatewayId", gatewayId).toString();
    }
}

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

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object that represents a Homematic device.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HmDevice {
    private final Logger logger = LoggerFactory.getLogger(HmDevice.class);

    public static final String TYPE_GATEWAY_EXTRAS = "GATEWAY-EXTRAS";
    public static final String ADDRESS_GATEWAY_EXTRAS = "GWE00000000";

    private final HmInterface hmInterface;
    private final String address;
    private final String type;
    private String name;
    private final String firmware;
    private final String gatewayId;
    private final String homegearId;

    private List<HmChannel> channels = new ArrayList<>();

    public HmDevice(String address, HmInterface hmInterface, String type, String gatewayId, String homegearId,
            String firmware) {
        this.address = address;
        this.hmInterface = hmInterface;
        this.firmware = firmware;
        if ("HM-ES-TX-WM".equals(type) && Float.valueOf(firmware) > 2.0) {
            logger.debug("Found HM-ES-TX-WM with firmware version > 2.0, creating virtual type");
            this.type = type + "2";
        } else {
            this.type = type;
        }
        this.gatewayId = gatewayId;
        this.homegearId = homegearId;
    }

    /**
     * Returns the address of the device.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns the interface of the device.
     */
    public HmInterface getHmInterface() {
        return hmInterface;
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
     * Returns the gatewayId of the device.
     */
    public String getGatewayId() {
        return gatewayId;
    }

    /**
     * Returns the homegearId of the device.
     */
    public String getHomegearId() {
        return homegearId;
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
        return Objects.hash(address);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof HmDevice)) {
            return false;
        }
        HmDevice comp = (HmDevice) obj;
        return Objects.equals(address, comp.getAddress());
    }

    @Override
    public String toString() {
        return String.format("%s[hmInterface=%s,address=%s,type=%s,name=%s,firmware=%s,gatewayId=%s]",
                getClass().getSimpleName(), hmInterface, address, type, name, firmware, gatewayId);
    }
}

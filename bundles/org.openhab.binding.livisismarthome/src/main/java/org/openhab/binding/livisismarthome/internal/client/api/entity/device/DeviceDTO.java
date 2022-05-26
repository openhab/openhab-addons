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
package org.openhab.binding.livisismarthome.internal.client.api.entity.device;

import static org.openhab.binding.livisismarthome.internal.LivisiBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.livisismarthome.internal.client.api.entity.capability.CapabilityDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.location.LocationDTO;
import org.openhab.binding.livisismarthome.internal.client.api.entity.message.MessageDTO;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the structure of a {@link DeviceDTO}.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class DeviceDTO {

    private static final String PROTOCOL_ID_COSIP = "Cosip";
    private static final String PROTOCOL_ID_VIRTUAL = "Virtual";
    private static final String PROTOCOL_ID_WMBUS = "wMBus";

    /**
     * Unique id for the device, always available in model.
     */
    private String id;

    /**
     * Identifier of the manufacturer, always available in model
     */
    private String manufacturer;

    /**
     * Version number of the device for the domain model.
     *
     * If the functionality of the device changes, the version must
     * be increased to indicate that there are new or changed attributes
     * of the device. Always available in model.
     */
    private String version;

    /**
     * Defines the product, which is used as an identifier for selecting the
     * right add-in to support the functionality of the device.
     * Remark: one add-in can support multiple devices, e.g.
     * core.RWE, which supports all RWE hardware devices (also referred to as core devices).
     * Always available in model.
     */
    private String product;

    /**
     * Device number or id like SGTIN given by the manufacturer. Optional.
     */
    @SerializedName("serialnumber")
    private String serialNumber;

    /**
     * Specifies the type of the device, which is defined by the manufacturer. The triple of device type, manufacturer
     * and the version must be unique.
     * Always available in model.
     */
    private String type;

    private DeviceConfigDTO config;

    private List<String> capabilities;

    private Map<String, CapabilityDTO> capabilityMap;

    private DeviceStateDTO deviceState;

    /*
     * The tag container can contain any number of properties for grouping of the devices in the client, e.g. category
     * of device like “security related”. The tags can be freely chosen by the client and will not be considered by the
     * system for any business logic.
     *
     * Optional.
     *
     * @Key("tags")
     * private List<Property> tagList;
     */

    /**
     * The location contains the link to the location of the device. Optional.
     */
    @SerializedName("location")
    private String locationLink;

    private transient LocationDTO location;

    private List<MessageDTO> messageList;

    private boolean lowBattery;

    /**
     * Stores, if the {@link DeviceDTO} is battery powered.
     */
    private boolean batteryPowered = false;

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
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * @param manufacturer the manufacturer to set
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return the serialnumber
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialnumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * Returns true, if the {@link DeviceDTO} has a serial number.
     *
     * @return true if the device has serial number, otherwise false
     */
    public boolean hasSerialNumber() {
        return serialNumber != null && !serialNumber.isEmpty();
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the config
     */
    public DeviceConfigDTO getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(DeviceConfigDTO config) {
        this.config = config;
    }

    /**
     * Returns the {@link DeviceStateDTO}. Only available, if device has a state. Better check with
     * {@link DeviceDTO#hasDeviceState()} first!
     *
     * @return the entityState or null
     */
    public DeviceStateDTO getDeviceState() {
        return deviceState;
    }

    /**
     * @param deviceState the deviceState to set
     */
    public void setDeviceState(DeviceStateDTO deviceState) {
        this.deviceState = deviceState;
    }

    /**
     * Returns, if the {@link DeviceDTO} has a state. Not all {@link DeviceDTO}s have a state.
     *
     * @return true if the device has a device state, otherwise false
     */
    public boolean hasDeviceState() {
        return deviceState != null;
    }

    /**
     * @return the capabilityList
     */
    public List<String> getCapabilities() {
        return Objects.requireNonNullElse(capabilities, Collections.emptyList());
    }

    /**
     * @param capabilityList the capabilityList to set
     */
    public void setCapabilities(List<String> capabilityList) {
        this.capabilities = capabilityList;
    }

    /**
     * @param capabilityMap the capabilityMap to set
     */
    public void setCapabilityMap(Map<String, CapabilityDTO> capabilityMap) {
        this.capabilityMap = capabilityMap;
    }

    /**
     * @return the capabilityMap
     */
    public Map<String, CapabilityDTO> getCapabilityMap() {
        if (capabilityMap != null) {
            return capabilityMap;
        }
        return Collections.emptyMap();
    }

    /**
     * Returns the {@link CapabilityDTO} with the given id.
     *
     * @param id capability id
     * @return capability
     */
    public CapabilityDTO getCapabilityWithId(String id) {
        return this.capabilityMap.get(id);
    }

    /**
     * @return the locationLink
     */
    public String getLocationLink() {
        return locationLink;
    }

    /**
     * @param locationLink the locationList to set
     */
    public void setLocation(String locationLink) {
        this.locationLink = locationLink;
    }

    /**
     * Returns the id of the {@link LocationDTO}
     *
     * @return location id
     */
    public String getLocationId() {
        if (locationLink != null) {
            return locationLink.replace("/location/", "");
        }
        return null;
    }

    /**
     * Returns the {@link LocationDTO} of the {@link DeviceDTO}. Better check with {@link DeviceDTO#hasLocation()}
     * first, as not
     * all devices have one.
     *
     * @return the location
     */
    public LocationDTO getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    /**
     * Returns, if the {@link DeviceDTO} has a {@link LocationDTO}. Not all devices have a {@link LocationDTO}...
     *
     * @return boolean true, if a {@link LocationDTO} is set, else false
     */
    public boolean hasLocation() {
        return location != null;
    }

    public @NonNull String getLocationName() {
        LocationDTO location = getLocation();
        if (location != null && location.getName() != null) {
            return location.getName();
        }
        return "<none>";
    }

    /**
     * @return the messageList
     */
    public List<MessageDTO> getMessageList() {
        if (messageList != null) {
            return messageList;
        }
        return Collections.emptyList();
    }

    /**
     * @param messageList the messageList to set
     */
    public void setMessageList(List<MessageDTO> messageList) {
        this.messageList = messageList;
        applyMessageList(messageList);
    }

    private void applyMessageList(List<MessageDTO> messageList) {
        if (messageList != null && !messageList.isEmpty()) {
            boolean isUnreachableMessageFound = false;
            boolean isLowBatteryMessageFound = false;
            for (final MessageDTO message : messageList) {
                switch (message.getType()) {
                    case MessageDTO.TYPE_DEVICE_UNREACHABLE:
                        isUnreachableMessageFound = true;
                        break;
                    case MessageDTO.TYPE_DEVICE_LOW_BATTERY:
                        isLowBatteryMessageFound = true;
                        break;
                }
            }
            if (isUnreachableMessageFound) {
                setReachable(false); // overwrite only when there is a corresponding message (to keep the state of the
                                     // API in other cases)
            }
            if (isLowBatteryMessageFound) {
                setLowBattery(true); // overwrite only when there is a corresponding message (to keep the state of the
                                     // API in other cases)
            }
        }
    }

    /**
     * Sets if the {@link DeviceDTO} is reachable;
     *
     * @param isReachable reachable (boolean)
     */
    private void setReachable(boolean isReachable) {
        if (getDeviceState().hasIsReachableState()) {
            getDeviceState().setReachable(isReachable);
        }
    }

    /**
     * Returns if the {@link DeviceDTO} is reachable.
     *
     * @return reachable (boolean)
     */
    public Boolean isReachable() {
        if (hasDeviceState() && getDeviceState().hasIsReachableState()) {
            return getDeviceState().isReachable();
        }
        return null;
    }

    /**
     * Sets the low battery state for the {@link DeviceDTO}.
     *
     * @param isBatteryLow true if the battery is low, otherwise false
     */
    public void setLowBattery(boolean isBatteryLow) {
        this.lowBattery = isBatteryLow;
    }

    /**
     * Returns true if the {@link DeviceDTO} has a low battery warning. Only available on battery devices.
     *
     * @return true if the battery is low, otherwise false
     */
    public boolean hasLowBattery() {
        return lowBattery;
    }

    /**
     * Returns true, if the {@link DeviceDTO} is battery powered.
     *
     * @return true if the device is battery powered, otherwise false
     */
    public boolean isBatteryPowered() {
        return batteryPowered;
    }

    /**
     * Sets if the device is battery powered.
     *
     * @param isBatteryPowerd true if the device is battery powered, otherwise false
     */
    public void setIsBatteryPowered(boolean isBatteryPowerd) {
        batteryPowered = isBatteryPowerd;
    }

    /**
     * Returns true, if the {@link DeviceDTO} has {@link MessageDTO}s.
     *
     * @return true if messages accoring the device are available, otherwise false
     */
    public boolean hasMessages() {
        return (messageList != null && !messageList.isEmpty());
    }

    /**
     * Returns true if the device is a SmartHomeController (SHC).
     *
     * @return true if the device is a SmartHomeController (SHC) otherwise false
     */
    public boolean isController() {
        return isClassicController() || DEVICE_SHCA.equals(type);
    }

    /**
     * Returns true if the device is a classic controller (SHC, before Gen 2.).
     *
     * @return true if it is a classic controller, otherwise false
     */
    public boolean isClassicController() {
        return DEVICE_SHC.equals(type);
    }

    /**
     * Returns true, if the {@link DeviceDTO} is a virtual device (e.g. a VariableActuator).
     *
     * @return true if it is a virtual device, otherwise false
     */
    public boolean isVirtualDevice() {
        return PROTOCOL_ID_VIRTUAL.equals(getConfig().getProtocolId());
    }

    /**
     * Returns true, if the {@link DeviceDTO} is a radio device.
     *
     * @return true if it is a radio device, otherwise false
     */
    public boolean isRadioDevice() {
        return isCoSipDevice() || isWMBusDevice();
    }

    /**
     * Returns true, if the {@link DeviceDTO} is a CoSip device.
     *
     * @return true if it is a CoSip device, otherwise false
     */
    public boolean isCoSipDevice() {
        return PROTOCOL_ID_COSIP.equals(getConfig().getProtocolId());
    }

    /**
     * Returns true, if the {@link DeviceDTO} is a W-Mbus device.
     *
     * @return true if it is a W-Mbus device, otherwise false
     */
    public boolean isWMBusDevice() {
        return PROTOCOL_ID_WMBUS.equals(getConfig().getProtocolId());
    }

    @Override
    public String toString() {
        return "Device [" + "id=" + getId() + " manufacturer=" + getManufacturer() + " version=" + getVersion()
                + " product=" + getProduct() + " serialnumber=" + getSerialNumber() + " type=" + getType() + " name="
                + getConfig().getName() + "]";
    }
}

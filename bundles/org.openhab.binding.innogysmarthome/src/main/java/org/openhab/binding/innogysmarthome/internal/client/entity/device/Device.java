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

import static org.openhab.binding.innogysmarthome.internal.InnogyBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.location.Location;
import org.openhab.binding.innogysmarthome.internal.client.entity.message.Message;

import com.google.gson.annotations.SerializedName;

/**
 * Defines the structure of a {@link Device}.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Device {

    public static final String DEVICE_MANUFACTURER_RWE = "RWE";
    public static final String DEVICE_MANUFACTURER_INNOGY = "innogy";

    protected static final String PROTOCOL_ID_COSIP = "Cosip";
    protected static final String PROTOCOL_ID_VIRTUAL = "Virtual";
    protected static final String PROTOCOL_ID_WMBUS = "wMBus";

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
    private String serialnumber;

    /**
     * Specifies the type of the device, which is defined by the manufacturer. The triple of device type, manufacturer
     * and the version must be unique.
     * Always available in model.
     */
    private String type;

    private DeviceConfig config;

    private List<String> capabilities;

    private Map<String, Capability> capabilityMap;

    private DeviceState deviceState;

    /**
     * The tag container can contain any number of properties for grouping of the devices in the client, e.g. category
     * of device like “security related”. The tags can be freely chosen by the client and will not be considered by the
     * system for any business logic.
     *
     * Optional.
     */
    // @Key("tags")
    // private List<Property> tagList;

    /**
     * The location contains the link to the location of the device. Optional.
     */
    @SerializedName("location")
    private String locationLink;

    private transient Location location;

    private List<Message> messageList;

    private boolean lowBattery;

    /**
     * Stores, if the {@link Device} is battery powered.
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
    public String getSerialnumber() {
        return serialnumber;
    }

    /**
     * @param serialnumber the serialnumber to set
     */
    public void setSerialnumber(String serialnumber) {
        this.serialnumber = serialnumber;
    }

    /**
     * Returns true, if the {@link Device} has a serial number.
     *
     * @return
     */
    public boolean hasSerialNumber() {
        return serialnumber != null && !serialnumber.isEmpty();
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
    public DeviceConfig getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(DeviceConfig config) {
        this.config = config;
    }

    /**
     * Returns the {@link DeviceState}. Only available, if device has a state. Better check with
     * {@link Device#hasDeviceState()} first!
     *
     * @return the entityState or null
     */
    public DeviceState getDeviceState() {
        return deviceState;
    }

    /**
     * @param deviceState the deviceState to set
     */
    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    /**
     * Returns, if the {@link Device} has a state. Not all {@link Device}s have a state.
     *
     * @return
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
    public void setCapabilityMap(Map<String, Capability> capabilityMap) {
        this.capabilityMap = capabilityMap;
    }

    /**
     * @return the capabilityMap
     */
    public Map<String, Capability> getCapabilityMap() {
        return this.capabilityMap;
    }

    /**
     * Returns the {@link Capability} with the given id.
     *
     * @param id
     * @return
     */
    public Capability getCapabilityWithId(String id) {
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
     * Returns the id of the {@link Location}
     *
     * @return
     */
    public String getLocationId() {
        if (locationLink != null) {
            return locationLink.replace("/location/", "");
        }
        return null;
    }

    /**
     * Returns the {@link Location} of the {@link Device}. Better check with {@link Device#hasLocation()} first, as not
     * all devices have one.
     *
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * Returns, if the {@link Device} has a {@link Location}. Not all devices have a {@link Location}...
     *
     * @return boolean true, if a {@link Location} is set, else false
     */
    public boolean hasLocation() {
        return location != null;
    }

    /**
     * @return the messageList
     */
    public List<Message> getMessageList() {
        return messageList;
    }

    /**
     * @param messageList the messageList to set
     */
    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        applyMessageList(messageList);
    }

    private void applyMessageList(List<Message> messageList) {
        if (messageList != null && !messageList.isEmpty()) {
            boolean isUnreachableMessageFound = false;
            boolean isLowBatteryMessageFound = false;
            for (final Message message : messageList) {
                switch (message.getType()) {
                    case Message.TYPE_DEVICE_UNREACHABLE:
                        isUnreachableMessageFound = true;
                        break;
                    case Message.TYPE_DEVICE_LOW_BATTERY:
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
     * Sets if the {@link Device} is reachable;
     *
     * @param isReachable
     */
    private void setReachable(boolean isReachable) {
        if (getDeviceState().hasIsReachableState()) {
            getDeviceState().setReachable(isReachable);
        }
    }

    /**
     * Returns if the {@link Device} is reachable.
     *
     * @return
     */
    public boolean isReachable() {
        return getDeviceState().getState().getIsReachable().getValue();
    }

    /**
     * Sets the low battery state for the {@link Device}.
     *
     * @param hasLowBattery
     */
    private void setLowBattery(boolean hasLowBattery) {
        this.lowBattery = hasLowBattery;
    }

    /**
     * Returns true if the {@link Device} has a low battery warning. Only available on battery devices.
     *
     * @return
     */
    public boolean hasLowBattery() {
        return lowBattery;
    }

    /**
     * Returns true, if the {@link Device} is battery powered.
     *
     * @return
     */
    public boolean isBatteryPowered() {
        return batteryPowered;
    }

    /**
     * Sets if the device is battery powered.
     *
     * @param hasBattery
     */
    public void setIsBatteryPowered(boolean hasBattery) {
        batteryPowered = hasBattery;
    }

    /**
     * Returns true, if the {@link Device} has {@link Message}s.
     *
     * @return
     */
    public boolean hasMessages() {
        return (messageList != null && !messageList.isEmpty());
    }

    /**
     * Returns true if the device is a controller (SHC).
     *
     * @return
     */
    public boolean isController() {
        return DEVICE_SHC.equals(type) || DEVICE_SHCA.equals(type);
    }

    /**
     * Returns true, if the device is made by RWE.
     *
     * @return
     */
    public boolean isRWEDevice() {
        return DEVICE_MANUFACTURER_RWE.equals(manufacturer);
    }

    /**
     * Returns true, if the device is made by innogy.
     *
     * @return
     */
    public boolean isInnogyDevice() {
        return DEVICE_MANUFACTURER_INNOGY.equals(manufacturer);
    }

    /**
     * Returns true, if the {@link Device} is a virtual device (e.g. a VariableActuator).
     *
     * @return
     */
    public boolean isVirtualDevice() {
        return PROTOCOL_ID_VIRTUAL.equals(getConfig().getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a radio device.
     *
     * @return
     */
    public boolean isRadioDevice() {
        return PROTOCOL_ID_COSIP.equals(getConfig().getProtocolId())
                || PROTOCOL_ID_WMBUS.equals(getConfig().getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a CoSip device.
     *
     * @return
     */
    public boolean isCoSipDevice() {
        return PROTOCOL_ID_COSIP.equals(getConfig().getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a W-Mbus device.
     *
     * @return
     */
    public boolean isWMBusDevice() {
        return PROTOCOL_ID_WMBUS.equals(getConfig().getProtocolId());
    }

    @Override
    public String toString() {
        final String string = "Device [" + "id=" + getId() + " manufacturer=" + getManufacturer() + " version="
                + getVersion() + " product=" + getProduct() + " serialnumber=" + getSerialnumber() + " type="
                + getType() + " name=" + getConfig().getName() + "]";
        return string;
    }
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.device;

import java.util.HashMap;
import java.util.List;

import org.openhab.binding.innogysmarthome.internal.client.entity.ConfigPropertyList;
import org.openhab.binding.innogysmarthome.internal.client.entity.Location;
import org.openhab.binding.innogysmarthome.internal.client.entity.Message;
import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.capability.Capability;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.CapabilityLink;
import org.openhab.binding.innogysmarthome.internal.client.entity.link.Link;
import org.openhab.binding.innogysmarthome.internal.client.entity.state.DeviceState;

import com.google.api.client.util.Key;

/**
 * Defines the structure of a {@link Device}.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public class Device extends ConfigPropertyList {

    public static final String DEVICE_TYPE_SHC = "SHC";
    public static final String DEVICE_TYPE_ANALOG_METER = "AnalogMeter";
    public static final String DEVICE_TYPE_BRC8 = "BRC8";
    public static final String DEVICE_TYPE_GENERATION_METER = "GenerationMeter";
    public static final String DEVICE_TYPE_ISC2 = "ISC2";
    public static final String DEVICE_TYPE_ISD2 = "ISD2";
    public static final String DEVICE_TYPE_ISR2 = "ISR2";
    public static final String DEVICE_TYPE_ISS2 = "ISS2";
    public static final String DEVICE_TYPE_PSD = "PSD";
    public static final String DEVICE_TYPE_PSS = "PSS";
    public static final String DEVICE_TYPE_PSSO = "PSSO";
    public static final String DEVICE_TYPE_RST = "RST";
    public static final String DEVICE_TYPE_SMARTMETER = "SmartMeter";
    public static final String DEVICE_TYPE_TWO_WAY_METER = "TwoWayMeter";
    public static final String DEVICE_TYPE_VARIABLE_ACTUATOR = "VariableActuator";
    public static final String DEVICE_TYPE_WDS = "WDS";
    public static final String DEVICE_TYPE_WMD = "WMD";
    public static final String DEVICE_TYPE_WMDO = "WMDO";
    public static final String DEVICE_TYPE_WRT = "WRT";
    public static final String DEVICE_TYPE_WSC2 = "WSC2";
    public static final String DEVICE_TYPE_WSD = "WSD";
    public static final String DEVICE_TYPE_WSD2 = "WSD2";

    public static final String DEVICE_MANUFACTURER_RWE = "RWE";
    public static final String DEVICE_MANUFACTURER_INNOGY = "innogy";

    /**
     * Unique id for the device, always available in model.
     */
    @Key("id")
    private String id;

    /**
     * Identifier of the manufacturer, always available in model
     */
    @Key("manufacturer")
    private String manufacturer;

    /**
     * Version number of the device for the domain model.
     *
     * If the functionality of the device changes, the version must
     * be increased to indicate that there are new or changed attributes
     * of the device. Always available in model.
     */
    @Key("version")
    private String version;

    /**
     * Defines the product, which is used as an identifier for selecting the
     * right add-in to support the functionality of the device.
     * Remark: one add-in can support multiple devices, e.g.
     * core.RWE, which supports all RWE hardware devices (also referred to as core devices).
     * Always available in model.
     */
    @Key("product")
    private String product;

    /**
     * Device number or id like SGTIN given by the manufacturer. Optional.
     */
    @Key("serialnumber")
    private String serialnumber;

    /**
     * Link to the metadata of that specific device. The link should be complete and can be followed without further
     * additions. The triple of device type, manufacturer and version define the unique path to the metadata.
     *
     * Optional.
     */
    @Key("desc")
    private String desc;

    /**
     * Specifies the type of the device, which is defined by the manufacturer. The triple of device type, manufacturer
     * and the version must be unique.
     * Always available in model.
     */
    @Key("type")
    private String type;

    private DeviceState deviceState;

    /**
     * The tag container can contain any number of properties for grouping of the devices in the client, e.g. category
     * of device like “security related”. The tags can be freely chosen by the client and will not be considered by the
     * system for any business logic.
     *
     * Optional.
     */
    @Key("Tags")
    private List<Property> tagList;

    /**
     * Contains a list of the device capabilities.
     *
     * Optional.
     */
    @Key("Capabilities")
    private List<CapabilityLink> capabilityLinkList;

    private HashMap<String, Capability> capabilityMap;

    /**
     * The location contains the link to the location of the device. Optional.
     */
    @Key("Location")
    private List<Link> locationLinkList;

    private Location location;

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
        return (serialnumber != null && serialnumber != "");
    }

    /**
     * Returns a relative link to the description.
     *
     * @return the desc
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Sets the description.
     *
     * @param desc the desc to set
     */
    public void setDescription(String desc) {
        this.desc = desc;
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
     * Returns the {@link DeviceState}. Only available, if device has a state. Better check with
     * {@link Device#hasState()} first!
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
    public boolean hasState() {
        return deviceState != null;
    }

    /**
     * @return the tagList
     */
    public List<Property> getTagList() {
        return tagList;
    }

    /**
     * @param tagList the tagList to set
     */
    public void setTagList(List<Property> tagList) {
        this.tagList = tagList;
    }

    /**
     * @return the capabilityList
     */
    public List<CapabilityLink> getCapabilityLinkList() {
        return capabilityLinkList;
    }

    /**
     * @param capabilityList the capabilityList to set
     */
    public void setCapabilityList(List<CapabilityLink> capabilityList) {
        this.capabilityLinkList = capabilityList;
    }

    /**
     * @param capabilityMap the capabilityMap to set
     */
    public void setCapabilityMap(HashMap<String, Capability> capabilityMap) {
        this.capabilityMap = capabilityMap;
    }

    public HashMap<String, Capability> getCapabilityMap() {
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
     * @return the locationList
     */
    public List<Link> getLocationLinkList() {
        return locationLinkList;
    }

    /**
     * @param locationList the locationList to set
     */
    public void setLocationList(List<Link> locationList) {
        this.locationLinkList = locationList;
    }

    public String getLocationId() {
        if (locationLinkList != null) {
            Link locationLink = locationLinkList.get(0);
            if (locationLink != null) {
                return locationLink.getValue().replace("/location/", "");
            }
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

        for (Message m : messageList) {
            setLowBattery(m.getType().equals(Message.TYPE_DEVICE_LOW_BATTERY));
            setReachable(!m.getType().equals(Message.TYPE_DEVICE_UNREACHABLE));
        }
    }

    /**
     * Sets if the {@link Device} is reachable;
     *
     * @param isReachable
     */
    public void setReachable(boolean isReachable) {
        getDeviceState().setReachable(isReachable);
    }

    /**
     * Returns if the {@link Device} is reachable.
     *
     * @return
     */
    public boolean isReachable() {
        return getDeviceState().isReachable();
    }

    /**
     * Sets the low battery state for the {@link Device}.
     *
     * @param hasLowBattery
     */
    public void setLowBattery(boolean hasLowBattery) {
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
        return DEVICE_TYPE_SHC.equals(type);
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
        return PROTOCOL_ID_VIRTUAL.equals(getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a radio device.
     *
     * @return
     */
    public boolean isRadioDevice() {
        return PROTOCOL_ID_COSIP.equals(getProtocolId()) || PROTOCOL_ID_WMBUS.equals(getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a CoSip device.
     *
     * @return
     */
    public boolean isCoSipDevice() {
        return PROTOCOL_ID_COSIP.equals(getProtocolId());
    }

    /**
     * Returns true, if the {@link Device} is a W-Mbus device.
     *
     * @return
     */
    public boolean isWMBusDevice() {
        return PROTOCOL_ID_WMBUS.equals(getProtocolId());
    }

    @Override
    public String toString() {
        String string = "Device [" + "id=" + getId() + " manufacturer=" + getManufacturer() + " version=" + getVersion()
                + " product=" + getProduct() + " serialnumber=" + getSerialnumber() + " type=" + getType() + " name="
                + getName() + "]";
        return string;
    }

}

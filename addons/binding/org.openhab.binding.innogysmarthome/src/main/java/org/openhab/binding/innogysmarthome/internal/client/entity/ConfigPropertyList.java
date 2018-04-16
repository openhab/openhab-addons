/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.innogysmarthome.internal.client.Util;
import org.openhab.binding.innogysmarthome.internal.client.entity.device.Device;

import com.google.api.client.util.Key;

/**
 * Defines a list of configuration properties. It will be extended by the {@link Device}, that holds a list of config
 * properties.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public abstract class ConfigPropertyList extends PropertyList {

    /** config property names */
    protected static final String CONFIG_PROPERTY_NAME = "Name";
    protected static final String CONFIG_PROPERTY_PROTOCOL_ID = "ProtocolId";
    protected static final String CONFIG_PROPERTY_TIME_OF_ACCEPTANCE = "TimeOfAcceptance";
    protected static final String CONFIG_PROPERTY_TIME_OF_DISCOVERY = "TimeOfDiscovery";
    protected static final String CONFIG_PROPERTY_HARDWARE_VERSION = "HardwareVersion";
    protected static final String CONFIG_PROPERTY_SOFTWARE_VERSION = "SoftwareVersion";
    protected static final String CONFIG_PROPERTY_FIRMWARE_VERSION = "FirmwareVersion";
    protected static final String CONFIG_PROPERTY_HOSTNAME = "HostName";
    protected static final String CONFIG_PROPERTY_ACTIVITY_LOG_ENABLED = "ActivityLogEnabled";
    protected static final String CONFIG_PROPERTY_CONFIGURATION_STATE = "ConfigurationState";
    protected static final String CONFIG_PROPERTY_GEOLOCATION = "GeoLocation";
    protected static final String CONFIG_PROPERTY_TIMEZONE = "TimeZone";
    protected static final String CONFIG_PROPERTY_CURRENT_UTC_OFFSET = "CurrentUTCOffset";
    protected static final String CONFIG_PROPERTY_IP_ADDRESS = "IPAddress";
    protected static final String CONFIG_PROPERTY_MAC_ADDRESS = "MACAddress";
    protected static final String CONFIG_PROPERTY_SHC_TYPE = "ShcType";
    protected static final String CONFIG_PROPERTY_BACKEND_CONNECTION_MONITORED = "BackendConnectionMonitored";
    protected static final String CONFIG_PROPERTY_RFCOM_FAILURE_NOTIFICATION = "RFCommFailureNotification";
    protected static final String CONFIG_PROPERTY_POSTCODE = "PostCode";
    protected static final String CONFIG_PROPERTY_CITY = "City";
    protected static final String CONFIG_PROPERTY_STREET = "Street";
    protected static final String CONFIG_PROPERTY_HOUSENUMBER = "HouseNumber";
    protected static final String CONFIG_PROPERTY_COUNTRY = "Country";
    protected static final String CONFIG_PROPERTY_HOUSEHOLD_TYPE = "HouseholdType";
    protected static final String CONFIG_PROPERTY_NUMBER_OF_PERSONS = "NumberOfPersons";
    protected static final String CONFIG_PROPERTY_NUMBER_OF_FLOORS = "NumberOfFloors";
    protected static final String CONFIG_PROPERTY_LIVINGAREA = "LivingArea";
    protected static final String CONFIG_PROPERTY_REGISTRATION_TIME = "RegistrationTime";
    protected static final String CONFIG_PROPERTY_DISPLAY_CURRENT_TEMPERATURE = "DisplayCurrentTemperature";
    protected static final String CONFIG_PROPERTY_UNDERLYING_DEVICE_IDS = "UnderlyingDeviceIds";
    protected static final String CONFIG_PROPERTY_METER_ID = "MeterId";
    protected static final String CONFIG_PROPERTY_METER_FIRMWARE_VERSION = "MeterFirmwareVersion";
    protected static final String CONFIG_PROPERTY_DEVICE_TYPE = "DeviceType";

    protected static final String PROTOCOL_ID_COSIP = "Cosip";
    protected static final String PROTOCOL_ID_VIRTUAL = "Virtual";
    protected static final String PROTOCOL_ID_WMBUS = "wMBus";

    /**
     * This represents a container of all configuration properties.
     *
     * Optional.
     */
    @Key("Config")
    protected List<Property> configList;
    protected HashMap<String, Property> configMap;

    /**
     * @return the configList
     */
    public List<Property> getConfigList() {
        return configList;
    }

    /**
     * @param configList the configList to set
     */
    public void setConfigList(List<Property> configList) {
        this.configList = configList;
    }

    /**
     * Returns true, if config properties are available.
     *
     * @return
     */
    public boolean hasConfigProperties() {
        return (configList != null && !configList.isEmpty());
    }

    /*
     * (non-Javadoc)
     *
     * @see in.ollie.innogysmarthome.entity.PropertyList#getPropertyMap()
     */
    @Override
    protected Map<String, Property> getPropertyMap() {
        if (configMap == null) {
            configMap = PropertyList.getHashMap(configList);
        }

        return configMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see in.ollie.innogysmarthome.entity.PropertyList#getPropertyList()
     */
    @Override
    protected List<Property> getPropertyList() {
        if (configList == null) {
            configList = new ArrayList<>();
        }

        return configList;
    }

    /**
     * @return
     */
    public Map<String, Property> getConfigMap() {
        return getPropertyMap();
    }

    /**
     * Returns the name of the {@link Device}.
     *
     * @return
     */
    public String getName() {
        return getPropertyValueAsString(CONFIG_PROPERTY_NAME);
    }

    public String getProtocolId() {
        return getPropertyValueAsString(CONFIG_PROPERTY_PROTOCOL_ID);
    }

    /**
     * Returns the time, when the {@link Device} was added to the SHC configuration.
     *
     * @return
     */
    public ZonedDateTime getTimeOfAcceptance() {
        String time = getPropertyValueAsString(CONFIG_PROPERTY_TIME_OF_ACCEPTANCE);
        if (time == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(time);
    }

    /**
     * Returns the time, when the {@link Device} was discovered by the SHC.
     *
     * @return
     */
    public ZonedDateTime getTimeOfDiscovery() {
        String time = getPropertyValueAsString(CONFIG_PROPERTY_TIME_OF_DISCOVERY);
        if (time == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(time);
    }

    public String getHardwareVersion() {
        return getPropertyValueAsString(CONFIG_PROPERTY_HARDWARE_VERSION);
    }

    public String getSoftwareVersion() {
        return getPropertyValueAsString(CONFIG_PROPERTY_SOFTWARE_VERSION);
    }

    public String getFirmwareVersion() {
        return getPropertyValueAsString(CONFIG_PROPERTY_FIRMWARE_VERSION);
    }

    public String getHostName() {
        return getPropertyValueAsString(CONFIG_PROPERTY_HOSTNAME);
    }

    public Boolean getActivityLogEnabled() {
        return getPropertyValueAsBoolean(CONFIG_PROPERTY_ACTIVITY_LOG_ENABLED);
    }

    public String getConfigurationState() {
        return getPropertyValueAsString(CONFIG_PROPERTY_CONFIGURATION_STATE);
    }

    public String getGeoLocation() {
        return getPropertyValueAsString(CONFIG_PROPERTY_GEOLOCATION);
    }

    public String getTimeZone() {
        return getPropertyValueAsString(CONFIG_PROPERTY_TIMEZONE);
    }

    public Integer getCurrentUTCOffset() {
        return getPropertyValueAsInteger(CONFIG_PROPERTY_CURRENT_UTC_OFFSET);
    }

    public String getIpAddress() {
        return getPropertyValueAsString(CONFIG_PROPERTY_IP_ADDRESS);
    }

    public String getMacAddress() {
        return getPropertyValueAsString(CONFIG_PROPERTY_MAC_ADDRESS);
    }

    public String getSHCType() {
        return getPropertyValueAsString(CONFIG_PROPERTY_SHC_TYPE);
    }

    public Boolean getBackendConnectionMonitored() {
        return getPropertyValueAsBoolean(CONFIG_PROPERTY_BACKEND_CONNECTION_MONITORED);
    }

    public Boolean getRFCommFailureNotification() {
        return getPropertyValueAsBoolean(CONFIG_PROPERTY_RFCOM_FAILURE_NOTIFICATION);
    }

    public String getPostCode() {
        return getPropertyValueAsString(CONFIG_PROPERTY_POSTCODE);
    }

    public String getCity() {
        return getPropertyValueAsString(CONFIG_PROPERTY_CITY);
    }

    public String getStreet() {
        return getPropertyValueAsString(CONFIG_PROPERTY_STREET);
    }

    public String getHouseNumber() {
        return getPropertyValueAsString(CONFIG_PROPERTY_HOUSENUMBER);
    }

    public String getCountry() {
        return getPropertyValueAsString(CONFIG_PROPERTY_COUNTRY);
    }

    public String getHouseHoldType() {
        return getPropertyValueAsString(CONFIG_PROPERTY_HOUSEHOLD_TYPE);
    }

    public Integer getNumberOfPersons() {
        return getPropertyValueAsInteger(CONFIG_PROPERTY_NUMBER_OF_PERSONS);
    }

    public Integer getNumberOfFloors() {
        return getPropertyValueAsInteger(CONFIG_PROPERTY_NUMBER_OF_FLOORS);
    }

    public Integer getLivingArea() {
        return getPropertyValueAsInteger(CONFIG_PROPERTY_LIVINGAREA);
    }

    public ZonedDateTime getRegistrationTime() {
        return Util.convertZuluTimeStringToDate(getPropertyValueAsString(CONFIG_PROPERTY_REGISTRATION_TIME));
    }

    public String getRegistrationTimeFormattedString() {
        return Util.convertZuluTimeStringToDate(getPropertyValueAsString(CONFIG_PROPERTY_REGISTRATION_TIME))
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
    }

    public String getDisplayCurrentTemperature() {
        return getPropertyValueAsString(CONFIG_PROPERTY_DISPLAY_CURRENT_TEMPERATURE);
    }

    public String getUnderLyingDeviceIds() {
        return getPropertyValueAsString(CONFIG_PROPERTY_UNDERLYING_DEVICE_IDS);
    }

    public String getMeterId() {
        return getPropertyValueAsString(CONFIG_PROPERTY_METER_ID);
    }

    public String getMeterFirmwareVersion() {
        return getPropertyValueAsString(CONFIG_PROPERTY_METER_FIRMWARE_VERSION);
    }

    public String getDeviceType() {
        return getPropertyValueAsString(CONFIG_PROPERTY_DEVICE_TYPE);
    }
}

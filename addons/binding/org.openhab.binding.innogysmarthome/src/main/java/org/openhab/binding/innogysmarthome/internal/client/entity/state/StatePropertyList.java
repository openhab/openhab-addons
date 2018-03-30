/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity.state;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.innogysmarthome.internal.client.Util;
import org.openhab.binding.innogysmarthome.internal.client.entity.Property;
import org.openhab.binding.innogysmarthome.internal.client.entity.PropertyList;

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

/**
 * The {@link StatePropertyList} holds a list of state properties.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public abstract class StatePropertyList extends PropertyList {

    /** state property field names */
    protected static final String STATE_PROPERTY_KEY_NAME = "name";
    protected static final String STATE_PROPERTY_KEY_VALUE = "value";
    protected static final String STATE_PROPERTY_KEY_LASTCHANGED = "lastchanged";

    /** state name list */
    protected static final String STATE_NAME_ISREACHABLE = "IsReachable";
    protected static final String STATE_NAME_DEVICECONFIGURATIONSTATE = "DeviceConfigurationState";
    protected static final String STATE_NAME_DEVICEINCLUSIONSTATE = "DeviceInclusionState";
    protected static final String STATE_NAME_UPDATESTATE = "UpdateState";
    protected static final String STATE_NAME_FIRMWAREVERSION = "FirmwareVersion";

    /** state name list - SHC specials */
    protected static final String STATE_NAME_UPDATEAVAILABLE = "UpdateAvailable";
    protected static final String STATE_NAME_LASTREBOOT = "LastReboot";
    protected static final String STATE_NAME_MBUSDONGLEATTACHED = "MBusDongleAttached";
    protected static final String STATE_NAME_LBDONGLEATTACHED = "LBDongleAttached";
    protected static final String STATE_NAME_CONFIGVERSION = "ConfigVersion";
    protected static final String STATE_NAME_OSSTATE = "OSState";
    protected static final String STATE_NAME_MEMORYLOAD = "MemoryLoad";
    protected static final String STATE_NAME_CPULOAD = "CPULoad";

    protected static final String DEVICE_INCLUSION_STATE_INCLUDED = "Included";
    protected static final String DEVICE_INCLUSION_STATE_PENDING = "InclusionPending";

    protected static final String DEVICE_UPDATE_STATE_UPTODATE = "UpToDate";

    /**
     * This represents a container of all configuration properties.
     *
     * Optional.
     */
    @Key("State")
    protected List<Property> stateList;

    protected HashMap<String, Property> stateMap;

    /**
     * @return the stateList
     */
    public List<Property> getStateList() {
        return stateList;
    }

    /**
     * @param stateList the stateList to set
     */
    public void setStateList(List<Property> stateList) {
        this.stateList = stateList;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.innogysmarthome.internal.client.entity.PropertyList#getPropertyMap()
     */
    @Override
    protected Map<String, Property> getPropertyMap() {
        if (stateMap == null) {
            stateMap = PropertyList.getHashMap(stateList);
        }

        return stateMap;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.innogysmarthome.internal.client.entity.PropertyList#getPropertyList()
     */
    @Override
    protected List<Property> getPropertyList() {
        if (stateList == null) {
            stateList = new ArrayList<>();
        }

        return stateList;
    }

    /**
     * @return
     */
    public Map<String, Property> getStateMap() {
        return getPropertyMap();
    }

    /**
     * Returns the name of the state.
     *
     * @return
     */
    public String getName() {
        return getPropertyValueAsString(STATE_PROPERTY_KEY_NAME);
    }

    /**
     * Returns the value of the state.
     *
     * @return
     */
    public Object getValue() {
        return getPropertyValue(STATE_PROPERTY_KEY_VALUE);
    }

    /**
     * Returns the {@link DateTime}, when the last state change occurred.
     *
     * @return
     */
    public ZonedDateTime getLastChanged() {
        String time = getPropertyValueAsString(STATE_PROPERTY_KEY_LASTCHANGED);
        if (time == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(time);
    }

    /**
     * Returns the String of the available Update or an empty {@link String} if none.
     *
     * @return
     */
    public String getUpdateAvailable() {
        return getPropertyValueAsString(STATE_NAME_UPDATEAVAILABLE);
    }

    /**
     * Return the {@link DateTime} of the last reboot.
     *
     * @return
     */
    public ZonedDateTime getLastReboot() {
        String time = getPropertyValueAsString(STATE_NAME_LASTREBOOT);
        if (time == null) {
            return null;
        }
        return Util.convertZuluTimeStringToDate(time);
    }

    /**
     * Returns the config version of the smarthome setup.
     *
     * The config version changes everytime, the configuration on the controller is changed and saved.
     *
     * @return
     */
    public Integer getConfigVersion() {
        return getPropertyValueAsInteger(STATE_NAME_CONFIGVERSION);
    }
}

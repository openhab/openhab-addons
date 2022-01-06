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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices;

import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;

/**
 * The {@link Circuit} represents a circuit of the digitalStrom system. For that all information will be able to get and
 * set through the same named getter- and setter-methods. To get informed about status and configuration changes a
 * {@link DeviceStatusListener} can be registered. For that and to get the general device informations like the dSID the
 * {@link Circuit} implements the {@link GeneralDeviceInformations} interface.
 *
 * @author Michael Ochel - initial contributer
 * @author Matthias Siegele - initial contributer
 */
public interface Circuit extends GeneralDeviceInformation {

    /**
     * Returns the hardware version of this {@link Circuit} as {@link Integer}.
     *
     * @return hardware version
     */
    Integer getHwVersion();

    /**
     * Sets the hardware version of this {@link Circuit} as {@link Integer}.
     *
     * @param hwVersion the new hardware version as {@link Integer}
     */
    void setHwVersion(Integer hwVersion);

    /**
     * Returns the hardware version of this {@link Circuit} as {@link String}.
     *
     * @return hardware version
     */
    String getHwVersionString();

    /**
     * Sets the hardware version of this {@link Circuit} as {@link String}.
     *
     * @param hwVersionString the new hardware version as {@link Integer}
     */
    void setHwVersionString(String hwVersionString);

    /**
     * Returns the software version of this {@link Circuit} as {@link String}.
     *
     * @return the software version
     */
    String getSwVersion();

    /**
     * Sets the software version of this {@link Circuit} as {@link String}.
     *
     * @param swVersion the new software version
     */
    void setSwVersion(String swVersion);

    /**
     * Returns the arm software version of this {@link Circuit} as {@link Integer}.
     *
     * @return the arm software version
     */
    Integer getArmSwVersion();

    /**
     * Sets the arm software version of this {@link Circuit} as {@link Integer}.
     *
     * @param armSwVersion the new arm software version
     */
    void setArmSwVersion(Integer armSwVersion);

    /**
     * Returns the dsp software version of this {@link Circuit} as {@link Integer}.
     *
     * @return the dsp softwaree version
     */
    Integer getDspSwVersion();

    /**
     * Sets the dsp software version of this {@link Circuit} as {@link Integer}.
     *
     * @param dspSwVersion the new dsp software version
     */
    void setDspSwVersion(Integer dspSwVersion);

    /**
     * Returns the api version of this {@link Circuit} as {@link Integer}.
     *
     * @return the api version as {@link Integer}
     */
    Integer getApiVersion();

    /**
     * Setss the api version of this {@link Circuit} as {@link Integer}.
     *
     * @param apiVersion the new api version
     */
    void setApiVersion(Integer apiVersion);

    /**
     * Returns the hardware name of this {@link Circuit}.
     *
     * @return the hardware name
     */
    String getHwName();

    /**
     * Sets the hardware name of this {@link Circuit}.
     *
     * @param hwName the new hardware name
     */
    void setHwName(String hwName);

    /**
     * Returns the bus member type of this {@link Circuit} as {@link Integer}.
     *
     * @return the bus member type
     */
    Integer getBusMemberType();

    /**
     * Sets the bus member type of this {@link Circuit} as {@link Integer}.
     *
     * @param busMemberType the new bus member type
     */
    void setBusMemberType(Integer busMemberType);

    /**
     * Returns true, if this {@link Circuit} has connected {@link Device}'s, otherwise false.
     *
     * @return true, if {@link Device}'s are connected
     */
    Boolean getHasDevices();

    /**
     * Sets the connected devices flag.
     *
     * @param hasDevices the new connected devices flag
     */
    void setHasDevices(Boolean hasDevices);

    /**
     * Returns true, if this {@link Circuit} is valid to metering power data, otherwise false.
     *
     * @return true, if is valid to metering power data
     */
    Boolean getHasMetering();

    /**
     * Sets the flag hasMetering.
     *
     * @param hasMetering the new hasMetering flag.
     */
    void setHasMetering(Boolean hasMetering);

    /**
     * Returns the vdc configuration URL of this {@link Circuit} as {@link String}.
     *
     * @return the vdc configuration URL
     */
    String getVdcConfigURL();

    /**
     * Sets the vdc configuration URL of this {@link Circuit} as {@link String}.
     *
     * @param vdcConfigURL the new vdc configuration URL
     */
    void setVdcConfigURL(String vdcConfigURL);

    /**
     * Returns the vdc mode UID of this {@link Circuit} as {@link String}.
     *
     * @return the vdc mode UID
     */
    String getVdcModelUID();

    /**
     * Sets the vdc mode UID of this {@link Circuit} as {@link String}.
     *
     * @param vdcModelUID the new vdc mode UID
     */
    void setVdcModelUID(String vdcModelUID);

    /**
     * Returns the vdc hardware GUID of this {@link Circuit} as {@link String}.
     *
     * @return the vdc hardware GUID
     */
    String getVdcHardwareGuid();

    /**
     * Sets the vdc hardware GUID of this {@link Circuit} as {@link String}.
     *
     * @param vdcHardwareGuid the new vdc hardware GUID
     */
    void setVdcHardwareGuid(String vdcHardwareGuid);

    /**
     * Returns the vdc hardware model GUID of this {@link Circuit} as {@link String}.
     *
     * @return the vdc hardware mode GUID
     */
    String getVdcHardwareModelGuid();

    /**
     * Sets the vdc hardware model GUID of this {@link Circuit} as {@link String}.
     *
     * @param vdcHardwareModelGuid the new vdc model GUID
     */
    void setVdcHardwareModelGuid(String vdcHardwareModelGuid);

    /**
     * Returns the vdc vendor GUID of this {@link Circuit} as {@link String}.
     *
     * @return the vdc vendor GUID
     */
    String getVdcVendorGuid();

    /**
     * Sets the vdc vendor GUID of this {@link Circuit} as {@link String}.
     *
     * @param vdcVendorGuid the new vdc vendor GUID
     */
    void setVdcVendorGuid(String vdcVendorGuid);

    /**
     * Returns the vdc oem GUID of this {@link Circuit} as {@link String}.
     *
     * @return the vdc oem GUID
     */
    String getVdcOemGuid();

    /**
     * Sets the vdc oem GUID of this {@link Circuit} as {@link String}.
     *
     * @param vdcOemGuid the new vdc oem GUID
     */
    void setVdcOemGuid(String vdcOemGuid);

    /**
     * Returns true, if actions from new {@link Device}'s will be ignored by this {@link Circuit}, otherwise false.
     *
     * @return true, if actions form new device will be ignored
     */
    Boolean getIgnoreActionsFromNewDevices();

    /**
     * Sets the flag for ignore actions from new {@link Device}'s.
     *
     * @param ignoreActionsFromNewDevices the new ignore actions from new devices flag
     */
    void setIgnoreActionsFromNewDevices(Boolean ignoreActionsFromNewDevices);

    /**
     * Adds a new {@link CachedMeteringValue} or update the existing, if the new one is newer.
     *
     * @param cachedMeteringValue the new {@link CachedMeteringValue}
     */
    void addMeteringValue(CachedMeteringValue cachedMeteringValue);

    /**
     * Returns the value of the given {@link CachedMeteringValue} through the {@link MeteringTypeEnum} and
     * {@link MeteringUnitsEnum}.
     *
     * @param meteringType (must not be null)
     * @param meteringUnit (can be null, default is {@link MeteringUnitsEnum#WH})
     * @return the metering value or -1, if the metering value dose not exist
     */
    double getMeteringValue(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit);

    /**
     * Returns the {@link List} of all {@link CachedMeteringValue}'s.
     *
     * @return list of all {@link CachedMeteringValue}
     */
    List<CachedMeteringValue> getAllCachedMeteringValues();
}

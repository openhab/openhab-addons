/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.impl;

import java.util.Arrays;
import java.util.List;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.AbstractGeneralDeviceInformations;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.Circuit;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.CachedMeteringValue;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.DeviceStateUpdate;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringTypeEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.MeteringUnitsEnum;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.impl.DeviceStateUpdateImpl;

import com.google.gson.JsonObject;

/**
 * The {@link CircuitImpl} is the implementation of the {@link Circuit} and represent a digitalSTROM circuit.
 *
 * @author Michael Ochel - Initial contribution
 * @author Matthias Siegele - Initial contribution
 */
public class CircuitImpl extends AbstractGeneralDeviceInformations implements Circuit {

    // Config
    private Integer hwVersion;
    private String hwVersionString;
    private String swVersion;
    private Integer armSwVersion;
    private Integer dspSwVersion;
    private Integer apiVersion;
    private String hwName;
    private Integer busMemberType;
    private Boolean hasDevices;
    private Boolean hasMetering;
    private String vdcConfigURL;
    private String vdcModelUID;
    private String vdcHardwareGuid;
    private String vdcHardwareModelGuid;
    private String vdcVendorGuid;
    private String vdcOemGuid;
    private Boolean ignoreActionsFromNewDevices;

    // Metering
    private CachedMeteringValue consumption;
    private CachedMeteringValue energyWh;
    private CachedMeteringValue energyWs;
    // currently energyDelta not exist
    // private CachedMeteringValue energyDeltaWh;
    // private CachedMeteringValue energyDeltaWs;

    /**
     * Creates a new {@link CircuitImpl} through the digitalSTROM json response as {@link JsonObject}.
     *
     * @param jObject of the digitalSTROM json response, must not be null
     */
    public CircuitImpl(JsonObject jObject) {
        super(jObject);
        if (jObject.get(JSONApiResponseKeysEnum.HW_VERSION.getKey()) != null) {
            hwVersion = jObject.get(JSONApiResponseKeysEnum.HW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HW_VERSION_STRING.getKey()) != null) {
            hwVersionString = jObject.get(JSONApiResponseKeysEnum.HW_VERSION_STRING.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.SW_VERSION.getKey()) != null) {
            swVersion = jObject.get(JSONApiResponseKeysEnum.SW_VERSION.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.ARM_SW_VERSION.getKey()) != null) {
            armSwVersion = jObject.get(JSONApiResponseKeysEnum.ARM_SW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.DSP_SW_VERSION.getKey()) != null) {
            dspSwVersion = jObject.get(JSONApiResponseKeysEnum.DSP_SW_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.API_VERSION.getKey()) != null) {
            apiVersion = jObject.get(JSONApiResponseKeysEnum.API_VERSION.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HW_NAME.getKey()) != null) {
            hwName = jObject.get(JSONApiResponseKeysEnum.HW_NAME.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.BUS_MEMBER_TYPE.getKey()) != null) {
            busMemberType = jObject.get(JSONApiResponseKeysEnum.BUS_MEMBER_TYPE.getKey()).getAsInt();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HAS_DEVICES.getKey()) != null) {
            hasDevices = jObject.get(JSONApiResponseKeysEnum.HAS_DEVICES.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.HAS_METERING.getKey()) != null) {
            hasMetering = jObject.get(JSONApiResponseKeysEnum.HAS_METERING.getKey()).getAsBoolean();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_CONFIG_URL.getKey()) != null) {
            vdcConfigURL = jObject.get(JSONApiResponseKeysEnum.VDC_CONFIG_URL.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_MODEL_UID.getKey()) != null) {
            vdcModelUID = jObject.get(JSONApiResponseKeysEnum.VDC_MODEL_UID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_GUID.getKey()) != null) {
            vdcHardwareGuid = jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_MODEL_GUID.getKey()) != null) {
            vdcHardwareModelGuid = jObject.get(JSONApiResponseKeysEnum.VDC_HARDWARE_MODEL_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_VENDOR_GUID.getKey()) != null) {
            vdcVendorGuid = jObject.get(JSONApiResponseKeysEnum.VDC_VENDOR_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.VDC_OEM_GUID.getKey()) != null) {
            vdcOemGuid = jObject.get(JSONApiResponseKeysEnum.VDC_OEM_GUID.getKey()).getAsString();
        }
        if (jObject.get(JSONApiResponseKeysEnum.IGNORE_ACTIONS_FROM_NEW_DEVICES.getKey()) != null) {
            ignoreActionsFromNewDevices = jObject.get(JSONApiResponseKeysEnum.IGNORE_ACTIONS_FROM_NEW_DEVICES.getKey())
                    .getAsBoolean();
        }
    }

    @Override
    public Integer getHwVersion() {
        return hwVersion;
    }

    @Override
    public void setHwVersion(Integer hwVersion) {
        this.hwVersion = hwVersion;
    }

    @Override
    public String getHwVersionString() {
        return hwVersionString;
    }

    @Override
    public void setHwVersionString(String hwVersionString) {
        this.hwVersionString = hwVersionString;
    }

    @Override
    public String getSwVersion() {
        return swVersion;
    }

    @Override
    public void setSwVersion(String swVersion) {
        this.swVersion = swVersion;
    }

    @Override
    public Integer getArmSwVersion() {
        return armSwVersion;
    }

    @Override
    public void setArmSwVersion(Integer armSwVersion) {
        this.armSwVersion = armSwVersion;
    }

    @Override
    public Integer getDspSwVersion() {
        return dspSwVersion;
    }

    @Override
    public void setDspSwVersion(Integer dspSwVersion) {
        this.dspSwVersion = dspSwVersion;
    }

    @Override
    public Integer getApiVersion() {
        return apiVersion;
    }

    @Override
    public void setApiVersion(Integer apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String getHwName() {
        return hwName;
    }

    @Override
    public void setHwName(String hwName) {
        this.hwName = hwName;
    }

    @Override
    public Integer getBusMemberType() {
        return busMemberType;
    }

    @Override
    public void setBusMemberType(Integer busMemberType) {
        this.busMemberType = busMemberType;
    }

    @Override
    public Boolean getHasDevices() {
        return hasDevices;
    }

    @Override
    public void setHasDevices(Boolean hasDevices) {
        this.hasDevices = hasDevices;
    }

    @Override
    public Boolean getHasMetering() {
        return hasMetering;
    }

    @Override
    public void setHasMetering(Boolean hasMetering) {
        this.hasMetering = hasMetering;
    }

    @Override
    public String getVdcConfigURL() {
        return vdcConfigURL;
    }

    @Override
    public void setVdcConfigURL(String vdcConfigURL) {
        this.vdcConfigURL = vdcConfigURL;
    }

    @Override
    public String getVdcModelUID() {
        return vdcModelUID;
    }

    @Override
    public void setVdcModelUID(String vdcModelUID) {
        this.vdcModelUID = vdcModelUID;
    }

    @Override
    public String getVdcHardwareGuid() {
        return vdcHardwareGuid;
    }

    @Override
    public void setVdcHardwareGuid(String vdcHardwareGuid) {
        this.vdcHardwareGuid = vdcHardwareGuid;
    }

    @Override
    public String getVdcHardwareModelGuid() {
        return vdcHardwareModelGuid;
    }

    @Override
    public void setVdcHardwareModelGuid(String vdcHardwareModelGuid) {
        this.vdcHardwareModelGuid = vdcHardwareModelGuid;
    }

    @Override
    public String getVdcVendorGuid() {
        return vdcVendorGuid;
    }

    @Override
    public void setVdcVendorGuid(String vdcVendorGuid) {
        this.vdcVendorGuid = vdcVendorGuid;
    }

    @Override
    public String getVdcOemGuid() {
        return vdcOemGuid;
    }

    @Override
    public void setVdcOemGuid(String vdcOemGuid) {
        this.vdcOemGuid = vdcOemGuid;
    }

    @Override
    public Boolean getIgnoreActionsFromNewDevices() {
        return ignoreActionsFromNewDevices;
    }

    @Override
    public void setIgnoreActionsFromNewDevices(Boolean ignoreActionsFromNewDevices) {
        this.ignoreActionsFromNewDevices = ignoreActionsFromNewDevices;
    }

    @Override
    public void addMeteringValue(CachedMeteringValue cachedMeteringValue) {
        if (cachedMeteringValue != null) {
            switch (cachedMeteringValue.getMeteringType()) {
                case CONSUMPTION:
                    if (checkNewer(consumption, cachedMeteringValue)) {
                        consumption = cachedMeteringValue;
                        informListener(cachedMeteringValue);
                    }
                    break;
                case ENERGY:
                    if (cachedMeteringValue.getMeteringUnit().equals(MeteringUnitsEnum.WH)) {
                        if (checkNewer(energyWh, cachedMeteringValue)) {
                            energyWh = cachedMeteringValue;
                            informListener(cachedMeteringValue);
                        }
                    } else {
                        if (checkNewer(energyWs, cachedMeteringValue)) {
                            energyWs = cachedMeteringValue;
                            informListener(cachedMeteringValue);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void informListener(CachedMeteringValue newMeteringValue) {
        if (isListenerRegisterd()) {
            super.listener.onDeviceStateChanged(
                    new DeviceStateUpdateImpl(DeviceStateUpdate.UPDATE_CIRCUIT_METER, newMeteringValue));
        }
    }

    private boolean checkNewer(CachedMeteringValue oldCachedMeteringValue, CachedMeteringValue newCachedMeteringValue) {
        return oldCachedMeteringValue == null
                || oldCachedMeteringValue.getDateAsDate().before(newCachedMeteringValue.getDateAsDate());
    }

    @Override
    public double getMeteringValue(MeteringTypeEnum meteringType, MeteringUnitsEnum meteringUnit) {
        switch (meteringType) {
            case CONSUMPTION:
                return getValue(consumption);
            case ENERGY:
                if (MeteringUnitsEnum.WS.equals(meteringUnit)) {
                    return getValue(energyWs);
                } else {
                    return getValue(energyWh);
                }
            default:
                break;
        }
        return -1;
    }

    private double getValue(CachedMeteringValue cachedMeteringValue) {
        return cachedMeteringValue != null ? cachedMeteringValue.getValue() : -1;
    }

    @Override
    public List<CachedMeteringValue> getAllCachedMeteringValues() {
        return Arrays.asList(consumption, energyWh, energyWs);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((apiVersion == null) ? 0 : apiVersion.hashCode());
        result = prime * result + ((armSwVersion == null) ? 0 : armSwVersion.hashCode());
        result = prime * result + ((busMemberType == null) ? 0 : busMemberType.hashCode());
        result = prime * result + ((dspSwVersion == null) ? 0 : dspSwVersion.hashCode());
        result = prime * result + ((hasDevices == null) ? 0 : hasDevices.hashCode());
        result = prime * result + ((hasMetering == null) ? 0 : hasMetering.hashCode());
        result = prime * result + ((hwName == null) ? 0 : hwName.hashCode());
        result = prime * result + ((hwVersion == null) ? 0 : hwVersion.hashCode());
        result = prime * result + ((hwVersionString == null) ? 0 : hwVersionString.hashCode());
        result = prime * result + ((ignoreActionsFromNewDevices == null) ? 0 : ignoreActionsFromNewDevices.hashCode());
        result = prime * result + ((swVersion == null) ? 0 : swVersion.hashCode());
        result = prime * result + ((vdcConfigURL == null) ? 0 : vdcConfigURL.hashCode());
        result = prime * result + ((vdcHardwareGuid == null) ? 0 : vdcHardwareGuid.hashCode());
        result = prime * result + ((vdcHardwareModelGuid == null) ? 0 : vdcHardwareModelGuid.hashCode());
        result = prime * result + ((vdcModelUID == null) ? 0 : vdcModelUID.hashCode());
        result = prime * result + ((vdcOemGuid == null) ? 0 : vdcOemGuid.hashCode());
        result = prime * result + ((vdcVendorGuid == null) ? 0 : vdcVendorGuid.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof CircuitImpl)) {
            return false;
        }
        CircuitImpl other = (CircuitImpl) obj;
        if (apiVersion == null) {
            if (other.apiVersion != null) {
                return false;
            }
        } else if (!apiVersion.equals(other.apiVersion)) {
            return false;
        }
        if (armSwVersion == null) {
            if (other.armSwVersion != null) {
                return false;
            }
        } else if (!armSwVersion.equals(other.armSwVersion)) {
            return false;
        }
        if (busMemberType == null) {
            if (other.busMemberType != null) {
                return false;
            }
        } else if (!busMemberType.equals(other.busMemberType)) {
            return false;
        }
        if (dspSwVersion == null) {
            if (other.dspSwVersion != null) {
                return false;
            }
        } else if (!dspSwVersion.equals(other.dspSwVersion)) {
            return false;
        }
        if (hasDevices == null) {
            if (other.hasDevices != null) {
                return false;
            }
        } else if (!hasDevices.equals(other.hasDevices)) {
            return false;
        }
        if (hasMetering == null) {
            if (other.hasMetering != null) {
                return false;
            }
        } else if (!hasMetering.equals(other.hasMetering)) {
            return false;
        }
        if (hwName == null) {
            if (other.hwName != null) {
                return false;
            }
        } else if (!hwName.equals(other.hwName)) {
            return false;
        }
        if (hwVersion == null) {
            if (other.hwVersion != null) {
                return false;
            }
        } else if (!hwVersion.equals(other.hwVersion)) {
            return false;
        }
        if (hwVersionString == null) {
            if (other.hwVersionString != null) {
                return false;
            }
        } else if (!hwVersionString.equals(other.hwVersionString)) {
            return false;
        }
        if (ignoreActionsFromNewDevices == null) {
            if (other.ignoreActionsFromNewDevices != null) {
                return false;
            }
        } else if (!ignoreActionsFromNewDevices.equals(other.ignoreActionsFromNewDevices)) {
            return false;
        }
        if (swVersion == null) {
            if (other.swVersion != null) {
                return false;
            }
        } else if (!swVersion.equals(other.swVersion)) {
            return false;
        }
        if (vdcConfigURL == null) {
            if (other.vdcConfigURL != null) {
                return false;
            }
        } else if (!vdcConfigURL.equals(other.vdcConfigURL)) {
            return false;
        }
        if (vdcHardwareGuid == null) {
            if (other.vdcHardwareGuid != null) {
                return false;
            }
        } else if (!vdcHardwareGuid.equals(other.vdcHardwareGuid)) {
            return false;
        }
        if (vdcHardwareModelGuid == null) {
            if (other.vdcHardwareModelGuid != null) {
                return false;
            }
        } else if (!vdcHardwareModelGuid.equals(other.vdcHardwareModelGuid)) {
            return false;
        }
        if (vdcModelUID == null) {
            if (other.vdcModelUID != null) {
                return false;
            }
        } else if (!vdcModelUID.equals(other.vdcModelUID)) {
            return false;
        }
        if (vdcOemGuid == null) {
            if (other.vdcOemGuid != null) {
                return false;
            }
        } else if (!vdcOemGuid.equals(other.vdcOemGuid)) {
            return false;
        }
        if (vdcVendorGuid == null) {
            if (other.vdcVendorGuid != null) {
                return false;
            }
        } else if (!vdcVendorGuid.equals(other.vdcVendorGuid)) {
            return false;
        }
        return true;
    }
}

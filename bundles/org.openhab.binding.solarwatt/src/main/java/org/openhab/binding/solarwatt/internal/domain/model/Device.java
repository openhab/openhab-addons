/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain.model;

import static org.openhab.binding.solarwatt.internal.SolarwattBindingConstants.CHANNEL_STATE_DEVICE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.binding.solarwatt.internal.domain.SolarwattTag;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * Base class for all devices which are connected to the energy manager.
 *
 * This fields have been identified to exist:
 * com.kiwigrid.lib.device.Device=[
 * IdFingerPrint,
 * IdInterfaceList,
 * IdName,
 * IdFirmware,
 * PasswordLock,
 * IdLabelSet,
 * StateDevice,
 * StateVisibleIsSet,
 * IdFingerPrintVersion,
 * IdDriver,
 * IdModelCode,
 * StateLockedIsSet,
 * IdManufacturer,
 * IdSerialNumber,
 * StateErrorList
 * ]
 * 
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class Device {

    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.lib.device.Device";

    private final String guid;
    private @Nullable String idName;
    private @Nullable String idFirmware;
    private @Nullable String idManufacturer;

    protected final Map<String, State> stateValues;
    protected final Map<String, SolarwattChannel> solarwattChannelSet;

    public Device(DeviceDTO deviceDTO) {
        this.stateValues = new HashMap<>();
        this.solarwattChannelSet = new HashMap<>();
        this.guid = deviceDTO.getGuid();

        this.update(deviceDTO);
    }

    public void update(DeviceDTO deviceDTO) {
        this.idName = deviceDTO.getStringTag("IdName");
        this.idFirmware = deviceDTO.getStringTag("IdFirmware");
        this.idManufacturer = deviceDTO.getStringTag("IdManufacturer");

        this.addStringState(CHANNEL_STATE_DEVICE, deviceDTO);
    }

    public Map<String, State> getStateValues() {
        return this.stateValues;
    }

    public Map<String, SolarwattChannel> getSolarwattChannelSet() {
        return this.solarwattChannelSet;
    }

    protected void addWattHourQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addWattHourQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addWattHourQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.stateValues.put(solarwattTag.getChannelName(),
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), Units.WATT_HOUR)),
                        solarwattTag.getTagName()));
        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), Units.WATT_HOUR, "energy", advanced));
        }
    }

    protected void addWattQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addWattQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addWattQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.stateValues.put(solarwattTag.getChannelName(),
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), Units.WATT)),
                        solarwattTag.getTagName()));
        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), Units.WATT, "energy", advanced));
        }
    }

    protected void addSecondsQuantity(String channelName, String tagName, String path, DeviceDTO deviceDTO) {
        this.stateValues.put(channelName,
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigInteger(), Units.SECOND)),
                        channelName, tagName, path));
        if (!this.solarwattChannelSet.containsKey(channelName)) {
            this.solarwattChannelSet.put(channelName, new SolarwattChannel(channelName, Units.SECOND, "time"));
        }
    }

    protected void addPercentQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.stateValues.put(solarwattTag.getChannelName(),
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), Units.PERCENT)),
                        solarwattTag.getTagName()));

        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), Units.PERCENT, "status"));
        }
    }

    protected void addCelsiusQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.stateValues.put(solarwattTag.getChannelName(),
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), SIUnits.CELSIUS)),
                        solarwattTag.getTagName()));

        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), SIUnits.CELSIUS, "temperature"));
        }
    }

    protected void addAmpereQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addAmpereQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addAmpereQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.stateValues.put(solarwattTag.getChannelName(),
                deviceDTO.getState((jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), Units.AMPERE)),
                        solarwattTag.getTagName()));

        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), Units.AMPERE, "current", advanced));
        }
    }

    protected void addStringState(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.stateValues.put(solarwattTag.getChannelName(), deviceDTO
                .getState((jsonElement -> new StringType(jsonElement.getAsString())), solarwattTag.getTagName()));

        if (!this.solarwattChannelSet.containsKey(solarwattTag.getChannelName())) {
            this.solarwattChannelSet.put(solarwattTag.getChannelName(),
                    new SolarwattChannel(solarwattTag.getChannelName(), "status"));
        }
    }

    public String getGuid() {
        return this.guid;
    }

    public @Nullable String getIdName() {
        return this.idName;
    }

    public @Nullable String getIdFirmware() {
        return this.idFirmware;
    }

    public @Nullable String getIdManufacturer() {
        return this.idManufacturer;
    }

    protected String getSolarWattLabel() {
        return "Device";
    }

    public String getLabel() {
        return this.getSolarWattLabel() + " " + this.getIdName();
    }
}

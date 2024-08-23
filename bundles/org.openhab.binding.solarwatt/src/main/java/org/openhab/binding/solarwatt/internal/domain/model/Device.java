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
package org.openhab.binding.solarwatt.internal.domain.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarwatt.internal.domain.SolarwattChannel;
import org.openhab.binding.solarwatt.internal.domain.SolarwattTag;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
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
    public static final String WATT_HOUR_CATEGORY = "energy";
    public static final String WATT_CATEGORY = "energy";
    private final String guid;
    private @Nullable String idName;
    private @Nullable String idFirmware;
    private @Nullable String idManufacturer;
    private ThingStatus stateDevice = ThingStatus.UNINITIALIZED;
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
        if ("OK".equals(deviceDTO.getStringTag("StateDevice"))) {
            this.stateDevice = ThingStatus.ONLINE;
        } else {
            this.stateDevice = ThingStatus.OFFLINE;
        }
    }

    public BigDecimal getBigDecimalFromChannel(String channelName) {
        State state = this.getStateValues().get(channelName);
        if (state != null) {
            @SuppressWarnings("rawtypes")
            QuantityType quantity = state.as(QuantityType.class);
            if (quantity != null) {
                return quantity.toBigDecimal();
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Add a channeltype to the known channel types.
     *
     * {@link org.openhab.core.thing.type.ChannelType} is only created if it does noct exist.
     *
     * @param tagName name for the channel
     * @param unit unit for channel
     * @param category text category
     * @param advanced wether or not to display only in advanced
     */
    public void addChannel(String tagName, @Nullable Unit<?> unit, String category, Boolean advanced) {
        this.solarwattChannelSet.computeIfAbsent(tagName, s -> new SolarwattChannel(tagName, unit, category, advanced));
    }

    /**
     * Add a state with unit and BigInteger as value.
     *
     * @param solarwattTag combined tag and channel name
     * @param deviceDTO raw device data
     * @param unit unit for value
     */
    public void addStateBigInteger(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Unit<?> unit) {
        this.addState(solarwattTag.getChannelName(), deviceDTO.getState(
                (jsonElement -> new QuantityType<>(jsonElement.getAsBigInteger(), unit)), solarwattTag.getTagName()));
    }

    /**
     * Add a state from a json path with unit and BigInteger as value.
     *
     * @param channelName target channe
     * @param tagName tag for value
     * @param path to find value
     * @param deviceDTO raw device data
     * @param unit unit for value
     */
    public void addStateBigInteger(String channelName, String tagName, String path, DeviceDTO deviceDTO, Unit<?> unit) {
        this.addState(channelName, deviceDTO.getState(
                (jsonElement -> new QuantityType<>(jsonElement.getAsBigInteger(), unit)), channelName, tagName, path));
    }

    /**
     * Add a state with unit and BigDecimal as value.
     *
     * @param solarwattTag combined tag and channel name
     * @param deviceDTO raw device data
     * @param unit unit for value
     */
    public void addStateBigDecimal(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Unit<?> unit) {
        this.addState(solarwattTag.getChannelName(), deviceDTO.getState(
                (jsonElement -> new QuantityType<>(jsonElement.getAsBigDecimal(), unit)), solarwattTag.getTagName()));
    }

    /**
     * Add a state with unit and BigDecimal as value.
     *
     * @param solarwattTag combined tag and channel name
     * @param value BigDecimal value
     * @param unit unit for value
     */
    public void addStateBigDecimal(SolarwattTag solarwattTag, BigDecimal value, Unit<?> unit) {
        this.addState(solarwattTag.getChannelName(), new QuantityType<>(value, unit));
    }

    /**
     * Add a string state.
     *
     * @param solarwattTag combined tag and channel name
     * @param deviceDTO raw device data
     */
    public void addStateString(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addState(solarwattTag.getChannelName(), deviceDTO
                .getState((jsonElement -> new StringType(jsonElement.getAsString())), solarwattTag.getTagName()));
    }

    public void addStateSwitch(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addState(solarwattTag.getChannelName(), deviceDTO
                .getState((jsonElement -> OnOffType.from(jsonElement.getAsString())), solarwattTag.getTagName()));
    }

    public ThingStatus getStateDevice() {
        return this.stateDevice;
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
        this.addChannel(solarwattTag.getChannelName(), Units.WATT_HOUR, WATT_HOUR_CATEGORY, advanced);

        this.addStateBigDecimal(solarwattTag, deviceDTO, Units.WATT_HOUR);
    }

    protected void addWattQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addWattQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addWattQuantity(SolarwattTag solarwattTag, BigDecimal value, Boolean advanced) {
        this.addChannel(solarwattTag.getChannelName(), Units.WATT, WATT_CATEGORY, advanced);

        this.addStateBigDecimal(solarwattTag, value, Units.WATT);
    }

    protected void addWattQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.addChannel(solarwattTag.getChannelName(), Units.WATT, WATT_CATEGORY, advanced);

        this.addStateBigDecimal(solarwattTag, deviceDTO, Units.WATT);
    }

    protected void addSecondsQuantity(String channelName, String tagName, String path, DeviceDTO deviceDTO) {
        this.addChannel(channelName, Units.SECOND, "time", false);

        this.addStateBigInteger(channelName, tagName, path, deviceDTO, Units.SECOND);
    }

    protected void addPercentQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addChannel(solarwattTag.getChannelName(), Units.PERCENT, "status", false);

        this.addStateBigDecimal(solarwattTag, deviceDTO, Units.PERCENT);
    }

    protected void addCelsiusQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addChannel(solarwattTag.getChannelName(), SIUnits.CELSIUS, "temperature", false);

        this.addStateBigDecimal(solarwattTag, deviceDTO, SIUnits.CELSIUS);
    }

    protected void addAmpereQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addAmpereQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addAmpereQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.addChannel(solarwattTag.getChannelName(), Units.AMPERE, "current", advanced);

        this.addStateBigDecimal(solarwattTag, deviceDTO, Units.AMPERE);
    }

    protected void addVoltageQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addVoltageQuantity(solarwattTag, deviceDTO, false);
    }

    protected void addVoltageQuantity(SolarwattTag solarwattTag, DeviceDTO deviceDTO, Boolean advanced) {
        this.addChannel(solarwattTag.getChannelName(), Units.VOLT, "voltage", advanced);

        this.addStateBigDecimal(solarwattTag, deviceDTO, Units.VOLT);
    }

    protected void addStringState(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addChannel(solarwattTag.getChannelName(), null, "status", false);

        this.addStateString(solarwattTag, deviceDTO);
    }

    protected void addSwitchState(SolarwattTag solarwattTag, DeviceDTO deviceDTO) {
        this.addChannel(solarwattTag.getChannelName(), null, "switch", false);

        this.addStateSwitch(solarwattTag, deviceDTO);
    }

    /**
     * Add state to map and return it for further usage.
     *
     * @param channelName where to put the state
     * @param state to put
     */
    public void addState(String channelName, @Nullable State state) {
        if (state != null) {
            this.stateValues.put(channelName, state);
        }
    }

    /**
     * Get state from map
     * 
     * @param channelName state to return
     * @return {@link State} found
     */
    public @Nullable State getState(String channelName) {
        return this.stateValues.get(channelName);
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

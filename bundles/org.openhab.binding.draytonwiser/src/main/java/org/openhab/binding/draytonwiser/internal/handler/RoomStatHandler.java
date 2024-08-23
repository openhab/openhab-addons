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
package org.openhab.binding.draytonwiser.internal.handler;

import static org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.BatteryLevel;
import org.openhab.binding.draytonwiser.internal.DraytonWiserBindingConstants.SignalStrength;
import org.openhab.binding.draytonwiser.internal.api.DraytonWiserApiException;
import org.openhab.binding.draytonwiser.internal.handler.RoomStatHandler.RoomStatData;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.RoomStatDTO;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link RoomStatHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class RoomStatHandler extends DraytonWiserThingHandler<RoomStatData> {

    private String serialNumber = "";

    public RoomStatHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        serialNumber = getConfig().get("serialNumber").toString();
    }

    @Override
    protected void handleCommand(final String channelId, final Command command) throws DraytonWiserApiException {
        if (command instanceof OnOffType && CHANNEL_DEVICE_LOCKED.equals(channelId)) {
            setDeviceLocked(OnOffType.ON.equals(command));
        }
    }

    @Override
    protected void refresh() {
        updateState(CHANNEL_CURRENT_TEMPERATURE, this::getTemperature);
        updateState(CHANNEL_CURRENT_HUMIDITY, this::getHumidity);
        updateState(CHANNEL_CURRENT_SETPOINT, this::getSetPoint);
        updateState(CHANNEL_CURRENT_SIGNAL_RSSI, this::getSignalRSSI);
        updateState(CHANNEL_CURRENT_SIGNAL_LQI, this::getSignalLQI);
        updateState(CHANNEL_CURRENT_BATTERY_VOLTAGE, this::getBatteryVoltage);
        updateState(CHANNEL_CURRENT_WISER_SIGNAL_STRENGTH, this::getWiserSignalStrength);
        updateState(CHANNEL_CURRENT_SIGNAL_STRENGTH, this::getSignalStrength);
        updateState(CHANNEL_CURRENT_WISER_BATTERY_LEVEL, this::getWiserBatteryLevel);
        updateState(CHANNEL_CURRENT_BATTERY_LEVEL, this::getBatteryLevel);
        updateState(CHANNEL_ZIGBEE_CONNECTED, this::getZigbeeConnected);
        updateState(CHANNEL_DEVICE_LOCKED, this::getDeviceLocked);
    }

    @Override
    protected @Nullable RoomStatData collectData(final DraytonWiserDTO domainDTOProxy) {
        final RoomStatDTO roomStat = domainDTOProxy.getRoomStat(serialNumber);
        final DeviceDTO device = roomStat == null ? null : domainDTOProxy.getExtendedDeviceProperties(roomStat.getId());

        return roomStat == null || device == null ? null : new RoomStatData(roomStat, device);
    }

    private State getSetPoint() {
        return new QuantityType<>(getData().roomStat.getSetPoint() / 10.0, SIUnits.CELSIUS);
    }

    private State getHumidity() {
        final Integer humidity = getData().roomStat.getMeasuredHumidity();

        return humidity == null ? UnDefType.UNDEF : new QuantityType<>(humidity, Units.PERCENT);
    }

    private State getTemperature() {
        final int fullScaleTemp = getData().roomStat.getMeasuredTemperature();

        return OFFLINE_TEMPERATURE == fullScaleTemp ? UnDefType.UNDEF
                : new QuantityType<>(fullScaleTemp / 10.0, SIUnits.CELSIUS);
    }

    private State getSignalRSSI() {
        final Integer rssi = getData().device.getRssi();
        return rssi == null ? UnDefType.UNDEF : new QuantityType<>(rssi, Units.DECIBEL_MILLIWATTS);
    }

    private State getSignalLQI() {
        final Integer lqi = getData().device.getLqi();
        return lqi == null ? UnDefType.UNDEF : new DecimalType(lqi);
    }

    private State getWiserSignalStrength() {
        return new StringType(getData().device.getDisplayedSignalStrength());
    }

    private State getSignalStrength() {
        return SignalStrength.toSignalStrength(getData().device.getDisplayedSignalStrength());
    }

    private State getBatteryVoltage() {
        final Integer voltage = getData().device.getBatteryVoltage();
        return voltage == null ? UnDefType.UNDEF : new QuantityType<>(voltage / 10.0, Units.VOLT);
    }

    private State getWiserBatteryLevel() {
        return new StringType(getData().device.getBatteryLevel());
    }

    private State getBatteryLevel() {
        return BatteryLevel.toBatteryLevel(getData().device.getBatteryLevel());
    }

    private State getZigbeeConnected() {
        return OnOffType.from(OFFLINE_TEMPERATURE != getData().roomStat.getMeasuredTemperature());
    }

    private State getDeviceLocked() {
        return getData().device.getDeviceLockEnabled() == null ? UnDefType.UNDEF
                : OnOffType.from(getData().device.getDeviceLockEnabled());
    }

    private void setDeviceLocked(final boolean state) throws DraytonWiserApiException {
        getApi().setDeviceLocked(getData().device.getId(), state);
    }

    static class RoomStatData {
        public final RoomStatDTO roomStat;
        public final DeviceDTO device;

        public RoomStatData(final RoomStatDTO roomStat, final DeviceDTO device) {
            this.roomStat = roomStat;
            this.device = device;
        }
    }
}

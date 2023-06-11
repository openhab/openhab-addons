/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.draytonwiser.internal.handler.TRVHandler.SmartValveData;
import org.openhab.binding.draytonwiser.internal.model.DeviceDTO;
import org.openhab.binding.draytonwiser.internal.model.DraytonWiserDTO;
import org.openhab.binding.draytonwiser.internal.model.SmartValveDTO;
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
 * The {@link TRVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 * @author Hilbrand Bouwkamp - Simplified handler to handle null data
 */
@NonNullByDefault
public class TRVHandler extends DraytonWiserThingHandler<SmartValveData> {

    private String serialNumber = "";

    public TRVHandler(final Thing thing) {
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
        updateState(CHANNEL_CURRENT_DEMAND, this::getDemand);
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
    protected @Nullable SmartValveData collectData(final DraytonWiserDTO domainDTOProxy) {
        final SmartValveDTO smartValve = domainDTOProxy.getSmartValve(serialNumber);
        final DeviceDTO device = smartValve == null ? null
                : domainDTOProxy.getExtendedDeviceProperties(smartValve.getId());

        return smartValve == null || device == null ? null : new SmartValveData(smartValve, device);
    }

    private State getSetPoint() {
        return new QuantityType<>(getData().smartValve.getSetPoint() / 10.0, SIUnits.CELSIUS);
    }

    private State getDemand() {
        final Integer demand = getData().smartValve.getPercentageDemand();
        return demand == null ? UnDefType.UNDEF : new QuantityType<>(demand, Units.PERCENT);
    }

    private State getTemperature() {
        final int fullScaleTemp = getData().smartValve.getMeasuredTemperature();

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
        return OnOffType.from(getData().smartValve.getMeasuredTemperature() != OFFLINE_TEMPERATURE);
    }

    private State getDeviceLocked() {
        final Boolean locked = getData().device.getDeviceLockEnabled();

        return locked == null ? UnDefType.UNDEF : OnOffType.from(locked);
    }

    private void setDeviceLocked(final Boolean state) throws DraytonWiserApiException {
        getApi().setDeviceLocked(getData().device.getId(), state);
    }

    static class SmartValveData {
        public final SmartValveDTO smartValve;
        public final DeviceDTO device;

        public SmartValveData(final SmartValveDTO smartValve, final DeviceDTO device) {
            this.smartValve = smartValve;
            this.device = device;
        }
    }
}

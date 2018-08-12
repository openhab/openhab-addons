/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.draytonwiser.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.draytonwiser.DraytonWiserBindingConstants;
import org.openhab.binding.draytonwiser.internal.config.Device;
import org.openhab.binding.draytonwiser.internal.config.SmartValve;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TRVHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class TRVHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TRVHandler.class);

    @Nullable
    private SmartValve smartValve;

    public TRVHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_DEVICE_LOCKED)) {
            boolean deviceLocked = command.toString().toUpperCase().equals("ON");
            setDeviceLocked(deviceLocked);
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean smartValveUpdated = updateSmartValveData();
            if (smartValveUpdated) {
                updateStatus(ThingStatus.ONLINE);
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_TEMPERATURE),
                        getTemperature());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_DEMAND),
                        getDemand());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SETPOINT),
                        getSetPoint());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_RSSI),
                        getSignalRSSI());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_LQI),
                        getSignalLQI());
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_CURRENT_BATTERY_VOLTAGE), getBatteryVoltage());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_CURRENT_WISER_SIGNAL_STRENGTH),
                        getWiserSignalStrength());
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_STRENGTH), getSignalStrength());
                updateState(
                        new ChannelUID(getThing().getUID(),
                                DraytonWiserBindingConstants.CHANNEL_CURRENT_WISER_BATTERY_LEVEL),
                        getWiserBatteryLevel());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_BATTERY_LEVEL),
                        getBatteryLevel());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ZIGBEE_CONNECTED),
                        getZigbeeConnected());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_DEVICE_LOCKED),
                        getDeviceLocked());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateSmartValveData() {
        if (bridgeHandler == null) {
            return false;
        }
        smartValve = bridgeHandler.getSmartValve(getThing().getConfiguration().get("serialNumber").toString());
        return smartValve != null;
    }

    private State getSetPoint() {
        if (smartValve != null) {
            return new QuantityType<>((float) smartValve.getSetPoint() / 10, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }

    private State getDemand() {
        if (smartValve != null) {
            return new DecimalType(smartValve.getPercentageDemand());
        }
        return UnDefType.UNDEF;
    }

    private State getTemperature() {
        if (smartValve != null) {
            Integer fullScaleTemp = smartValve.getMeasuredTemperature();
            if (fullScaleTemp.equals(DraytonWiserBindingConstants.OFFLINE_TEMPERATURE)) {
                return UnDefType.UNDEF;
            }
            return new QuantityType<>((float) fullScaleTemp / 10, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }

    private State getSignalRSSI() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getRssi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getSignalLQI() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getLqi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getWiserSignalStrength() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new StringType(device.getDisplayedSignalStrength());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getSignalStrength() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType(DraytonWiserBindingConstants.SIGNAL_STRENGTH_MAP
                        .getOrDefault(device.getDisplayedSignalStrength(), 0));
            }
        }

        return new DecimalType(0);
    }

    private State getBatteryVoltage() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getBatteryVoltage() / 10);
            }
        }

        return UnDefType.UNDEF;
    }

    private State getWiserBatteryLevel() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new StringType(device.getBatteryLevel());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getBatteryLevel() {
        if (smartValve != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType(
                        DraytonWiserBindingConstants.BATTERY_LEVEL_MAP.getOrDefault(device.getBatteryLevel(), 0));
            }
        }

        return UnDefType.UNDEF;
    }

    private State getZigbeeConnected() {
        if (smartValve != null) {
            Integer fullScaleTemp = smartValve.getMeasuredTemperature();
            if (fullScaleTemp.equals(DraytonWiserBindingConstants.OFFLINE_TEMPERATURE)) {
                return OnOffType.OFF;
            }
            return OnOffType.ON;
        }

        return OnOffType.OFF;
    }

    private State getDeviceLocked() {
        if (smartValve != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                Boolean locked = device.getDeviceLockEnabled();
                if (locked != null) {
                    return locked ? OnOffType.ON : OnOffType.OFF;
                }
            }
        }

        return OnOffType.OFF;
    }

    private void setDeviceLocked(Boolean state) {
        if (bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                bridgeHandler.setDeviceLocked(device.getId(), state);
            }
        }
    }
}

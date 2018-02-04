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
import org.eclipse.smarthome.core.library.types.StringType;
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
        // if (channelUID.getId().equals(CHANNEL_1)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    protected void refresh() {
        try {
            boolean smartValveUpdated = updateSmartValveData();
            if (smartValveUpdated) {
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
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_STRENGTH), getSignalStrength());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_BATTERY_LEVEL),
                        getBatteryLevel());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateSmartValveData() {
        smartValve = getBridgeHandler().getSmartValve(getThing().getConfiguration().get("serialNumber").toString());
        return smartValve != null;
    }

    private State getSetPoint() {
        if (smartValve != null) {
            return new DecimalType((float) smartValve.getSetPoint() / 10);
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
            return new DecimalType((float) smartValve.getMeasuredTemperature() / 10);
        }

        return UnDefType.UNDEF;
    }

    private State getSignalRSSI() {
        if (smartValve != null) {
            Device device = getBridgeHandler().getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getRssi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getSignalLQI() {
        if (smartValve != null) {
            Device device = getBridgeHandler().getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getLqi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getSignalStrength() {
        if (smartValve != null) {
            Device device = getBridgeHandler().getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new StringType(device.getDisplayedSignalStrength());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getBatteryVoltage() {
        if (smartValve != null) {
            Device device = getBridgeHandler().getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new DecimalType((float) device.getBatteryVoltage() / 10);
            }
        }

        return UnDefType.UNDEF;
    }

    private State getBatteryLevel() {
        if (smartValve != null) {
            Device device = getBridgeHandler().getExtendedDeviceProperties(smartValve.getId());
            if (device != null) {
                return new StringType(device.getBatteryLevel());
            }
        }

        return UnDefType.UNDEF;
    }
}

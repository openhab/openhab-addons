/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.draytonwiser.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.openhab.binding.draytonwiser.internal.config.Schedule;
import org.openhab.binding.draytonwiser.internal.config.SmartPlug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link SmartPlugHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Schofield - Initial contribution
 */
@NonNullByDefault
public class SmartPlugHandler extends DraytonWiserThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartPlugHandler.class);
    private Gson gson;

    @Nullable
    private SmartPlug smartPlug;

    public SmartPlugHandler(Thing thing) {
        super(thing);
        gson = new Gson();
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

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_SMARTPLUG_OUTPUT_STATE)) {
            boolean outputState = command.toString().toUpperCase().equals("ON");
            setOutputState(outputState);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_SMARTPLUG_AWAY_ACTION)) {
            boolean awayAction = command.toString().toUpperCase().equals("ON");
            setAwayAction(awayAction);
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_ROOM_MASTER_SCHEDULE)) {
            setMasterScheduleState(command.toString());
        }

        if (channelUID.getId().equals(DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE)) {
            boolean manualMode = command.toString().toUpperCase().equals("ON");
            setManualMode(manualMode);
        }
    }

    @Override
    protected void refresh() {
        try {
            boolean smartPlugUpdated = updateSmartPlugData();
            if (smartPlugUpdated) {
                updateStatus(ThingStatus.ONLINE);
                updateState(new ChannelUID(getThing().getUID(),
                        DraytonWiserBindingConstants.CHANNEL_SMARTPLUG_OUTPUT_STATE), getOutputState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_SMARTPLUG_AWAY_ACTION),
                        getAwayAction());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_RSSI),
                        getSignalRSSI());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_CURRENT_SIGNAL_LQI),
                        getSignalLQI());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ZIGBEE_CONNECTED),
                        getZigbeeConnected());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_DEVICE_LOCKED),
                        getDeviceLocked());
                updateState(new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_MANUAL_MODE_STATE),
                        getManualModeState());
                updateState(
                        new ChannelUID(getThing().getUID(), DraytonWiserBindingConstants.CHANNEL_ROOM_MASTER_SCHEDULE),
                        getMasterSchedule());
            }
        } catch (Exception e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private boolean updateSmartPlugData() {
        if (bridgeHandler == null) {
            return false;
        }
        smartPlug = bridgeHandler.getSmartPlug(getThing().getConfiguration().get("serialNumber").toString());
        return smartPlug != null;
    }

    private State getAwayAction() {
        if (smartPlug != null) {
            return smartPlug.getAwayAction().toLowerCase().equals("off") ? OnOffType.ON : OnOffType.OFF;
        }
        return UnDefType.UNDEF;
    }

    private State getOutputState() {
        if (smartPlug != null) {
            String outputState = smartPlug.getOutputState();
            if (outputState != null) {
                return outputState.toLowerCase().equals("on") ? OnOffType.ON : OnOffType.OFF;
            }
        }
        return OnOffType.OFF;
    }

    private State getSignalRSSI() {
        if (smartPlug != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartPlug.getId());
            if (device != null) {
                return new DecimalType((float) device.getRssi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getSignalLQI() {
        if (smartPlug != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartPlug.getId());
            if (device != null) {
                return new DecimalType((float) device.getLqi());
            }
        }

        return UnDefType.UNDEF;
    }

    private State getZigbeeConnected() {
        if (smartPlug != null && bridgeHandler != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartPlug.getId());
            if (device != null) {
                return device.getLqi() == null ? OnOffType.OFF : OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private State getDeviceLocked() {
        if (smartPlug != null) {
            Device device = bridgeHandler.getExtendedDeviceProperties(smartPlug.getId());
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
            Device device = bridgeHandler.getExtendedDeviceProperties(smartPlug.getId());
            if (device != null) {
                bridgeHandler.setDeviceLocked(device.getId(), state);
            }
        }
    }

    private void setMasterScheduleState(String scheduleJSON) {
        if (bridgeHandler != null && smartPlug != null) {
            bridgeHandler.setSmartPlugSchedule(smartPlug.getId(), scheduleJSON);
        }
    }

    private State getMasterSchedule() {
        if (smartPlug != null && bridgeHandler != null) {
            Integer scheduleId = smartPlug.getScheduleId();
            if (scheduleId != null) {
                return new StringType(gson.toJson(bridgeHandler.getSchedule(scheduleId), Schedule.class));
            }
        }

        return new StringType();
    }

    private State getManualModeState() {
        if (smartPlug != null) {
            if (smartPlug.getMode().toUpperCase().equals("MANUAL")) {
                return OnOffType.ON;
            }
        }

        return OnOffType.OFF;
    }

    private void setManualMode(Boolean manualMode) {
        if (bridgeHandler != null && smartPlug != null) {
            bridgeHandler.setSmartPlugManualMode(smartPlug.getId(), manualMode);
        }
    }

    private void setOutputState(Boolean outputState) {
        if (bridgeHandler != null && smartPlug != null) {
            bridgeHandler.setSmartPlugOutputState(smartPlug.getId(), outputState);
        }
    }

    private void setAwayAction(Boolean awayAction) {
        if (bridgeHandler != null && smartPlug != null) {
            bridgeHandler.setSmartPlugAwayAction(smartPlug.getId(), awayAction);
        }
    }
}

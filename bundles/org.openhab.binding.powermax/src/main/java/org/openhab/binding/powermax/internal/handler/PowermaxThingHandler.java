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
package org.openhab.binding.powermax.internal.handler;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.powermax.internal.config.PowermaxX10Configuration;
import org.openhab.binding.powermax.internal.config.PowermaxZoneConfiguration;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxX10Settings;
import org.openhab.binding.powermax.internal.state.PowermaxZoneSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowermaxThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxThingHandler extends BaseThingHandler implements PowermaxPanelSettingsListener {

    private final Logger logger = LoggerFactory.getLogger(PowermaxThingHandler.class);

    private static final int ZONE_NR_MIN = 1;
    private static final int ZONE_NR_MAX = 64;
    private static final int X10_NR_MIN = 1;
    private static final int X10_NR_MAX = 16;

    private PowermaxBridgeHandler bridgeHandler;

    public PowermaxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());

        boolean validConfig = false;
        String errorMsg = "Unexpected thing type " + getThing().getThingTypeUID();

        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            PowermaxZoneConfiguration config = getConfigAs(PowermaxZoneConfiguration.class);
            if (config.zoneNumber != null && config.zoneNumber >= ZONE_NR_MIN && config.zoneNumber <= ZONE_NR_MAX) {
                validConfig = true;
            } else {
                errorMsg = "zoneNumber setting must be defined in thing configuration and set between " + ZONE_NR_MIN
                        + " and " + ZONE_NR_MAX;
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
            PowermaxX10Configuration config = getConfigAs(PowermaxX10Configuration.class);
            if (config.deviceNumber != null && config.deviceNumber >= X10_NR_MIN && config.deviceNumber <= X10_NR_MAX) {
                validConfig = true;
            } else {
                errorMsg = "deviceNumber setting must be defined in thing configuration and set between " + X10_NR_MIN
                        + " and " + X10_NR_MAX;
            }
        }

        if (validConfig) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                initializeThingState(null, null);
            } else {
                initializeThingState(bridge.getHandler(), bridge.getStatus());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for thing {}", bridgeStatusInfo, getThing().getUID());
        initializeThingState((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeThingState(ThingHandler bridgeHandler, ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.UNKNOWN);
                logger.debug("Set handler status to UNKNOWN for thing {} (bridge ONLINE)", getThing().getUID());
                this.bridgeHandler = (PowermaxBridgeHandler) bridgeHandler;
                this.bridgeHandler.registerPanelSettingsListener(this);
                onPanelSettingsUpdated(this.bridgeHandler.getPanelSettings());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                logger.debug("Set handler status to OFFLINE for thing {} (bridge OFFLINE)", getThing().getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            logger.debug("Set handler status to OFFLINE for thing {}", getThing().getUID());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());
        if (bridgeHandler != null) {
            bridgeHandler.unregisterPanelSettingsListener(this);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} from channel {}", command, channelUID.getId());

        if (bridgeHandler == null) {
            return;
        } else if (command instanceof RefreshType) {
            updateChannelFromAlarmState(channelUID.getId(), bridgeHandler.getCurrentState());
        } else {
            switch (channelUID.getId()) {
                case BYPASSED:
                    if (command instanceof OnOffType) {
                        bridgeHandler.zoneBypassed(getConfigAs(PowermaxZoneConfiguration.class).zoneNumber.byteValue(),
                                command.equals(OnOffType.ON));
                    } else {
                        logger.debug("Command of type {} while OnOffType is expected. Command is ignored.",
                                command.getClass().getSimpleName());
                    }
                    break;
                case X10_STATUS:
                    bridgeHandler.x10Command(getConfigAs(PowermaxX10Configuration.class).deviceNumber.byteValue(),
                            command);
                    break;
                default:
                    logger.debug("No available command for channel {}. Command is ignored.", channelUID.getId());
                    break;
            }
        }
    }

    /**
     * Update channel to match a new alarm system state
     *
     * @param channel: the channel
     * @param state: the alarm system state
     */
    public void updateChannelFromAlarmState(String channel, PowermaxState state) {
        if (state == null || channel == null || !isLinked(channel)) {
            return;
        }

        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            int num = getConfigAs(PowermaxZoneConfiguration.class).zoneNumber.intValue();
            if (channel.equals(TRIPPED) && (state.isSensorTripped(num) != null)) {
                updateState(TRIPPED, state.isSensorTripped(num) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            } else if (channel.equals(LAST_TRIP) && (state.getSensorLastTripped(num) != null)) {
                ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(state.getSensorLastTripped(num)),
                        TimeZone.getDefault().toZoneId());
                updateState(LAST_TRIP, new DateTimeType(zoned));
            } else if (channel.equals(BYPASSED) && (state.isSensorBypassed(num) != null)) {
                updateState(BYPASSED, state.isSensorBypassed(num) ? OnOffType.ON : OnOffType.OFF);
            } else if (channel.equals(ARMED) && (state.isSensorArmed(num) != null)) {
                updateState(ARMED, state.isSensorArmed(num) ? OnOffType.ON : OnOffType.OFF);
            } else if (channel.equals(LOW_BATTERY) && (state.isSensorLowBattery(num) != null)) {
                updateState(LOW_BATTERY, state.isSensorLowBattery(num) ? OnOffType.ON : OnOffType.OFF);
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
            int num = getConfigAs(PowermaxX10Configuration.class).deviceNumber.intValue();
            if (channel.equals(X10_STATUS) && (state.getPGMX10DeviceStatus(num) != null)) {
                updateState(X10_STATUS, state.getPGMX10DeviceStatus(num) ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    @Override
    public void onPanelSettingsUpdated(PowermaxPanelSettings settings) {
        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            PowermaxZoneConfiguration config = getConfigAs(PowermaxZoneConfiguration.class);
            onZoneSettingsUpdated(config.zoneNumber, settings);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
            if (isNotReadyForThingStatusUpdate()) {
                return;
            }

            PowermaxX10Configuration config = getConfigAs(PowermaxX10Configuration.class);
            PowermaxX10Settings deviceSettings = (settings == null) ? null
                    : settings.getX10Settings(config.deviceNumber);
            if (settings == null) {
                if (getThing().getStatus() != ThingStatus.UNKNOWN) {
                    updateStatus(ThingStatus.UNKNOWN);
                    logger.debug("Set handler status to UNKNOWN for thing {}", getThing().getUID());
                }
            } else if (deviceSettings == null || !deviceSettings.isEnabled()) {
                if (getThing().getStatus() != ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Disabled device");
                    logger.debug("Set handler status to OFFLINE for thing {} (X10 device {} disabled)",
                            getThing().getUID(), config.deviceNumber);
                }
            } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Set handler status to ONLINE for thing {} (X10 device {} enabled)", getThing().getUID(),
                        config.deviceNumber);
            }
        }
    }

    @Override
    public void onZoneSettingsUpdated(int zoneNumber, PowermaxPanelSettings settings) {
        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            PowermaxZoneConfiguration config = getConfigAs(PowermaxZoneConfiguration.class);
            if (zoneNumber == config.zoneNumber) {
                if (isNotReadyForThingStatusUpdate()) {
                    return;
                }

                PowermaxZoneSettings zoneSettings = (settings == null) ? null
                        : settings.getZoneSettings(config.zoneNumber);
                if (settings == null) {
                    if (getThing().getStatus() != ThingStatus.UNKNOWN) {
                        updateStatus(ThingStatus.UNKNOWN);
                        logger.debug("Set handler status to UNKNOWN for thing {}", getThing().getUID());
                    }
                } else if (zoneSettings == null) {
                    if (getThing().getStatus() != ThingStatus.OFFLINE) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Zone not paired");
                        logger.debug("Set handler status to OFFLINE for thing {} (zone number {} not paired)",
                                getThing().getUID(), config.zoneNumber);
                    }
                } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Set handler status to ONLINE for thing {} (zone number {} paired)",
                            getThing().getUID(), config.zoneNumber);
                }
            }
        }
    }

    private boolean isNotReadyForThingStatusUpdate() {
        return (getThing().getStatus() == ThingStatus.OFFLINE)
                && ((getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)
                        || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE)
                        || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_UNINITIALIZED));
    }

    public PowermaxZoneConfiguration getZoneConfiguration() {
        return getConfigAs(PowermaxZoneConfiguration.class);
    }

    public PowermaxX10Configuration getX10Configuration() {
        return getConfigAs(PowermaxX10Configuration.class);
    }
}

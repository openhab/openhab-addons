/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.handler;

import static org.openhab.binding.powermax.PowermaxBindingConstants.*;

import java.util.Calendar;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
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
import org.openhab.binding.powermax.internal.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.config.PowermaxX10Configuration;
import org.openhab.binding.powermax.internal.config.PowermaxZoneConfiguration;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
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

    private void initializeThingState(ThingHandler handler, ThingStatus bridgeStatus) {
        if (handler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
                bridgeHandler = (PowermaxBridgeHandler) handler;
                bridgeHandler.registerPanelSettingsListener(this);
                logger.debug("Set handler status to ONLINE for thing {} (bridge ONLINE)", getThing().getUID());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                logger.debug("Set handler status to OFFLINE for thing {} (bridge OFFLINE)", getThing().getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
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
                    }
                    break;
                case X10_STATUS:
                    if (command instanceof StringType) {
                        bridgeHandler.x10Command(getConfigAs(PowermaxX10Configuration.class).deviceNumber.byteValue(),
                                command.toString());
                    }
                    break;
                default:
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
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(state.getSensorLastTripped(num));
                updateState(LAST_TRIP, new DateTimeType(cal));
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
            PowermaxZoneSettings zoneSettings = (settings == null) ? null : settings.getZoneSettings(config.zoneNumber);
            onZoneSettingsUpdated(config.zoneNumber, zoneSettings);
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
            if ((getThing().getStatus() == ThingStatus.OFFLINE)
                    && ((getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)
                            || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE))) {
                return;
            }

            PowermaxX10Configuration config = getConfigAs(PowermaxX10Configuration.class);
            PowermaxX10Settings deviceSettings = (settings == null) ? null
                    : settings.getX10Settings(config.deviceNumber);
            if (getThing().getStatus() != ThingStatus.OFFLINE
                    && ((deviceSettings == null) || !deviceSettings.isEnabled())) {
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("Set handler status to OFFLINE for thing {} (device number {} undefined)",
                        getThing().getUID(), config.deviceNumber);
            } else if (getThing().getStatus() != ThingStatus.ONLINE && deviceSettings != null
                    && deviceSettings.isEnabled()) {
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Set handler status to ONLINE for thing {} (device number {} defined)",
                        getThing().getUID(), config.deviceNumber);
            } else {
                logger.debug("Handler status {} (unchanged) for thing {}", getThing().getStatus(), getThing().getUID());
            }
        }
    }

    @Override
    public void onZoneSettingsUpdated(int zoneNumber, PowermaxZoneSettings settings) {
        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            PowermaxZoneConfiguration config = getConfigAs(PowermaxZoneConfiguration.class);
            if (zoneNumber == config.zoneNumber) {
                if ((getThing().getStatus() == ThingStatus.OFFLINE) && ((getThing().getStatusInfo()
                        .getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)
                        || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE))) {
                    return;
                }

                if (getThing().getStatus() != ThingStatus.OFFLINE && settings == null) {
                    updateStatus(ThingStatus.OFFLINE);
                    logger.debug("Set handler status to OFFLINE for thing {} (zone number {} undefined)",
                            getThing().getUID(), zoneNumber);
                } else if (getThing().getStatus() != ThingStatus.ONLINE && settings != null) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Set handler status to ONLINE for thing {} (zone number {} defined)",
                            getThing().getUID(), zoneNumber);
                } else {
                    logger.debug("Handler status {} (unchanged) for thing {}", getThing().getStatus(),
                            getThing().getUID());
                }
            }
        }
    }

    public PowermaxZoneConfiguration getZoneConfiguration() {
        return getConfigAs(PowermaxZoneConfiguration.class);
    }

    public PowermaxX10Configuration getX10Configuration() {
        return getConfigAs(PowermaxX10Configuration.class);
    }
}

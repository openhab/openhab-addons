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
package org.openhab.binding.powermax.internal.handler;

import static org.openhab.binding.powermax.internal.PowermaxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.config.PowermaxX10Configuration;
import org.openhab.binding.powermax.internal.config.PowermaxZoneConfiguration;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettings;
import org.openhab.binding.powermax.internal.state.PowermaxPanelSettingsListener;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.openhab.binding.powermax.internal.state.PowermaxStateContainer.Value;
import org.openhab.binding.powermax.internal.state.PowermaxX10Settings;
import org.openhab.binding.powermax.internal.state.PowermaxZoneSettings;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowermaxThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxThingHandler extends BaseThingHandler implements PowermaxPanelSettingsListener {

    private static final int ZONE_NR_MIN = 1;
    private static final int ZONE_NR_MAX = 64;
    private static final int X10_NR_MIN = 1;
    private static final int X10_NR_MAX = 16;

    private final Logger logger = LoggerFactory.getLogger(PowermaxThingHandler.class);

    private @Nullable PowermaxBridgeHandler bridgeHandler;

    public PowermaxThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThingState(null, null);
        } else {
            initializeThingState(bridge.getHandler(), bridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Bridge status changed to {} for thing {}", bridgeStatusInfo, getThing().getUID());
        Bridge bridge = getBridge();
        initializeThingState((bridge == null) ? null : bridge.getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeThingState(@Nullable ThingHandler bridgeHandler, @Nullable ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                boolean validConfig = false;
                String errorMsg = String.format("@text/offline.config-error-unexpected-thing-type [ \"%s\" ]",
                        getThing().getThingTypeUID().getAsString());

                if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
                    PowermaxZoneConfiguration config = getConfigAs(PowermaxZoneConfiguration.class);
                    if (config.zoneNumber >= ZONE_NR_MIN && config.zoneNumber <= ZONE_NR_MAX) {
                        validConfig = true;
                    } else {
                        errorMsg = String.format("@text/offline.config-error-invalid-zone-number [ \"%d\", \"%d\" ]",
                                ZONE_NR_MIN, ZONE_NR_MAX);
                    }
                } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
                    PowermaxX10Configuration config = getConfigAs(PowermaxX10Configuration.class);
                    if (config.deviceNumber >= X10_NR_MIN && config.deviceNumber <= X10_NR_MAX) {
                        validConfig = true;
                    } else {
                        errorMsg = String.format("@text/offline.config-error-invalid-device-number [ \"%d\", \"%d\" ]",
                                X10_NR_MIN, X10_NR_MAX);
                    }
                }

                if (validConfig) {
                    updateStatus(ThingStatus.UNKNOWN);
                    logger.debug("Set handler status to UNKNOWN for thing {} (bridge ONLINE)", getThing().getUID());
                    PowermaxBridgeHandler powermaxBridgeHandler = (PowermaxBridgeHandler) bridgeHandler;
                    this.bridgeHandler = powermaxBridgeHandler;
                    powermaxBridgeHandler.registerPanelSettingsListener(this);
                    onPanelSettingsUpdated(powermaxBridgeHandler.getPanelSettings());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMsg);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                setAllChannelsOffline();
                logger.debug("Set handler status to OFFLINE for thing {} (bridge OFFLINE)", getThing().getUID());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            logger.debug("Set handler status to OFFLINE for thing {}", getThing().getUID());
        }
    }

    /**
     * Update all channels to an UNDEF state to indicate that the bridge is offline
     */
    private synchronized void setAllChannelsOffline() {
        getThing().getChannels().forEach(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }

    @Override
    public void dispose() {
        logger.debug("Handler disposed for thing {}", getThing().getUID());
        PowermaxBridgeHandler powermaxBridgeHandler = bridgeHandler;
        if (powermaxBridgeHandler != null) {
            powermaxBridgeHandler.unregisterPanelSettingsListener(this);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command {} from channel {}", command, channelUID.getId());

        PowermaxBridgeHandler powermaxBridgeHandler = bridgeHandler;
        if (powermaxBridgeHandler == null) {
            return;
        } else if (command instanceof RefreshType) {
            updateChannelFromAlarmState(channelUID.getId(), powermaxBridgeHandler.getCurrentState());
        } else {
            switch (channelUID.getId()) {
                case BYPASSED:
                    if (command instanceof OnOffType) {
                        powermaxBridgeHandler.zoneBypassed(
                                Byte.valueOf(
                                        (byte) (getConfigAs(PowermaxZoneConfiguration.class).zoneNumber & 0x000000FF)),
                                command.equals(OnOffType.ON));
                    } else {
                        logger.debug("Command of type {} while OnOffType is expected. Command is ignored.",
                                command.getClass().getSimpleName());
                    }
                    break;
                case X10_STATUS:
                    powermaxBridgeHandler.x10Command(
                            Byte.valueOf(
                                    (byte) (getConfigAs(PowermaxX10Configuration.class).deviceNumber & 0x000000FF)),
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
    public void updateChannelFromAlarmState(String channel, @Nullable PowermaxState state) {
        if (state == null || !isLinked(channel)) {
            return;
        }

        if (getThing().getThingTypeUID().equals(THING_TYPE_ZONE)) {
            int num = getConfigAs(PowermaxZoneConfiguration.class).zoneNumber;

            for (Value<?> value : state.getZone(num).getValues()) {
                String vChannel = value.getChannel();

                if (channel.equals(vChannel) && (value.getValue() != null)) {
                    updateState(vChannel, value.getState());
                }
            }
        } else if (getThing().getThingTypeUID().equals(THING_TYPE_X10)) {
            int num = getConfigAs(PowermaxX10Configuration.class).deviceNumber;
            Boolean status = state.getPGMX10DeviceStatus(num);
            if (channel.equals(X10_STATUS) && (status != null)) {
                updateState(X10_STATUS, status ? OnOffType.ON : OnOffType.OFF);
            }
        }
    }

    @Override
    public void onPanelSettingsUpdated(@Nullable PowermaxPanelSettings settings) {
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
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.disabled-device");
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
    public void onZoneSettingsUpdated(int zoneNumber, @Nullable PowermaxPanelSettings settings) {
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
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.zone-not-paired");
                        logger.debug("Set handler status to OFFLINE for thing {} (zone number {} not paired)",
                                getThing().getUID(), config.zoneNumber);
                    }
                } else if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                    logger.debug("Set handler status to ONLINE for thing {} (zone number {} paired)",
                            getThing().getUID(), config.zoneNumber);

                    logger.debug("Using name '{}' for {}", getThing().getLabel(), getThing().getUID());
                    zoneSettings.setName(getThing().getLabel());
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

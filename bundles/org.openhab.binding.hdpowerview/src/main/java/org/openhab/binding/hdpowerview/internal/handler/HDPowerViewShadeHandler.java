/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.handler;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;
import static org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.NotSupportedException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.binding.hdpowerview.internal.dto.BatteryKind;
import org.openhab.binding.hdpowerview.internal.dto.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.binding.hdpowerview.internal.dto.ShadeData;
import org.openhab.binding.hdpowerview.internal.dto.ShadePosition;
import org.openhab.binding.hdpowerview.internal.dto.SurveyData;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubShadeTimeoutException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands for an HD PowerView Shade
 *
 * @author Andy Lintner - Initial contribution
 * @author Andrew Fiddian-Green - Added support for secondary rail positions
 */
@NonNullByDefault
public class HDPowerViewShadeHandler extends AbstractHubbedThingHandler {

    private enum RefreshKind {
        POSITION,
        SURVEY,
        BATTERY_LEVEL
    }

    private static final String COMMAND_CALIBRATE = "CALIBRATE";
    private static final String COMMAND_IDENTIFY = "IDENTIFY";

    private static final String DETECTED_SECONDARY_RAIL = "secondaryRailDetected";
    private static final String DETECTED_TILT_ANYWHERE = "tiltAnywhereDetected";
    private static final ShadeCapabilitiesDatabase DB = new ShadeCapabilitiesDatabase();

    private final Map<String, String> detectedCapabilities = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);

    private @Nullable ScheduledFuture<?> refreshPositionFuture = null;
    private @Nullable ScheduledFuture<?> refreshSignalFuture = null;
    private @Nullable ScheduledFuture<?> refreshBatteryLevelFuture = null;
    private @Nullable Capabilities capabilities;
    private int shadeId;
    private boolean isDisposing;

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        isDisposing = false;
        shadeId = getConfigAs(HDPowerViewShadeConfiguration.class).id;
        logger.debug("Initializing shade handler for shade {}", shadeId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-bridge-handler");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing shade handler for shade {}", shadeId);
        isDisposing = true;
        ScheduledFuture<?> future = refreshPositionFuture;
        if (future != null) {
            future.cancel(true);
        }
        refreshPositionFuture = null;
        future = refreshSignalFuture;
        if (future != null) {
            future.cancel(true);
        }
        refreshSignalFuture = null;
        future = refreshBatteryLevelFuture;
        if (future != null) {
            future.cancel(true);
        }
        refreshBatteryLevelFuture = null;
        capabilities = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        if (RefreshType.REFRESH == command) {
            switch (channelId) {
                case CHANNEL_SHADE_POSITION:
                case CHANNEL_SHADE_SECONDARY_POSITION:
                case CHANNEL_SHADE_VANE:
                    requestRefreshShadePosition();
                    break;
                case CHANNEL_SHADE_LOW_BATTERY:
                case CHANNEL_SHADE_BATTERY_LEVEL:
                case CHANNEL_SHADE_BATTERY_VOLTAGE:
                    requestRefreshShadeBatteryLevel();
                    break;
                case CHANNEL_SHADE_SIGNAL_STRENGTH:
                case CHANNEL_SHADE_HUB_RSSI:
                case CHANNEL_SHADE_REPEATER_RSSI:
                    requestRefreshShadeSurvey();
                    break;
            }
            return;
        }

        HDPowerViewHubHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.warn("Missing bridge handler");
            return;
        }
        HDPowerViewWebTargets webTargets = bridge.getWebTargets();
        try {
            handleShadeCommand(channelId, command, webTargets, shadeId);
        } catch (HubInvalidResponseException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
            } else {
                logger.warn("Bridge returned a bad JSON response: {} -> {}", e.getMessage(), cause.getMessage());
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (HubShadeTimeoutException e) {
            logger.warn("Shade {} timeout when sending command {}", shadeId, command);
        } catch (HubException e) {
            // ScheduledFutures will be cancelled by dispose(), naturally causing InterruptedException in invoke()
            // for any ongoing requests. Logging this would only cause confusion.
            if (!isDisposing) {
                logger.warn("Unexpected error: {}", e.getMessage());
            }
        }
    }

    private void handleShadeCommand(String channelId, Command command, HDPowerViewWebTargets webTargets, int shadeId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException,
            HubShadeTimeoutException {
        switch (channelId) {
            case CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    moveShade(PRIMARY_POSITION, ((PercentType) command).intValue(), webTargets, shadeId);
                } else if (command instanceof UpDownType) {
                    moveShade(PRIMARY_POSITION, UpDownType.UP == command ? 0 : 100, webTargets, shadeId);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP == command) {
                        stopShade(webTargets, shadeId);
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;

            case CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    moveShade(VANE_TILT_POSITION, ((PercentType) command).intValue(), webTargets, shadeId);
                } else if (command instanceof OnOffType) {
                    moveShade(VANE_TILT_POSITION, OnOffType.ON == command ? 100 : 0, webTargets, shadeId);
                }
                break;

            case CHANNEL_SHADE_SECONDARY_POSITION:
                if (command instanceof PercentType) {
                    moveShade(SECONDARY_POSITION, ((PercentType) command).intValue(), webTargets, shadeId);
                } else if (command instanceof UpDownType) {
                    moveShade(SECONDARY_POSITION, UpDownType.UP == command ? 0 : 100, webTargets, shadeId);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP == command) {
                        stopShade(webTargets, shadeId);
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;

            case CHANNEL_SHADE_COMMAND:
                if (command instanceof StringType) {
                    if (COMMAND_IDENTIFY.equals(((StringType) command).toString())) {
                        logger.debug("Identify shade {}", shadeId);
                        identifyShade(webTargets, shadeId);
                    } else if (COMMAND_CALIBRATE.equals(((StringType) command).toString())) {
                        logger.debug("Calibrate shade {}", shadeId);
                        calibrateShade(webTargets, shadeId);
                    }
                } else {
                    logger.warn("Unsupported command: {}. Supported commands are: " + COMMAND_CALIBRATE, command);
                }
                break;
        }
    }

    /**
     * Update the state of the channels based on the ShadeData provided.
     *
     * @param shadeData the ShadeData to be used.
     */
    protected void onReceiveUpdate(ShadeData shadeData) {
        updateStatus(ThingStatus.ONLINE);
        updateCapabilities(shadeData);
        updateSoftProperties(shadeData);
        updateFirmwareProperties(shadeData);
        ShadePosition shadePosition = shadeData.positions;
        if (shadePosition != null) {
            updatePositionStates(shadePosition);
        }
        updateBatteryStates(shadeData.batteryStatus, shadeData.batteryStrength);
        updateSignalStrengthState(shadeData.signalStrength);
    }

    private void updateCapabilities(ShadeData shade) {
        if (capabilities != null) {
            // Already cached.
            return;
        }
        Capabilities capabilities = DB.getCapabilities(shade.type, shade.capabilities);
        if (capabilities.getValue() < 0) {
            logger.debug("Unable to set capabilities for shade {}", shade.id);
            return;
        }
        logger.debug("Caching capabilities {} for shade {}", capabilities.getValue(), shade.id);
        this.capabilities = capabilities;

        updateDynamicChannels(capabilities, shade);
    }

    private Capabilities getCapabilitiesOrDefault() {
        Capabilities capabilities = this.capabilities;
        if (capabilities == null) {
            return new Capabilities();
        }
        return capabilities;
    }

    /**
     * Update the Thing's properties based on the contents of the provided ShadeData.
     *
     * Checks the database of known Shade 'types' and 'capabilities' and logs any unknown or incompatible values, so
     * that developers can be kept updated about the potential need to add support for that type resp. capabilities.
     *
     * @param shadeData
     */
    private void updateSoftProperties(ShadeData shadeData) {
        final Map<String, String> properties = getThing().getProperties();
        boolean propChanged = false;

        // update 'type' property
        final int type = shadeData.type;
        String propKey = HDPowerViewBindingConstants.PROPERTY_SHADE_TYPE;
        String propOldVal = properties.getOrDefault(propKey, "");
        String propNewVal = DB.getType(type).toString();
        if (!propNewVal.equals(propOldVal)) {
            propChanged = true;
            getThing().setProperty(propKey, propNewVal);
            if ((type > 0) && !DB.isTypeInDatabase(type)) {
                DB.logTypeNotInDatabase(type);
            }
        }

        // update 'capabilities' property
        Capabilities capabilities = DB.getCapabilities(shadeData.capabilities);
        final int capabilitiesVal = capabilities.getValue();
        propKey = HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES;
        propOldVal = properties.getOrDefault(propKey, "");
        propNewVal = capabilities.toString();
        if (!propNewVal.equals(propOldVal)) {
            propChanged = true;
            getThing().setProperty(propKey, propNewVal);
            if ((capabilitiesVal >= 0) && !DB.isCapabilitiesInDatabase(capabilitiesVal)) {
                DB.logCapabilitiesNotInDatabase(type, capabilitiesVal);
            }
        }

        if (propChanged && DB.isCapabilitiesInDatabase(capabilitiesVal) && DB.isTypeInDatabase(type)
                && (capabilitiesVal != DB.getType(type).getCapabilities()) && (shadeData.capabilities != null)) {
            DB.logCapabilitiesMismatch(type, capabilitiesVal);
        }
    }

    private void updateFirmwareProperties(ShadeData shadeData) {
        Map<String, String> properties = editProperties();
        Firmware shadeFirmware = shadeData.firmware;
        Firmware motorFirmware = shadeData.motor;
        if (shadeFirmware != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, shadeFirmware.toString());
        }
        if (motorFirmware != null) {
            properties.put(PROPERTY_MOTOR_FIRMWARE_VERSION, motorFirmware.toString());
        }
        updateProperties(properties);
    }

    /**
     * After a hard refresh, update the Thing's detected capabilities based on the contents of the provided ShadeData.
     *
     * Checks if the secondary support capabilities in the database of known Shade 'types' and 'capabilities' matches
     * that implied by the ShadeData and logs any incompatible values, so that developers can be kept updated about the
     * potential need to add support for that type resp. capabilities.
     *
     * @param shadeData
     */
    private void updateDetectedCapabilities(ShadeData shadeData) {
        final ShadePosition positions = shadeData.positions;
        if (positions == null) {
            return;
        }
        Capabilities capabilities = getCapabilitiesOrDefault();

        // update 'secondary rail' detected capability
        String capsKey = DETECTED_SECONDARY_RAIL;
        String capsOldVal = detectedCapabilities.getOrDefault(capsKey, "");
        boolean capsNewBool = positions.secondaryRailDetected();
        String capsNewVal = String.valueOf(capsNewBool);
        if (!capsNewVal.equals(capsOldVal)) {
            detectedCapabilities.put(capsKey, capsNewVal);
            if (capsNewBool != capabilities.supportsSecondary()) {
                DB.logPropertyMismatch(capsKey, shadeData.type, capabilities.getValue(), capsNewBool);
            }
        }

        // update 'tilt anywhere' detected capability
        capsKey = DETECTED_TILT_ANYWHERE;
        capsOldVal = detectedCapabilities.getOrDefault(capsKey, "");
        capsNewBool = positions.tiltAnywhereDetected();
        capsNewVal = String.valueOf(capsNewBool);
        if (!capsNewVal.equals(capsOldVal)) {
            detectedCapabilities.put(capsKey, capsNewVal);
            if (capsNewBool != capabilities.supportsTiltAnywhere()) {
                DB.logPropertyMismatch(capsKey, shadeData.type, capabilities.getValue(), capsNewBool);
            }
        }
    }

    private void updatePositionStates(ShadePosition shadePos) {
        Capabilities capabilities = this.capabilities;
        if (capabilities == null) {
            logger.debug("The 'shadeCapabilities' field has not yet been initialized");
            updateState(CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_VANE, UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, UnDefType.UNDEF);
            return;
        }
        updateState(CHANNEL_SHADE_POSITION, shadePos.getState(capabilities, PRIMARY_POSITION));
        updateState(CHANNEL_SHADE_VANE, shadePos.getState(capabilities, VANE_TILT_POSITION));
        updateState(CHANNEL_SHADE_SECONDARY_POSITION, shadePos.getState(capabilities, SECONDARY_POSITION));
    }

    private void updateBatteryStates(int batteryStatus, double batteryStrength) {
        updateBatteryLevelStates(batteryStatus);
        updateState(CHANNEL_SHADE_BATTERY_VOLTAGE,
                batteryStrength > 0 ? new QuantityType<>(batteryStrength / 10, Units.VOLT) : UnDefType.UNDEF);
    }

    private void updateBatteryLevelStates(int batteryStatus) {
        int mappedValue;
        switch (batteryStatus) {
            case 1: // Low
                mappedValue = 10;
                break;
            case 2: // Medium
                mappedValue = 50;
                break;
            case 3: // High
            case 4: // Plugged in
                mappedValue = 100;
                break;
            default: // No status available (0) or invalid
                updateState(CHANNEL_SHADE_LOW_BATTERY, UnDefType.UNDEF);
                updateState(CHANNEL_SHADE_BATTERY_LEVEL, UnDefType.UNDEF);
                return;
        }
        updateState(CHANNEL_SHADE_LOW_BATTERY, batteryStatus == 1 ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_SHADE_BATTERY_LEVEL, new DecimalType(mappedValue));
    }

    private void updateSignalStrengthState(int signalStrength) {
        updateState(CHANNEL_SHADE_SIGNAL_STRENGTH, new DecimalType(signalStrength));
    }

    private void moveShade(CoordinateSystem coordSys, int newPercent, HDPowerViewWebTargets webTargets, int shadeId)
            throws HubInvalidResponseException, HubProcessingException, HubMaintenanceException,
            HubShadeTimeoutException {
        ShadePosition newPosition = null;
        // (try to) read the positions from the hub
        ShadeData shadeData = webTargets.getShade(shadeId);
        updateCapabilities(shadeData);
        newPosition = shadeData.positions;
        // if no positions returned, then create a new position
        if (newPosition == null) {
            newPosition = new ShadePosition();
        }
        Capabilities capabilities = getCapabilitiesOrDefault();
        // set the new position value, and write the positions to the hub
        shadeData = webTargets.moveShade(shadeId, newPosition.setPosition(capabilities, coordSys, newPercent));
        updateShadePositions(shadeData);
    }

    private void stopShade(HDPowerViewWebTargets webTargets, int shadeId) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException {
        updateShadePositions(webTargets.stopShade(shadeId));
        // Positions in response from stop motion is not updated to to actual positions yet,
        // so we need to request hard refresh.
        requestRefreshShadePosition();
    }

    private void identifyShade(HDPowerViewWebTargets webTargets, int shadeId) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException {
        updateShadePositions(webTargets.jogShade(shadeId));
    }

    private void calibrateShade(HDPowerViewWebTargets webTargets, int shadeId) throws HubInvalidResponseException,
            HubProcessingException, HubMaintenanceException, HubShadeTimeoutException {
        updateShadePositions(webTargets.calibrateShade(shadeId));
    }

    private void updateShadePositions(ShadeData shadeData) {
        ShadePosition shadePosition = shadeData.positions;
        if (shadePosition == null) {
            return;
        }
        updateCapabilities(shadeData);
        updatePositionStates(shadePosition);
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its current position
     */
    protected synchronized void requestRefreshShadePosition() {
        if (refreshPositionFuture == null) {
            refreshPositionFuture = scheduler.schedule(this::doRefreshShadePosition, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its survey data
     */
    protected synchronized void requestRefreshShadeSurvey() {
        if (refreshSignalFuture == null) {
            refreshSignalFuture = scheduler.schedule(this::doRefreshShadeSignal, 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its battery level state
     */
    protected synchronized void requestRefreshShadeBatteryLevel() {
        if (refreshBatteryLevelFuture == null) {
            refreshBatteryLevelFuture = scheduler.schedule(this::doRefreshShadeBatteryLevel, 0, TimeUnit.SECONDS);
        }
    }

    private void doRefreshShadePosition() {
        this.doRefreshShade(RefreshKind.POSITION);
        refreshPositionFuture = null;
    }

    private void doRefreshShadeSignal() {
        this.doRefreshShade(RefreshKind.SURVEY);
        refreshSignalFuture = null;
    }

    private void doRefreshShadeBatteryLevel() {
        this.doRefreshShade(RefreshKind.BATTERY_LEVEL);
        refreshBatteryLevelFuture = null;
    }

    private void doRefreshShade(RefreshKind kind) {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new HubProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            ShadeData shadeData;
            switch (kind) {
                case POSITION:
                    shadeData = webTargets.refreshShadePosition(shadeId);
                    updateShadePositions(shadeData);
                    updateDetectedCapabilities(shadeData);
                    break;
                case SURVEY:
                    List<SurveyData> surveyData = webTargets.getShadeSurvey(shadeId);
                    if (!surveyData.isEmpty()) {
                        if (logger.isDebugEnabled()) {
                            StringJoiner joiner = new StringJoiner(", ");
                            surveyData.forEach(data -> joiner.add(data.toString()));
                            logger.debug("Survey response for shade {}: {}", shadeId, joiner.toString());
                        }

                        int hubRssi = Integer.MAX_VALUE;
                        int repeaterRssi = Integer.MAX_VALUE;
                        for (SurveyData survey : surveyData) {
                            if (survey.neighborId == 0) {
                                hubRssi = survey.rssi;
                            } else {
                                repeaterRssi = survey.rssi;
                            }
                        }
                        updateState(CHANNEL_SHADE_HUB_RSSI, hubRssi == Integer.MAX_VALUE ? UnDefType.UNDEF
                                : new QuantityType<>(hubRssi, Units.DECIBEL_MILLIWATTS));
                        updateState(CHANNEL_SHADE_REPEATER_RSSI, repeaterRssi == Integer.MAX_VALUE ? UnDefType.UNDEF
                                : new QuantityType<>(repeaterRssi, Units.DECIBEL_MILLIWATTS));

                        shadeData = webTargets.getShade(shadeId);
                        updateSignalStrengthState(shadeData.signalStrength);
                    } else {
                        logger.info("No data from shade {} survey", shadeId);
                        /*
                         * Setting signal strength channel to UNDEF here would be reverted on next poll,
                         * since signal strength is part of shade response. So leaving current value,
                         * even though refreshing the value failed.
                         */
                        updateState(CHANNEL_SHADE_HUB_RSSI, UnDefType.UNDEF);
                        updateState(CHANNEL_SHADE_REPEATER_RSSI, UnDefType.UNDEF);
                    }
                    break;
                case BATTERY_LEVEL:
                    shadeData = webTargets.refreshShadeBatteryLevel(shadeId);
                    updateBatteryStates(shadeData.batteryStatus, shadeData.batteryStrength);
                    break;
                default:
                    throw new NotSupportedException("Unsupported refresh kind " + kind.toString());
            }
        } catch (HubInvalidResponseException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
            } else {
                logger.warn("Bridge returned a bad JSON response: {} -> {}", e.getMessage(), cause.getMessage());
            }
            // Survey calls are unreliable and often returns "{}" as payload. For repeater RSSI tracking to be useful,
            // we need to reset channels also in this case.
            if (kind == RefreshKind.SURVEY) {
                updateState(CHANNEL_SHADE_HUB_RSSI, UnDefType.UNDEF);
                updateState(CHANNEL_SHADE_REPEATER_RSSI, UnDefType.UNDEF);
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (HubShadeTimeoutException e) {
            logger.info("Shade {} wireless refresh time out", shadeId);
        } catch (HubException e) {
            // ScheduledFutures will be cancelled by dispose(), naturally causing InterruptedException in invoke()
            // for any ongoing requests. Logging this would only cause confusion.
            if (!isDisposing) {
                logger.warn("Unexpected error: {}", e.getMessage());
            }
        }
    }

    /**
     * If the given channel exists in the thing, but is NOT required in the thing, then add it to a list of channels to
     * be removed. Or if the channel does NOT exist in the thing, but is required in the thing, then log a warning.
     *
     * @param removeList the list of channels to be removed from the thing.
     * @param channelId the id of the channel to be (eventually) removed.
     * @param channelRequired true if the thing requires this channel.
     */
    private void removeListProcessChannel(List<Channel> removeList, String channelId, boolean channelRequired) {
        Channel channel = thing.getChannel(channelId);
        if (!channelRequired && channel != null) {
            removeList.add(channel);
        } else if (channelRequired && channel == null) {
            logger.warn("Shade {} does not have a '{}' channel => please reinitialize the thing", shadeId, channelId);
        }
    }

    /**
     * Remove previously statically created channels if the shade does not support them or they are not relevant.
     *
     * @param capabilities the capabilities of the shade.
     * @param shade the shade data.
     */
    private void updateDynamicChannels(Capabilities capabilities, ShadeData shade) {
        List<Channel> removeList = new ArrayList<>();

        removeListProcessChannel(removeList, CHANNEL_SHADE_POSITION, capabilities.supportsPrimary());

        removeListProcessChannel(removeList, CHANNEL_SHADE_SECONDARY_POSITION,
                capabilities.supportsSecondary() || capabilities.supportsSecondaryOverlapped());

        removeListProcessChannel(removeList, CHANNEL_SHADE_VANE,
                capabilities.supportsTiltAnywhere() || capabilities.supportsTiltOnClosed());

        boolean batteryChannelsRequired = shade.getBatteryKind() != BatteryKind.HARDWIRED_POWER_SUPPLY;
        removeListProcessChannel(removeList, CHANNEL_SHADE_BATTERY_LEVEL, batteryChannelsRequired);
        removeListProcessChannel(removeList, CHANNEL_SHADE_LOW_BATTERY, batteryChannelsRequired);

        if (!removeList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringJoiner joiner = new StringJoiner(", ");
                removeList.forEach(c -> joiner.add(c.getUID().getId()));
                logger.debug("Removing unsupported channels for {}: {}", shadeId, joiner.toString());
            }
            updateThing(editThing().withoutChannels(removeList).build());
        }
    }
}

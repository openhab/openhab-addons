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
import static org.openhab.binding.hdpowerview.internal.api.CoordinateSystem.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.NotSupportedException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.responses.Survey;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
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

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);
    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();

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
        if (shadeId <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-id");
            return;
        }
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        if (!(bridge.getHandler() instanceof HDPowerViewHubHandler)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.conf-error.invalid-bridge-handler");
            return;
        }
        ThingStatus bridgeStatus = bridge.getStatus();
        if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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
        if (webTargets == null) {
            logger.warn("Web targets not initialized");
            return;
        }
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
        updateState(CHANNEL_SHADE_SIGNAL_STRENGTH, new DecimalType(shadeData.signalStrength));
    }

    private void updateCapabilities(ShadeData shade) {
        if (capabilities != null) {
            // Already cached.
            return;
        }
        Integer value = shade.capabilities;
        if (value != null) {
            int valueAsInt = value.intValue();
            logger.debug("Caching capabilities {} for shade {}", valueAsInt, shade.id);
            capabilities = db.getCapabilities(valueAsInt);
        } else {
            logger.debug("Capabilities not included in shade response");
        }
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
        String propNewVal = db.getType(type).toString();
        if (!propNewVal.equals(propOldVal)) {
            propChanged = true;
            getThing().setProperty(propKey, propNewVal);
            if ((type > 0) && !db.isTypeInDatabase(type)) {
                db.logTypeNotInDatabase(type);
            }
        }

        // update 'capabilities' property
        final Integer temp = shadeData.capabilities;
        final int capabilitiesVal = temp != null ? temp.intValue() : -1;
        Capabilities capabilities = db.getCapabilities(capabilitiesVal);
        propKey = HDPowerViewBindingConstants.PROPERTY_SHADE_CAPABILITIES;
        propOldVal = properties.getOrDefault(propKey, "");
        propNewVal = capabilities.toString();
        if (!propNewVal.equals(propOldVal)) {
            propChanged = true;
            getThing().setProperty(propKey, propNewVal);
            if ((capabilitiesVal >= 0) && !db.isCapabilitiesInDatabase(capabilitiesVal)) {
                db.logCapabilitiesNotInDatabase(type, capabilitiesVal);
            }
        }

        if (propChanged && db.isCapabilitiesInDatabase(capabilitiesVal) && db.isTypeInDatabase(type)
                && (capabilitiesVal != db.getType(type).getCapabilities())) {
            db.logCapabilitiesMismatch(type, capabilitiesVal);
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
     * After a hard refresh, update the Thing's properties based on the contents of the provided ShadeData.
     *
     * Checks if the secondary support capabilities in the database of known Shade 'types' and 'capabilities' matches
     * that implied by the ShadeData and logs any incompatible values, so that developers can be kept updated about the
     * potential need to add support for that type resp. capabilities.
     *
     * @param shadeData
     */
    private void updateHardProperties(ShadeData shadeData) {
        final ShadePosition positions = shadeData.positions;
        if (positions == null) {
            return;
        }
        Capabilities capabilities = getCapabilitiesOrDefault();
        final Map<String, String> properties = getThing().getProperties();

        // update 'secondary rail detected' property
        String propKey = HDPowerViewBindingConstants.PROPERTY_SECONDARY_RAIL_DETECTED;
        String propOldVal = properties.getOrDefault(propKey, "");
        boolean propNewBool = positions.secondaryRailDetected();
        String propNewVal = String.valueOf(propNewBool);
        if (!propNewVal.equals(propOldVal)) {
            getThing().setProperty(propKey, propNewVal);
            if (propNewBool != capabilities.supportsSecondary()) {
                db.logPropertyMismatch(propKey, shadeData.type, capabilities.getValue(), propNewBool);
            }
        }

        // update 'tilt anywhere detected' property
        propKey = HDPowerViewBindingConstants.PROPERTY_TILT_ANYWHERE_DETECTED;
        propOldVal = properties.getOrDefault(propKey, "");
        propNewBool = positions.tiltAnywhereDetected();
        propNewVal = String.valueOf(propNewBool);
        if (!propNewVal.equals(propOldVal)) {
            getThing().setProperty(propKey, propNewVal);
            if (propNewBool != capabilities.supportsTiltAnywhere()) {
                db.logPropertyMismatch(propKey, shadeData.type, capabilities.getValue(), propNewBool);
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
            if (webTargets == null) {
                throw new HubProcessingException("Web targets not initialized");
            }
            ShadeData shadeData;
            switch (kind) {
                case POSITION:
                    shadeData = webTargets.refreshShadePosition(shadeId);
                    updateShadePositions(shadeData);
                    updateHardProperties(shadeData);
                    break;
                case SURVEY:
                    Survey survey = webTargets.getShadeSurvey(shadeId);
                    if (survey.surveyData != null) {
                        logger.debug("Survey response for shade {}: {}", survey.shadeId, survey.toString());
                    } else {
                        logger.warn("No response from shade {} survey", shadeId);
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
}

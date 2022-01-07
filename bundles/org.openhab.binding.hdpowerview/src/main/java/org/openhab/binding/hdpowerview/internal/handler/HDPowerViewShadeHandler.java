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
import org.openhab.binding.hdpowerview.internal.HubMaintenanceException;
import org.openhab.binding.hdpowerview.internal.HubProcessingException;
import org.openhab.binding.hdpowerview.internal.api.CoordinateSystem;
import org.openhab.binding.hdpowerview.internal.api.Firmware;
import org.openhab.binding.hdpowerview.internal.api.ShadePosition;
import org.openhab.binding.hdpowerview.internal.api.responses.Shade;
import org.openhab.binding.hdpowerview.internal.api.responses.Shades.ShadeData;
import org.openhab.binding.hdpowerview.internal.api.responses.Survey;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewShadeConfiguration;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
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

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewShadeHandler.class);

    private static final int REFRESH_DELAY_SEC = 10;
    private @Nullable ScheduledFuture<?> refreshPositionFuture = null;
    private @Nullable ScheduledFuture<?> refreshSignalFuture = null;
    private @Nullable ScheduledFuture<?> refreshBatteryLevelFuture = null;

    private final ShadeCapabilitiesDatabase db = new ShadeCapabilitiesDatabase();
    private int shadeCapabilities = -1;

    public HDPowerViewShadeHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        try {
            getShadeId();
        } catch (NumberFormatException e) {
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
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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

        switch (channelId) {
            case CHANNEL_SHADE_POSITION:
                if (command instanceof PercentType) {
                    moveShade(PRIMARY_ZERO_IS_CLOSED, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    moveShade(PRIMARY_ZERO_IS_CLOSED, UpDownType.UP.equals(command) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP.equals(command)) {
                        stopShade();
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;

            case CHANNEL_SHADE_VANE:
                if (command instanceof PercentType) {
                    moveShade(VANE_TILT_COORDS, ((PercentType) command).intValue());
                } else if (command instanceof OnOffType) {
                    moveShade(VANE_TILT_COORDS, OnOffType.ON.equals(command) ? 100 : 0);
                }
                break;

            case CHANNEL_SHADE_SECONDARY_POSITION:
                if (command instanceof PercentType) {
                    moveShade(SECONDARY_ZERO_IS_OPEN, ((PercentType) command).intValue());
                } else if (command instanceof UpDownType) {
                    moveShade(SECONDARY_ZERO_IS_OPEN, UpDownType.UP.equals(command) ? 0 : 100);
                } else if (command instanceof StopMoveType) {
                    if (StopMoveType.STOP.equals(command)) {
                        stopShade();
                    } else {
                        logger.warn("Unexpected StopMoveType command");
                    }
                }
                break;
        }
    }

    /**
     * Update the state of the channels based on the ShadeData provided.
     *
     * @param shadeData the ShadeData to be used; may be null.
     */
    protected void onReceiveUpdate(@Nullable ShadeData shadeData) {
        if (shadeData != null) {
            updateStatus(ThingStatus.ONLINE);
            updateSoftProperties(shadeData);
            updateFirmwareProperties(shadeData);
            updateBindingStates(shadeData.positions);
            updateBatteryLevel(shadeData.batteryStatus);
            updateState(CHANNEL_SHADE_BATTERY_VOLTAGE,
                    shadeData.batteryStrength > 0 ? new QuantityType<>(shadeData.batteryStrength / 10, Units.VOLT)
                            : UnDefType.UNDEF);
            updateState(CHANNEL_SHADE_SIGNAL_STRENGTH, new DecimalType(shadeData.signalStrength));
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
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

        // update shadeCapabilities field
        if (capabilitiesVal >= 0) {
            shadeCapabilities = capabilitiesVal;
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
            properties.put(PROPERTY_FIRMWARE_VERSION, shadeFirmware.toString());
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
        if (positions != null) {
            final Map<String, String> properties = getThing().getProperties();

            // update 'jsonHasSecondary' property
            String propKey = HDPowerViewBindingConstants.PROPERTY_SECONDARY_RAIL_DETECTED;
            String propOldVal = properties.getOrDefault(propKey, "");
            boolean propNewBool = positions.secondaryRailDetected();
            String propNewVal = String.valueOf(propNewBool);
            if (!propNewVal.equals(propOldVal)) {
                getThing().setProperty(propKey, propNewVal);
                final Integer temp = shadeData.capabilities;
                final int capabilities = temp != null ? temp.intValue() : -1;
                if (propNewBool != db.getCapabilities(capabilities).supportsSecondary()) {
                    db.logPropertyMismatch(propKey, shadeData.type, capabilities, propNewBool);
                }
            }

            // update 'jsonTiltAnywhere' property
            propKey = HDPowerViewBindingConstants.PROPERTY_TILT_ANYWHERE_DETECTED;
            propOldVal = properties.getOrDefault(propKey, "");
            propNewBool = positions.tiltAnywhereDetected();
            propNewVal = String.valueOf(propNewBool);
            if (!propNewVal.equals(propOldVal)) {
                getThing().setProperty(propKey, propNewVal);
                final Integer temp = shadeData.capabilities;
                final int capabilities = temp != null ? temp.intValue() : -1;
                if (propNewBool != db.getCapabilities(capabilities).supportsTiltAnywhere()) {
                    db.logPropertyMismatch(propKey, shadeData.type, capabilities, propNewBool);
                }
            }
        }
    }

    private void updateBindingStates(@Nullable ShadePosition shadePos) {
        if (shadePos == null) {
            logger.debug("The value of 'shadePosition' argument was null!");
        } else if (shadeCapabilities < 0) {
            logger.debug("The 'shadeCapabilities' field has not been initialized!");
        } else {
            Capabilities caps = db.getCapabilities(shadeCapabilities);
            updateState(CHANNEL_SHADE_POSITION, shadePos.getState(caps, PRIMARY_ZERO_IS_CLOSED));
            updateState(CHANNEL_SHADE_VANE, shadePos.getState(caps, VANE_TILT_COORDS));
            updateState(CHANNEL_SHADE_SECONDARY_POSITION, shadePos.getState(caps, SECONDARY_ZERO_IS_OPEN));
            return;
        }
        updateState(CHANNEL_SHADE_POSITION, UnDefType.UNDEF);
        updateState(CHANNEL_SHADE_VANE, UnDefType.UNDEF);
        updateState(CHANNEL_SHADE_SECONDARY_POSITION, UnDefType.UNDEF);
    }

    private void updateBatteryLevel(int batteryStatus) {
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

    private void moveShade(CoordinateSystem coordSys, int newPercent) {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new HubProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            if (webTargets == null) {
                throw new HubProcessingException("Web targets not initialized");
            }
            ShadePosition newPosition = null;
            // (try to) read the positions from the hub
            int shadeId = getShadeId();
            Shade shade = webTargets.getShade(shadeId);
            if (shade != null) {
                ShadeData shadeData = shade.shade;
                if (shadeData != null) {
                    newPosition = shadeData.positions;
                }
            }
            // if no positions returned, then create a new position
            if (newPosition == null) {
                newPosition = new ShadePosition();
            }
            // set the new position value, and write the positions to the hub
            webTargets.moveShade(shadeId,
                    newPosition.setPosition(db.getCapabilities(shadeCapabilities), coordSys, newPercent));
            // update the Channels to match the new position
            final ShadePosition finalPosition = newPosition;
            scheduler.submit(() -> {
                updateBindingStates(finalPosition);
            });
        } catch (HubProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
            return;
        }
    }

    private int getShadeId() throws NumberFormatException {
        String str = getConfigAs(HDPowerViewShadeConfiguration.class).id;
        if (str == null) {
            throw new NumberFormatException("null input string");
        }
        return Integer.parseInt(str);
    }

    private void stopShade() {
        try {
            HDPowerViewHubHandler bridge;
            if ((bridge = getBridgeHandler()) == null) {
                throw new HubProcessingException("Missing bridge handler");
            }
            HDPowerViewWebTargets webTargets = bridge.getWebTargets();
            if (webTargets == null) {
                throw new HubProcessingException("Web targets not initialized");
            }
            int shadeId = getShadeId();
            webTargets.stopShade(shadeId);
            requestRefreshShadePosition();
        } catch (HubProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
            return;
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
            return;
        }
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its current position
     */
    protected synchronized void requestRefreshShadePosition() {
        if (refreshPositionFuture == null) {
            refreshPositionFuture = scheduler.schedule(this::doRefreshShadePosition, REFRESH_DELAY_SEC,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its survey data
     */
    protected synchronized void requestRefreshShadeSurvey() {
        if (refreshSignalFuture == null) {
            refreshSignalFuture = scheduler.schedule(this::doRefreshShadeSignal, REFRESH_DELAY_SEC, TimeUnit.SECONDS);
        }
    }

    /**
     * Request that the shade shall undergo a 'hard' refresh for querying its battery level state
     */
    protected synchronized void requestRefreshShadeBatteryLevel() {
        if (refreshBatteryLevelFuture == null) {
            refreshBatteryLevelFuture = scheduler.schedule(this::doRefreshShadeBatteryLevel, REFRESH_DELAY_SEC,
                    TimeUnit.SECONDS);
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
            int shadeId = getShadeId();
            Shade shade;
            switch (kind) {
                case POSITION:
                    shade = webTargets.refreshShadePosition(shadeId);
                    break;
                case SURVEY:
                    Survey survey = webTargets.getShadeSurvey(shadeId);
                    if (survey != null && survey.surveyData != null) {
                        logger.debug("Survey response for shade {}: {}", survey.shadeId, survey.toString());
                    } else {
                        logger.warn("No response from shade {} survey", shadeId);
                    }
                    return;
                case BATTERY_LEVEL:
                    shade = webTargets.refreshShadeBatteryLevel(shadeId);
                    break;
                default:
                    throw new NotSupportedException("Unsupported refresh kind " + kind.toString());
            }
            if (shade != null) {
                ShadeData shadeData = shade.shade;
                if (shadeData != null) {
                    if (Boolean.TRUE.equals(shadeData.timedOut)) {
                        logger.warn("Shade {} wireless refresh time out", shadeId);
                    } else if (kind == RefreshKind.POSITION) {
                        updateHardProperties(shadeData);
                    }
                }
            }
        } catch (HubProcessingException | NumberFormatException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        }
    }
}

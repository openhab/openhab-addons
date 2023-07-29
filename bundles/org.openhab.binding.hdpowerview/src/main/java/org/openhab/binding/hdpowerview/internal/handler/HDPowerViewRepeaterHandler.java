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
package org.openhab.binding.hdpowerview.internal.handler;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewRepeaterConfiguration;
import org.openhab.binding.hdpowerview.internal.dto.Color;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.binding.hdpowerview.internal.dto.responses.RepeaterData;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubInvalidResponseException;
import org.openhab.binding.hdpowerview.internal.exceptions.HubMaintenanceException;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles commands for an HD PowerView Repeater
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class HDPowerViewRepeaterHandler extends AbstractHubbedThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewRepeaterHandler.class);

    private static final int REFRESH_INTERVAL_MINUTES = 5;
    private static final int IDENTITY_PERIOD_SECONDS = 3;
    private static final String COMMAND_IDENTIFY = "IDENTIFY";

    private @Nullable ScheduledFuture<?> refreshStatusFuture = null;
    private @Nullable ScheduledFuture<?> resetIdentifyStateFuture = null;
    private int repeaterId;

    public HDPowerViewRepeaterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        repeaterId = getConfigAs(HDPowerViewRepeaterConfiguration.class).id;
        logger.debug("Initializing repeater handler for repeater {}", repeaterId);
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-bridge-handler");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduleRefreshJob();
    }

    @Override
    public void dispose() {
        logger.debug("Disposing repeater handler for repeater {}", repeaterId);
        cancelRefreshJob();
        cancelResetIdentifyStateJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        HDPowerViewHubHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.warn("Missing bridge handler");
            return;
        }
        HDPowerViewWebTargets webTargets = bridge.getWebTargets();
        try {
            RepeaterData repeaterData;

            switch (channelUID.getId()) {
                case CHANNEL_REPEATER_COLOR:
                    if (command instanceof HSBType) {
                        Color currentColor = webTargets.getRepeater(repeaterId).color;
                        if (currentColor != null) {
                            HSBType hsbCommand = (HSBType) command;
                            var color = new Color(currentColor.brightness, hsbCommand);
                            repeaterData = webTargets.setRepeaterColor(repeaterId, color);
                            scheduler.submit(() -> updatePropertyAndStates(repeaterData));
                        }
                    } else if (command instanceof OnOffType) {
                        Color currentColor = webTargets.getRepeater(repeaterId).color;
                        if (currentColor != null) {
                            var color = command == OnOffType.ON
                                    ? new Color(currentColor.brightness, java.awt.Color.WHITE)
                                    : new Color(currentColor.brightness, java.awt.Color.BLACK);
                            repeaterData = webTargets.setRepeaterColor(repeaterId, color);
                            scheduler.submit(() -> updatePropertyAndStates(repeaterData));
                        }
                    }
                    break;
                case CHANNEL_REPEATER_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        Color currentColor = webTargets.getRepeater(repeaterId).color;
                        if (currentColor != null) {
                            PercentType brightness = (PercentType) command;
                            var color = new Color(brightness.intValue(), currentColor.red, currentColor.green,
                                    currentColor.blue);
                            repeaterData = webTargets.setRepeaterColor(repeaterId, color);
                            scheduler.submit(() -> updatePropertyAndStates(repeaterData));
                        }
                    }
                    break;
                case CHANNEL_REPEATER_IDENTIFY:
                    if (command instanceof StringType) {
                        if (COMMAND_IDENTIFY.equals(((StringType) command).toString())) {
                            repeaterData = webTargets.identifyRepeater(repeaterId);
                            scheduler.submit(() -> updatePropertyAndStates(repeaterData));
                            cancelResetIdentifyStateJob();
                            resetIdentifyStateFuture = scheduler.schedule(() -> {
                                updateState(CHANNEL_REPEATER_IDENTIFY, UnDefType.UNDEF);
                            }, IDENTITY_PERIOD_SECONDS, TimeUnit.SECONDS);
                        } else {
                            logger.warn("Unsupported command: {}. Supported commands are: " + COMMAND_IDENTIFY,
                                    command);
                        }
                    }
                    break;
                case CHANNEL_REPEATER_BLINKING_ENABLED:
                    repeaterData = webTargets.enableRepeaterBlinking(repeaterId, OnOffType.ON == command);
                    scheduler.submit(() -> updatePropertyAndStates(repeaterData));
                    break;
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
        } catch (HubException e) {
            logger.warn("Unexpected error: {}", e.getMessage());
        }
    }

    private void cancelResetIdentifyStateJob() {
        ScheduledFuture<?> scheduledJob = resetIdentifyStateFuture;
        if (scheduledJob != null) {
            scheduledJob.cancel(true);
        }
        resetIdentifyStateFuture = null;
    }

    private void scheduleRefreshJob() {
        cancelRefreshJob();
        logger.debug("Scheduling poll for repeater {} now, then every {} minutes", repeaterId,
                REFRESH_INTERVAL_MINUTES);
        this.refreshStatusFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, REFRESH_INTERVAL_MINUTES,
                TimeUnit.MINUTES);
    }

    private void cancelRefreshJob() {
        ScheduledFuture<?> future = this.refreshStatusFuture;
        if (future != null) {
            future.cancel(false);
        }
        this.refreshStatusFuture = null;
    }

    private synchronized void poll() {
        HDPowerViewHubHandler bridge = getBridgeHandler();
        if (bridge == null) {
            logger.warn("Missing bridge handler");
            return;
        }
        HDPowerViewWebTargets webTargets = bridge.getWebTargets();
        try {
            logger.debug("Polling for status information");

            RepeaterData repeaterData = webTargets.getRepeater(repeaterId);
            updatePropertyAndStates(repeaterData);

        } catch (HubInvalidResponseException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.warn("Bridge returned a bad JSON response: {}", e.getMessage());
            } else {
                logger.warn("Bridge returned a bad JSON response: {} -> {}", e.getMessage(), cause.getMessage());
            }
        } catch (HubMaintenanceException e) {
            // exceptions are logged in HDPowerViewWebTargets
        } catch (HubException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, e.getMessage());
        }
    }

    private void updatePropertyAndStates(RepeaterData repeaterData) {
        updateStatus(ThingStatus.ONLINE);

        Firmware firmware = repeaterData.firmware;
        if (firmware != null) {
            logger.debug("Repeater firmware version received: {}", firmware.toString());
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.toString());
        } else {
            logger.warn("Repeater firmware version missing in response");
        }

        Color color = repeaterData.color;
        if (color != null) {
            logger.debug("Repeater color data received: {}", color.toString());
            updateState(CHANNEL_REPEATER_COLOR, HSBType.fromRGB(color.red, color.green, color.red));
            updateState(CHANNEL_REPEATER_BRIGHTNESS, new PercentType(color.brightness));
        }

        updateState(CHANNEL_REPEATER_BLINKING_ENABLED, repeaterData.blinkEnabled ? OnOffType.ON : OnOffType.OFF);
    }
}

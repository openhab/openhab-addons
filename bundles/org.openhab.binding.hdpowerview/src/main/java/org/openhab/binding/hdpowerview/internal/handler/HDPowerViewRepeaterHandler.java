/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.ColorUtil;
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
    private static final int BRIGHTNESS_STEP_PERCENT = 5;
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
        if (command == RefreshType.REFRESH) {
            scheduleRefreshJob();
            return;
        }
        HDPowerViewWebTargets webTargets = bridge.getWebTargets();
        try {
            RepeaterData repeaterData;

            switch (channelUID.getId()) {
                case CHANNEL_REPEATER_COLOR:
                    handleColorCommand(command, webTargets);
                    break;
                case CHANNEL_REPEATER_IDENTIFY:
                    if (command instanceof StringType stringCommand) {
                        if (COMMAND_IDENTIFY.equals(stringCommand.toString())) {
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

    private void handleColorCommand(Command command, HDPowerViewWebTargets webTargets) throws HubException {
        if (command instanceof HSBType hsbCommand) {
            var color = new Color(hsbCommand.getBrightness().intValue(), ColorUtil.hsbTosRgb(hsbCommand));
            RepeaterData repeaterData = webTargets.setRepeaterColor(repeaterId, color);
            scheduler.submit(() -> updatePropertyAndStates(repeaterData));
            return;
        }
        Color currentColor = webTargets.getRepeater(repeaterId).color;
        if (currentColor == null) {
            return;
        }
        Color newColor;
        if (command instanceof PercentType brightnessCommand) {
            newColor = applyBrightnessToColor(currentColor, brightnessCommand.intValue());
        } else if (command instanceof IncreaseDecreaseType increaseDecreaseCommand) {
            int brightness = switch (increaseDecreaseCommand) {
                case INCREASE -> currentColor.brightness + BRIGHTNESS_STEP_PERCENT;
                case DECREASE -> currentColor.brightness - BRIGHTNESS_STEP_PERCENT;
            };
            brightness = brightness < 0 ? 0 : brightness > 100 ? 100 : brightness;
            newColor = applyBrightnessToColor(currentColor, brightness);
        } else if (command instanceof OnOffType) {
            // Light is turned off either by RGB black or zero brightness.
            int brightness;
            if (command == OnOffType.ON) {
                // Turn on with maximum brightness level per default,
                // if no existing brightness level is available.
                brightness = currentColor.brightness > 0 ? currentColor.brightness : 100;
            } else {
                // Turn off by zero brightness to preserve color.
                brightness = 0;
            }
            newColor = applyBrightnessToColor(currentColor, brightness);
        } else {
            logger.warn("Unsupported command: {}", command);
            return;
        }
        RepeaterData repeaterData = webTargets.setRepeaterColor(repeaterId, newColor);
        scheduler.submit(() -> updatePropertyAndStates(repeaterData));
    }

    private Color applyBrightnessToColor(Color currentColor, int brightness) {
        // If light is off by RGB black, then reset to white since otherwise brightness
        // would have no effect; otherwise preserve color.
        return currentColor.isBlack() ? new Color(brightness, java.awt.Color.WHITE)
                : new Color(brightness, currentColor);
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
            future.cancel(true);
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
            HSBType hsb;
            if (color.isBlack()) {
                // Light is off when RGB black, so discard brightness as otherwise it would appear on.
                hsb = HSBType.BLACK;
            } else {
                hsb = HSBType.fromRGB(color.red, color.green, color.blue);
                hsb = new HSBType(hsb.getHue(), hsb.getSaturation(), new PercentType(color.brightness));
            }
            updateState(CHANNEL_REPEATER_COLOR, hsb);
        }

        updateState(CHANNEL_REPEATER_BLINKING_ENABLED, OnOffType.from(repeaterData.blinkEnabled));
    }
}

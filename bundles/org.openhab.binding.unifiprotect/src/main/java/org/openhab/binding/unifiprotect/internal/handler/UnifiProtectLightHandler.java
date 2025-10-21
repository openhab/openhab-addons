/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.UniFiProtectApiClient;
import org.openhab.binding.unifiprotect.internal.api.dto.Light;
import org.openhab.binding.unifiprotect.internal.api.dto.LightDeviceSettings;
import org.openhab.binding.unifiprotect.internal.api.dto.LightModeSettings;
import org.openhab.binding.unifiprotect.internal.api.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.api.dto.events.EventType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Child handler for a UniFi Protect Floodlight.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectLightHandler extends UnifiProtectAbstractDeviceHandler<Light> {

    private final Logger logger = LoggerFactory.getLogger(UnifiProtectLightHandler.class);

    public UnifiProtectLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType eventType) {
        if (event.type == null) {
            return;
        }

        if (event.type == EventType.LIGHT_MOTION) {
            // Trigger PIR motion event and update last motion timestamp
            if (hasChannel(UnifiProtectBindingConstants.CHANNEL_PIR_MOTION)) {
                triggerChannel(new ChannelUID(thing.getUID(), UnifiProtectBindingConstants.CHANNEL_PIR_MOTION));
            }
            if (event.start != null) {
                updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_MOTION, event.start);
            }
        }
    }

    public void updateFromDevice(Light light) {
        super.updateFromDevice(light);
        updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_IS_DARK, light.isDark);
        updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_LIGHT, light.isLightOn);
        if (light.lastMotion != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_MOTION, light.lastMotion);
        }
        LightModeSettings lms = light.lightModeSettings;
        if (lms != null) {
            if (lms.mode != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_LIGHT_MODE, lms.mode.getApiValue());
            }
            if (lms.enableAt != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_ENABLE_AT, lms.enableAt.getApiValue());
            }
        }
        LightDeviceSettings lds = light.lightDeviceSettings;
        if (lds != null) {
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_INDICATOR_ENABLED, lds.isIndicatorEnabled);
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_PIR_DURATION, lds.pirDuration);
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_PIR_SENSITIVITY, lds.pirSensitivity);
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_LED_LEVEL, lds.ledLevel);
        }
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            refreshState(id);
            return;
        }

        UniFiProtectApiClient api = getApiClient();
        if (api == null) {
            return;
        }

        try {
            switch (id) {
                case UnifiProtectBindingConstants.CHANNEL_LIGHT: {
                    boolean value = OnOffType.ON.equals(command);
                    // Force light on/off
                    var patch = UniFiProtectApiClient.buildPatch("isLightForceEnabled", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_LIGHT_MODE: {
                    String value = command.toString();
                    var patch = UniFiProtectApiClient.buildPatch("lightModeSettings.mode", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_ENABLE_AT: {
                    String value = command.toString();
                    var patch = UniFiProtectApiClient.buildPatch("lightModeSettings.enableAt", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_INDICATOR_ENABLED: {
                    boolean value = OnOffType.ON.equals(command);
                    var patch = UniFiProtectApiClient.buildPatch("lightDeviceSettings.isIndicatorEnabled", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PIR_DURATION: {
                    int value;
                    try {
                        value = ((DecimalType) command).intValue();
                    } catch (Exception e) {
                        break;
                    }
                    var patch = UniFiProtectApiClient.buildPatch("lightDeviceSettings.pirDuration", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_PIR_SENSITIVITY: {
                    int value;
                    try {
                        value = ((DecimalType) command).intValue();
                    } catch (Exception e) {
                        break;
                    }
                    value = Math.max(0, Math.min(100, value));
                    var patch = UniFiProtectApiClient.buildPatch("lightDeviceSettings.pirSensitivity", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                case UnifiProtectBindingConstants.CHANNEL_LED_LEVEL: {
                    int value;
                    try {
                        value = ((DecimalType) command).intValue();
                    } catch (Exception e) {
                        break;
                    }
                    value = Math.max(1, Math.min(6, value));
                    var patch = UniFiProtectApiClient.buildPatch("lightDeviceSettings.ledLevel", value);
                    Light updated = api.patchLight(deviceId, patch);
                    updateFromDevice(updated);
                    break;
                }
                default:
                    break;
            }
        } catch (IOException e) {
            logger.debug("Error handling light command", e);
        }
    }
}

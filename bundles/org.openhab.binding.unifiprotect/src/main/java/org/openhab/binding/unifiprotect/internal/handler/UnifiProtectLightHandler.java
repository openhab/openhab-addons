/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Light;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.EventType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Child handler for a UniFi Protect Floodlight.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectLightHandler extends UnifiProtectAbstractDeviceHandler<Light> {

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

    @Override
    public void refreshFromDevice(Light light) {
        super.refreshFromDevice(light);
        updateLightChannels(light);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (command instanceof RefreshType) {
            refreshState(id);
            return;
        }

        UniFiProtectHybridClient api = getApiClient();
        if (api == null) {
            return;
        }

        switch (id) {
            case UnifiProtectBindingConstants.CHANNEL_LIGHT: {
                api.getPrivateClient().setLight(deviceId, OnOffType.ON.equals(command)).whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.debug("Failed to set light", ex);
                    }
                });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_LIGHT_MODE: {
                api.getPrivateClient().setLightMode(deviceId, command.toString(), null).whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.debug("Failed to set light mode", ex);
                    }
                });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_ENABLE_AT: {
                api.getPrivateClient()
                        .updateLight(deviceId, Map.of("lightModeSettings", Map.of("enableAt", command.toString())))
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set enable at", ex);
                            }
                        });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_INDICATOR_ENABLED: {
                api.getPrivateClient()
                        .updateLight(deviceId,
                                Map.of("lightDeviceSettings",
                                        Map.of("isIndicatorEnabled", OnOffType.ON.equals(command))))
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set indicator enabled", ex);
                            }
                        });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_PIR_DURATION: {
                Long value = timeToMilliseconds(command);
                api.getPrivateClient().setLightDuration(deviceId, value != null ? value.intValue() : 0)
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set PIR duration", ex);
                            }
                        });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_PIR_SENSITIVITY: {
                int value;
                try {
                    value = ((DecimalType) command).intValue();
                } catch (Exception e) {
                    logger.debug("Error parsing PIR sensitivity command", e);
                    break;
                }
                value = Math.max(0, Math.min(100, value));
                api.getPrivateClient().setLightPirSensitivity(deviceId, value).whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.debug("Failed to set PIR sensitivity", ex);
                    }
                });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_LED_LEVEL: {
                int value;
                try {
                    value = ((DecimalType) command).intValue();
                } catch (Exception e) {
                    logger.debug("Error parsing LED level command", e);
                    break;
                }
                value = Math.max(1, Math.min(6, value));
                api.getPrivateClient().updateLight(deviceId, Map.of("lightDeviceSettings", Map.of("ledLevel", value)))
                        .whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to set LED level", ex);
                            }
                        });
                break;
            }
            case UnifiProtectBindingConstants.CHANNEL_DEVICE_REBOOT: {
                if (command == OnOffType.ON) {
                    api.getPrivateClient().rebootDevice("light", deviceId).whenComplete((result, ex) -> {
                        if (ex != null) {
                            logger.debug("Failed to reboot light", ex);
                        }
                    });
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
            }
            default:
                break;
        }
    }

    protected void updateLightChannels(Light light) {
        if (light.lightModeSettings != null) {
            if (light.lightModeSettings.mode != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_LIGHT_MODE,
                        light.lightModeSettings.mode.toString());
            }
            if (light.lightModeSettings.enableAt != null) {
                updateStringChannel(UnifiProtectBindingConstants.CHANNEL_ENABLE_AT, light.lightModeSettings.enableAt);
            }
        }
        if (light.lightDeviceSettings != null) {
            updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_INDICATOR_ENABLED,
                    light.lightDeviceSettings.isIndicatorEnabled);
            updateTimeChannel(UnifiProtectBindingConstants.CHANNEL_PIR_DURATION, light.lightDeviceSettings.pirDuration);
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_PIR_SENSITIVITY,
                    light.lightDeviceSettings.pirSensitivity);
            updateIntegerChannel(UnifiProtectBindingConstants.CHANNEL_LED_LEVEL, light.lightDeviceSettings.ledLevel);
        }
        updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_IS_DARK, light.isDark);
        updateBooleanChannel(UnifiProtectBindingConstants.CHANNEL_LIGHT, light.isLightOn);
        if (light.lastMotion != null) {
            updateDateTimeChannel(UnifiProtectBindingConstants.CHANNEL_LAST_MOTION, light.lastMotion.toEpochMilli());
        }
    }
}

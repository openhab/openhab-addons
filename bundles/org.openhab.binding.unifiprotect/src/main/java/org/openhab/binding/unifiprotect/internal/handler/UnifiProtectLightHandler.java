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

import static org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
            if (hasChannel(CHANNEL_PIR_MOTION)) {
                triggerChannel(new ChannelUID(thing.getUID(), CHANNEL_PIR_MOTION));
            }
            if (event.start != null) {
                updateDateTimeChannel(CHANNEL_LAST_MOTION, event.start);
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
            case CHANNEL_LIGHT:
                logOnFailure(api.getPrivateClient().setLight(deviceId, OnOffType.ON.equals(command)), "set light");
                break;
            case CHANNEL_LIGHT_MODE:
                logOnFailure(api.getPrivateClient().setLightMode(deviceId, command.toString(), null), "set light mode");
                break;
            case CHANNEL_ENABLE_AT:
                logOnFailure(api.getPrivateClient().updateLight(deviceId,
                        Map.of("lightModeSettings", Map.of("enableAt", command.toString()))), "set enable at");
                break;
            case CHANNEL_INDICATOR_ENABLED:
                logOnFailure(
                        api.getPrivateClient().updateLight(deviceId,
                                Map.of("lightDeviceSettings",
                                        Map.of("isIndicatorEnabled", OnOffType.ON.equals(command)))),
                        "set indicator enabled");
                break;
            case CHANNEL_PIR_DURATION: {
                Long value = timeToMilliseconds(command);
                logOnFailure(api.getPrivateClient().setLightDuration(deviceId, value != null ? value.intValue() : 0),
                        "set PIR duration");
                break;
            }
            case CHANNEL_PIR_SENSITIVITY:
                if (command instanceof DecimalType decimal) {
                    int value = Math.max(0, Math.min(100, decimal.intValue()));
                    logOnFailure(api.getPrivateClient().setLightPirSensitivity(deviceId, value), "set PIR sensitivity");
                }
                break;
            case CHANNEL_LED_LEVEL:
                if (command instanceof DecimalType decimal) {
                    int value = Math.max(1, Math.min(6, decimal.intValue()));
                    logOnFailure(api.getPrivateClient().updateLight(deviceId,
                            Map.of("lightDeviceSettings", Map.of("ledLevel", value))), "set LED level");
                }
                break;
            case CHANNEL_DEVICE_REBOOT:
                if (command == OnOffType.ON) {
                    logOnFailure(api.getPrivateClient().rebootDevice("light", deviceId), "reboot light");
                    updateState(channelUID, OnOffType.OFF);
                }
                break;
            default:
                break;
        }
    }

    protected void updateLightChannels(Light light) {
        if (light.lightModeSettings != null) {
            if (light.lightModeSettings.mode != null) {
                updateStringChannel(CHANNEL_LIGHT_MODE, light.lightModeSettings.mode.toString());
            }
            if (light.lightModeSettings.enableAt != null) {
                updateStringChannel(CHANNEL_ENABLE_AT, light.lightModeSettings.enableAt);
            }
        }
        if (light.lightDeviceSettings != null) {
            updateBooleanChannel(CHANNEL_INDICATOR_ENABLED, light.lightDeviceSettings.isIndicatorEnabled);
            updateTimeChannel(CHANNEL_PIR_DURATION, light.lightDeviceSettings.pirDuration);
            updateIntegerChannel(CHANNEL_PIR_SENSITIVITY, light.lightDeviceSettings.pirSensitivity);
            updateIntegerChannel(CHANNEL_LED_LEVEL, light.lightDeviceSettings.ledLevel);
        }
        updateBooleanChannel(CHANNEL_IS_DARK, light.isDark);
        updateBooleanChannel(CHANNEL_LIGHT, light.isLightOn);
        if (light.lastMotion != null) {
            updateDateTimeChannel(CHANNEL_LAST_MOTION, light.lastMotion.toEpochMilli());
        }
    }
}

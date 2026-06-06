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

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.priv.dto.devices.Doorlock;
import org.openhab.binding.unifiprotect.internal.api.pub.dto.events.BaseEvent;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link UnifiProtectDoorlockHandler} is responsible for handling commands for UniFi Protect doorlocks.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiProtectDoorlockHandler extends UnifiProtectAbstractDeviceHandler<Doorlock> {

    public UnifiProtectDoorlockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            Doorlock dev = device;
            if (dev != null) {
                updateDoorlockChannels(dev);
            }
            return;
        }

        String channelId = channelUID.getId();
        UniFiProtectHybridClient api = getApiClient();

        if (api == null) {
            logger.debug("API not available for doorlock command");
            return;
        }

        switch (channelId) {
            case CHANNEL_LOCK:
                if (command instanceof OnOffType) {
                    if (command == OnOffType.ON) {
                        logOnFailure(api.getPrivateClient().lockDoorlock(deviceId), "lock doorlock");
                    } else {
                        logOnFailure(api.getPrivateClient().unlockDoorlock(deviceId), "unlock doorlock");
                    }
                }
                break;
            case CHANNEL_CALIBRATE:
                if (command == OnOffType.ON) {
                    logOnFailure(api.getPrivateClient().calibrateDoorlock(deviceId).thenRun(() -> {
                        logger.debug("Doorlock calibration started");
                        scheduler.schedule(() -> updateState(channelUID, OnOffType.OFF), 1, TimeUnit.SECONDS);
                    }), "calibrate doorlock");
                }
                break;
            case CHANNEL_AUTO_CLOSE_TIME:
                if (command instanceof DecimalType decimalCmd) {
                    int seconds = decimalCmd.intValue();
                    logOnFailure(api.getPrivateClient().setDoorlockAutoCloseTime(deviceId, seconds)
                            .thenAccept(updatedDoorlock -> {
                                logger.debug("Set auto-close time to {} seconds", seconds);
                                updateState(channelUID, new DecimalType(seconds));
                            }), "set auto-close time");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void refreshFromDevice(Doorlock device) {
        super.refreshFromDevice(device);
        updateDoorlockChannels(device);
    }

    /**
     * Update doorlock channels from Private API data
     */
    public void updateDoorlockChannels(Doorlock doorlock) {
        // Device properties
        if (doorlock.name != null) {
            updateProperty(PROPERTY_NAME, doorlock.name);
        }
        if (doorlock.marketName != null) {
            updateProperty(PROPERTY_MODEL, doorlock.marketName);
        } else if (doorlock.type != null) {
            updateProperty(PROPERTY_MODEL, doorlock.type);
        }
        if (doorlock.firmwareVersion != null) {
            updateProperty(PROPERTY_FIRMWARE_VERSION, doorlock.firmwareVersion);
        }
        if (doorlock.mac != null) {
            updateProperty(PROPERTY_MAC_ADDRESS, doorlock.mac);
        }
        if (doorlock.host != null) {
            updateProperty(PROPERTY_IP_ADDRESS, doorlock.host);
        }

        // Lock status
        if (doorlock.lockStatus != null) {
            updateStringChannel(CHANNEL_LOCK_STATUS, doorlock.lockStatus);

            // Update lock switch based on status
            boolean isLocked = "closed".equalsIgnoreCase(doorlock.lockStatus)
                    || "locked".equalsIgnoreCase(doorlock.lockStatus);
            updateState(CHANNEL_LOCK, OnOffType.from(isLocked));
        }

        // Battery status
        if (doorlock.batteryStatus != null && doorlock.batteryStatus.percentage != null) {
            updateState(CHANNEL_BATTERY, new DecimalType(doorlock.batteryStatus.percentage));
        }

        // Auto-close time
        if (doorlock.autoCloseTime != null) {
            updateState(CHANNEL_AUTO_CLOSE_TIME, new DecimalType(doorlock.autoCloseTime));
        }
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType type) {
        // Doorlocks don't have events in the public API
        // All updates come through Private API WebSocket
    }
}

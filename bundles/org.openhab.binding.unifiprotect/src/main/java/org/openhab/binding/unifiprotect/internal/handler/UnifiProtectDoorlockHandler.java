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

import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifiprotect.internal.UnifiProtectBindingConstants;
import org.openhab.binding.unifiprotect.internal.api.hybrid.UniFiProtectHybridClient;
import org.openhab.binding.unifiprotect.internal.api.hybrid.devices.DoorklockDevice;
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
public class UnifiProtectDoorlockHandler extends UnifiProtectAbstractDeviceHandler<DoorklockDevice> {

    public UnifiProtectDoorlockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            DoorklockDevice dev = device;
            if (dev != null) {
                updateDoorlockChannels(dev.privateDevice);
            }
            return;
        }

        String channelId = channelUID.getId();
        UniFiProtectHybridClient api = getApiClient();

        if (api == null) {
            logger.debug("API not available for doorlock command");
            return;
        }

        try {
            switch (channelId) {
                case UnifiProtectBindingConstants.CHANNEL_LOCK:
                    if (command instanceof OnOffType) {
                        if (command == OnOffType.ON) {
                            // Lock the door
                            api.getPrivateClient().lockDoorlock(deviceId).whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.debug("Failed to lock doorlock", ex);
                                }
                            });
                        } else {
                            // Unlock the door
                            api.getPrivateClient().unlockDoorlock(deviceId).whenComplete((result, ex) -> {
                                if (ex != null) {
                                    logger.debug("Failed to unlock doorlock", ex);
                                }
                            });
                        }
                    }
                    break;

                case UnifiProtectBindingConstants.CHANNEL_CALIBRATE:
                    if (command == OnOffType.ON) {
                        // Calibrate doorlock (door must be open and lock unlocked)
                        api.getPrivateClient().calibrateDoorlock(deviceId).thenRun(() -> {
                            logger.debug("Doorlock calibration started");
                            // Reset the switch after calibration starts
                            scheduler.schedule(() -> {
                                updateState(channelUID, OnOffType.OFF);
                            }, 1, TimeUnit.SECONDS);
                        }).whenComplete((result, ex) -> {
                            if (ex != null) {
                                logger.debug("Failed to calibrate doorlock", ex);
                            }
                        });
                    }
                    break;

                case UnifiProtectBindingConstants.CHANNEL_AUTO_CLOSE_TIME:
                    if (command instanceof DecimalType decimalCmd) {
                        int seconds = decimalCmd.intValue();
                        api.getPrivateClient().setDoorlockAutoCloseTime(deviceId, seconds)
                                .thenAccept(updatedDoorlock -> {
                                    logger.debug("Set auto-close time to {} seconds", seconds);
                                    // Update the channel with the new value
                                    updateState(channelUID, new DecimalType(seconds));
                                }).whenComplete((result, ex) -> {
                                    if (ex != null) {
                                        logger.debug("Failed to set auto-close time", ex);
                                    }
                                });
                    }
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Error handling command", e);
        }
    }

    @Override
    public void refreshFromDevice(DoorklockDevice device) {
        super.refreshFromDevice(device);
        updateDoorlockChannels(device.privateDevice);
    }

    /**
     * Update doorlock channels from Private API data
     */
    public void updateDoorlockChannels(Doorlock doorlock) {
        // Device properties
        if (doorlock.name != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_NAME, doorlock.name);
        }
        if (doorlock.marketName != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MODEL, doorlock.marketName);
        } else if (doorlock.type != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MODEL, doorlock.type);
        }
        if (doorlock.firmwareVersion != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_FIRMWARE_VERSION, doorlock.firmwareVersion);
        }
        if (doorlock.mac != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_MAC_ADDRESS, doorlock.mac);
        }
        if (doorlock.host != null) {
            updateProperty(UnifiProtectBindingConstants.PROPERTY_IP_ADDRESS, doorlock.host);
        }

        // Lock status
        if (doorlock.lockStatus != null) {
            updateStringChannel(UnifiProtectBindingConstants.CHANNEL_LOCK_STATUS, doorlock.lockStatus);

            // Update lock switch based on status
            boolean isLocked = "closed".equalsIgnoreCase(doorlock.lockStatus)
                    || "locked".equalsIgnoreCase(doorlock.lockStatus);
            updateState(UnifiProtectBindingConstants.CHANNEL_LOCK, OnOffType.from(isLocked));
        }

        // Battery status
        if (doorlock.batteryStatus != null && doorlock.batteryStatus.percentage != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_BATTERY,
                    new DecimalType(doorlock.batteryStatus.percentage));
        }

        // Auto-close time
        if (doorlock.autoCloseTime != null) {
            updateState(UnifiProtectBindingConstants.CHANNEL_AUTO_CLOSE_TIME, new DecimalType(doorlock.autoCloseTime));
        }
    }

    @Override
    public void handleEvent(BaseEvent event, WSEventType type) {
        // Doorlocks don't have events in the public API
        // All updates come through Private API WebSocket
    }
}

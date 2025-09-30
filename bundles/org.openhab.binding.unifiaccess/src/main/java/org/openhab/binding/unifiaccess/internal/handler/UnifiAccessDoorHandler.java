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
package org.openhab.binding.unifiaccess.internal.handler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiaccess.internal.UnifiAccessBindingConstants;
import org.openhab.binding.unifiaccess.internal.api.UniFiAccessApiClient;
import org.openhab.binding.unifiaccess.internal.config.UnifiAccessDeviceConfiguration;
import org.openhab.binding.unifiaccess.internal.dto.Door;
import org.openhab.binding.unifiaccess.internal.dto.DoorState;
import org.openhab.binding.unifiaccess.internal.dto.Image;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationState;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationUpdateV2Data;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewChangeData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Thing handler for UniFi Access Door things.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessDoorHandler extends BaseThingHandler {

    public static final String CONFIG_DOOR_ID = UnifiAccessBindingConstants.CONFIG_DEVICE_ID;

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessDoorHandler.class);
    private @Nullable Door door;
    public String deviceId = "";

    public UnifiAccessDoorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        deviceId = getConfigAs(UnifiAccessDeviceConfiguration.class).deviceId;
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::refreshAll);
            return;
        }
        String channelId = channelUID.getId();
        UnifiAccessBridgeHandler bridge = getBridgeHandler();
        UniFiAccessApiClient api = bridge != null ? bridge.getApiClient() : null;
        if (api == null) {
            return;
        }
        try {
            switch (channelId) {
                case UnifiAccessBindingConstants.CHANNEL_LOCK:
                    if (command instanceof OnOffType onOff) {
                        if (onOff == OnOffType.ON) {
                            api.lockEarly(deviceId);
                        } else {
                            api.unlockDoor(deviceId, null, null, null);
                        }
                    }
                    break;
                case UnifiAccessBindingConstants.CHANNEL_KEEP_UNLOCKED:
                    if (command instanceof OnOffType onOff) {
                        if (onOff == OnOffType.ON) {
                            api.keepDoorUnlocked(deviceId);
                        } else {
                            api.resetDoorLockRule(deviceId);
                        }
                    }
                    break;
                case UnifiAccessBindingConstants.CHANNEL_KEEP_LOCKED:
                    if (command instanceof OnOffType onOff) {
                        if (onOff == OnOffType.ON) {
                            api.keepDoorLocked(deviceId);
                        } else {
                            api.resetDoorLockRule(deviceId);
                        }
                    }
                    break;
                case UnifiAccessBindingConstants.CHANNEL_UNLOCK_MINUTES:
                    int minutes = Integer.parseInt(command.toString());
                    if (minutes > 0) {
                        api.unlockForMinutes(deviceId, minutes);
                    } else {
                        api.resetDoorLockRule(deviceId);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.debug("Command failed for door {}: {}", deviceId, e.getMessage());
        }
    }

    public void updateFromDoor(Door door) {
        logger.debug("Updating door state from door: {}", door);
        this.door = door;
        if (getThing().getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        refreshAll();
    }

    public void handleLocationState(LocationState locationState) {
        Door door = this.door;
        if (door == null) {
            return;
        }
        if (locationState.lock != null) {
            door.doorLockRelayStatus = locationState.lock;
            updateLock(locationState.lock);
        }
        if (locationState.dps != null) {
            door.doorPositionStatus = locationState.dps;
            updatePosition(locationState.dps);
        }
        if (locationState.remainUnlock != null) {
            DoorState.DoorLockRuleType rule = locationState.remainUnlock.type;
            List<String> lockChannels = new ArrayList<>(Arrays.asList(UnifiAccessBindingConstants.CHANNEL_KEEP_UNLOCKED,
                    UnifiAccessBindingConstants.CHANNEL_KEEP_LOCKED,
                    UnifiAccessBindingConstants.CHANNEL_UNLOCK_MINUTES));
            switch (rule) {
                case KEEP_UNLOCK:
                    updateState(UnifiAccessBindingConstants.CHANNEL_KEEP_UNLOCKED, OnOffType.ON);
                    lockChannels.remove(UnifiAccessBindingConstants.CHANNEL_KEEP_UNLOCKED);
                    break;
                case KEEP_LOCK:
                    updateState(UnifiAccessBindingConstants.CHANNEL_KEEP_LOCKED, OnOffType.ON);
                    lockChannels.remove(UnifiAccessBindingConstants.CHANNEL_KEEP_LOCKED);
                    break;
                case CUSTOM:
                    updateState(UnifiAccessBindingConstants.CHANNEL_UNLOCK_MINUTES,
                            new DecimalType(locationState.remainUnlock.until));
                    lockChannels.remove(UnifiAccessBindingConstants.CHANNEL_UNLOCK_MINUTES);
                    break;
                default:
                    break;
            }
            lockChannels.forEach(channel -> {
                if (UnifiAccessBindingConstants.CHANNEL_UNLOCK_MINUTES.equals(channel)) {
                    updateState(channel, new DecimalType(0));
                } else {
                    updateState(channel, OnOffType.OFF);
                }
            });
        }
    }

    public void handleLocationUpdateV2(LocationUpdateV2Data locationUpdate) {
        Door door = this.door;
        if (door == null) {
            return;
        }
        if (locationUpdate.state != null) {
            door.doorLockRelayStatus = locationUpdate.state.lock;
            updateLock(locationUpdate.state.lock);
            door.doorPositionStatus = locationUpdate.state.dps;
            updatePosition(locationUpdate.state.dps);
            if (locationUpdate.thumbnail != null) {
                UnifiAccessBridgeHandler bridge = getBridgeHandler();
                UniFiAccessApiClient api = bridge != null ? bridge.getApiClient() : null;
                if (api == null) {
                    return;
                }
                try {
                    Image thumbnail = api.getDoorThumbnail(locationUpdate.thumbnail.url);
                    updateState(UnifiAccessBindingConstants.CHANNEL_DOOR_THUMBNAIL,
                            new RawType(thumbnail.data, thumbnail.mediaType));
                } catch (Exception e) {
                    logger.debug("Failed to get door thumbnail for door {}: {}", door.id, e.getMessage());
                }
            }
        }
    }

    public void handleRemoteUnlock(Notification.RemoteUnlockData remoteUnlock) {
        Door door = this.door;
        if (door == null) {
            return;
        }
        setLastUnlock(remoteUnlock.fullName, System.currentTimeMillis());
        door.doorLockRelayStatus = DoorState.LockState.UNLOCKED;

        try {
            String payload = new Gson()
                    .toJson(Map.of("deviceId", remoteUnlock.uniqueId, "name", remoteUnlock.name, "fullName",
                            remoteUnlock.fullName, "level", remoteUnlock.level, "workTimeId", remoteUnlock.workTimeId));
            triggerChannel(UnifiAccessBindingConstants.CHANNEL_DOOR_REMOTE_UNLOCK, payload);
        } catch (Exception ignored) {
        }
    }

    public void handleDoorbellStatus(RemoteViewChangeData change) {
        try {
            String event = change.reason != null ? change.reason.name() : "UNKNOWN";
            triggerChannel(UnifiAccessBindingConstants.CHANNEL_DOORBELL_STATUS, event);
        } catch (Exception ignored) {
        }
    }

    public void triggerAccessAttemptSuccess(String payload) {
        try {
            triggerChannel(UnifiAccessBindingConstants.CHANNEL_DOOR_ACCESS_ATTEMPT_SUCCESS, payload);
        } catch (Exception ignored) {
        }
    }

    public void triggerAccessAttemptFailure(String payload) {
        try {
            triggerChannel(UnifiAccessBindingConstants.CHANNEL_DOOR_ACCESS_ATTEMPT_FAILURE, payload);
        } catch (Exception ignored) {
        }
    }

    public void triggerLogInsight(String payload) {
        try {
            triggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG_INSIGHT, payload);
        } catch (Exception ignored) {
        }
    }

    private void updateLock(DoorState.LockState lock) {
        updateState(UnifiAccessBindingConstants.CHANNEL_LOCK,
                lock == DoorState.LockState.LOCKED ? OnOffType.ON : OnOffType.OFF);
    }

    private void updatePosition(DoorState.DoorPosition position) {
        updateState(UnifiAccessBindingConstants.CHANNEL_DOOR_POSITION,
                position == DoorState.DoorPosition.OPEN ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
    }

    private void setLastUnlock(@Nullable String actorName, long whenEpochMs) {
        if (actorName != null) {
            updateState(UnifiAccessBindingConstants.CHANNEL_LAST_ACTOR, StringType.valueOf(actorName));
        }
        if (whenEpochMs > 0) {
            updateState(UnifiAccessBindingConstants.CHANNEL_LAST_UNLOCK,
                    new DateTimeType(Instant.ofEpochMilli(whenEpochMs)));
        }
    }

    private void refreshAll() {
        Door door = this.door;
        if (door == null) {
            return;
        }
        logger.debug("Updating door state from door: {}", door);
        if (door.doorLockRelayStatus != null) {
            updateLock(door.doorLockRelayStatus);
        }
        if (door.doorPositionStatus != null) {
            updatePosition(door.doorPositionStatus);
        }
    }

    private @Nullable UnifiAccessBridgeHandler getBridgeHandler() {
        var b = getBridge();
        if (b == null) {
            return null;
        }
        var h = b.getHandler();
        return (h instanceof UnifiAccessBridgeHandler handler) ? handler : null;
    }
}

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
package org.openhab.binding.unifiaccess.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiaccess.internal.UnifiAccessBindingConstants;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.Notification.DeviceUpdateData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.DeviceUpdateV2Data;
import org.openhab.binding.unifiaccess.internal.dto.Notification.InsightLogsAddData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationUpdateV2Data;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LogsAddData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteUnlockData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewChangeData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.RemoteViewData;
import org.openhab.core.library.types.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Routes WebSocket notification events from the UniFi Access controller to the appropriate thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class UnifiAccessNotificationRouter {

    private final Logger logger = LoggerFactory.getLogger(UnifiAccessNotificationRouter.class);
    private final Gson gson;
    private final UnifiAccessBridgeHandler bridgeHandler;
    private final Map<String, String> remoteViewRequestToDeviceId;

    public UnifiAccessNotificationRouter(Gson gson, UnifiAccessBridgeHandler bridgeHandler,
            Map<String, String> remoteViewRequestToDeviceId) {
        this.gson = gson;
        this.bridgeHandler = bridgeHandler;
        this.remoteViewRequestToDeviceId = remoteViewRequestToDeviceId;
    }

    /**
     * Dispatches a WebSocket notification to the appropriate handler based on event type.
     *
     * @param notification the notification received from the WebSocket stream
     */
    public void routeNotification(Notification notification) {
        logger.debug("Notification event: {} data: {}", notification.event, notification.data);
        try {
            switch (notification.event) {
                // When a doorbell rings
                case "access.remote_view":
                    handleRemoteViewEvent(notification);
                    break;
                // Doorbell status change
                case "access.remote_view.change":
                    handleRemoteViewChangeEvent(notification);
                    break;
                // Remote door unlock by admin
                case "access.data.device.remote_unlock":
                    handleRemoteUnlockEvent(notification);
                    break;
                case "access.data.device.update":
                    handleDeviceUpdateEvent(notification);
                    break;
                case "access.data.v2.device.update":
                    handleDeviceUpdateV2Event(notification);
                    break;
                case "access.data.v2.location.update":
                    handleLocationUpdateV2Event(notification);
                    break;
                case "access.logs.insights.add":
                    handleLogInsightEvent(notification);
                    break;
                case "access.logs.add":
                    handleLogAddEvent(notification);
                    break;
                case "access.data.device.delete":
                    handleDeviceDeleteEvent(notification);
                    break;
                case "access.base.info":
                    break;
                case "access.hw.door_bell":
                    handleHwDoorbellEvent(notification);
                    break;
                default:
                    // leave as raw
                    break;
            }
        } catch (Exception ex) {
            logger.debug("Failed to parse typed notification for {}: {}", notification.event, ex.getMessage());
        }
    }

    private void handleRemoteViewEvent(Notification notification) {
        RemoteViewData rv = notification.dataAsRemoteView(gson);
        if (rv == null) {
            return;
        }
        try {
            if (rv.requestId != null && rv.deviceId != null) {
                remoteViewRequestToDeviceId.put(rv.requestId, rv.deviceId);
            }
            if (rv.clearRequestId != null && rv.deviceId != null) {
                remoteViewRequestToDeviceId.put(rv.clearRequestId, rv.deviceId);
            }
            handleRemoteView(rv);
        } catch (Exception ex) {
            logger.debug("Failed to handle remote_view: {}", ex.getMessage());
        }
    }

    private void handleRemoteViewChangeEvent(Notification notification) {
        RemoteViewChangeData rvc = notification.dataAsRemoteViewChange(gson);
        if (rvc == null) {
            return;
        }
        try {
            // Route doorbell status to device and door handlers via request ID mapping
            if (rvc.remoteCallRequestId != null) {
                String deviceId = remoteViewRequestToDeviceId.get(rvc.remoteCallRequestId);
                if (deviceId != null) {
                    UnifiAccessDeviceHandler dh = bridgeHandler.getDeviceHandler(deviceId);
                    if (dh != null) {
                        dh.handleRemoteViewChange(rvc);
                    }
                    UnifiAccessDoorHandler d = bridgeHandler.getDoorHandler(deviceId);
                    if (d != null) {
                        d.handleDoorbellStatus(rvc);
                    }
                }
            }
        } catch (Exception ex) {
            logger.debug("Failed to handle remote_view.change: {}", ex.getMessage());
        }
    }

    private void handleRemoteUnlockEvent(Notification notification) {
        RemoteUnlockData ru = notification.dataAsRemoteUnlock(gson);
        logger.debug("Device remote unlock: {}", ru.name);
        handleRemoteUnlock(ru);
    }

    private void handleHwDoorbellEvent(Notification notification) {
        Notification.DoorBellData db = notification.dataAsDoorBell(gson);
        if (db == null || db.deviceId == null) {
            return;
        }
        UnifiAccessDeviceHandler dh = bridgeHandler.getDeviceHandler(db.deviceId);
        if (dh != null) {
            dh.handleHwDoorbell();
        }
    }

    private void handleDeviceUpdateEvent(Notification notification) {
        DeviceUpdateData du = notification.dataAsDeviceUpdate(gson);
        if (du == null) {
            return;
        }
        try {
            handleDeviceUpdate(du);
        } catch (Exception ex) {
            logger.debug("Failed to handle device update: {}", ex.getMessage());
        }
    }

    private void handleDeviceUpdateV2Event(Notification notification) {
        DeviceUpdateV2Data du2 = notification.dataAsDeviceUpdateV2(gson);
        if (du2 == null) {
            return;
        }
        try {
            handleDeviceUpdateV2(du2);
        } catch (Exception ex) {
            logger.debug("Failed to handle device update: {}", ex.getMessage());
        }
    }

    private void handleLocationUpdateV2Event(Notification notification) {
        LocationUpdateV2Data lu2 = notification.dataAsLocationUpdateV2(gson);
        if (lu2 == null) {
            return;
        }
        try {
            handleLocationUpdateV2(lu2);
        } catch (Exception ex) {
            logger.debug("Failed to handle location update: {}", ex.getMessage());
        }
    }

    private void handleLogInsightEvent(Notification notification) {
        InsightLogsAddData data = notification.dataAsInsightLogsAdd(gson);
        if (data == null) {
            return;
        }
        String payload = UnifiAccessLogPayloadBuilder.buildInsightPayload(gson, data);
        // bridge-level trigger
        bridgeHandler.fireTriggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG_INSIGHT, payload);

        // route to specific door if referenced
        String doorId = data.metadata != null && data.metadata.door != null ? data.metadata.door.id : null;
        if (doorId != null) {
            UnifiAccessDoorHandler dh = bridgeHandler.getDoorHandler(doorId);
            if (dh != null) {
                dh.triggerLogInsight(payload);
            }
        }
        // route to specific device if referenced
        String deviceId = data.metadata != null && data.metadata.device != null ? data.metadata.device.id : null;
        if (deviceId != null) {
            UnifiAccessDeviceHandler d = bridgeHandler.getDeviceHandler(deviceId);
            if (d != null) {
                d.triggerLogInsight(payload);
            }
        }
    }

    private void handleLogAddEvent(Notification notification) {
        LogsAddData data = notification.dataAsLogsAdd(gson);
        if (data == null || data.source == null) {
            return;
        }
        String payload = UnifiAccessLogPayloadBuilder.buildLogPayload(gson, data);
        bridgeHandler.fireTriggerChannel(UnifiAccessBindingConstants.CHANNEL_BRIDGE_LOG, payload);

        // door-level success/failure triggers
        @Nullable
        String doorId = (data.source.target == null) ? null
                : data.source.target.stream().filter(t -> "door".equalsIgnoreCase(t.type)).map(t -> t.id).findFirst()
                        .orElse(null);

        if (doorId != null) {
            boolean isSuccess = data.source.event != null && "ACCESS".equalsIgnoreCase(data.source.event.result);
            String accessPayload = UnifiAccessLogPayloadBuilder.buildAccessAttemptPayload(gson, data);
            UnifiAccessDoorHandler dh = bridgeHandler.getDoorHandler(doorId);
            if (dh != null) {
                if (isSuccess) {
                    dh.triggerAccessAttemptSuccess(accessPayload);
                } else {
                    dh.triggerAccessAttemptFailure(accessPayload);
                }
            }
        }
    }

    private void handleRemoteUnlock(RemoteUnlockData data) {
        UnifiAccessDoorHandler dh = bridgeHandler.getDoorHandler(data.uniqueId);
        if (dh != null) {
            dh.handleRemoteUnlock(data);
        }
    }

    private void handleRemoteView(RemoteViewData rv) {
        UnifiAccessDeviceHandler dh = bridgeHandler.getDeviceHandler(rv.deviceId);
        if (dh != null) {
            dh.handleRemoteView(rv);
        }
    }

    private void handleDeviceUpdate(DeviceUpdateData updateData) {
        UnifiAccessBaseHandler bh = bridgeHandler.getBaseHandler(updateData.uniqueId);
        if (bh != null) {
            bh.handleDeviceUpdate(updateData);
        }
    }

    private void handleDeviceUpdateV2(DeviceUpdateV2Data updateData) {
        String locId = updateData.locationId;
        if (locId == null || locId.isEmpty()) {
            return;
        }
        UnifiAccessBaseHandler bh = bridgeHandler.getBaseHandler(locId);
        if (bh != null) {
            bh.handleDeviceUpdateV2(updateData);
        }
    }

    private void handleDeviceDeleteEvent(Notification notification) {
        String deviceId = notification.eventObjectId;
        if (deviceId != null) {
            UnifiAccessBaseHandler bh = bridgeHandler.getBaseHandler(deviceId);
            if (bh != null) {
                bh.updateStatus(org.openhab.core.thing.ThingStatus.OFFLINE,
                        org.openhab.core.thing.ThingStatusDetail.GONE, "Device removed from controller");
            }
        }
    }

    private void handleLocationUpdateV2(LocationUpdateV2Data lu2) {
        // Update bridge-level emergency status from location state
        if (lu2.state != null && lu2.state.emergency != null) {
            String sw = lu2.state.emergency.software;
            String hw = lu2.state.emergency.hardware;
            String status = "normal";
            if ("lockdown".equalsIgnoreCase(sw) || "lockdown".equalsIgnoreCase(hw)) {
                status = "lockdown";
            } else if ("evacuation".equalsIgnoreCase(sw) || "evacuation".equalsIgnoreCase(hw)) {
                status = "evacuation";
            }
            bridgeHandler.updateBridgeState(UnifiAccessBindingConstants.CHANNEL_BRIDGE_EMERGENCY_STATUS,
                    new StringType(status));
        }
        // Route to door handler by location ID (lu2.id is the door/location ID)
        if (lu2.id != null) {
            UnifiAccessDoorHandler doorHandler = bridgeHandler.getDoorHandler(lu2.id);
            if (doorHandler != null) {
                doorHandler.handleLocationUpdateV2(lu2);
            }
        }
        // Forward to matching device handlers by device ids under this location
        if (lu2.state != null && lu2.deviceIds != null) {
            for (String deviceId : lu2.deviceIds) {
                UnifiAccessBaseHandler bh = bridgeHandler.getBaseHandler(deviceId);
                if (bh != null) {
                    bh.handleLocationUpdateV2(lu2);
                }
            }
        }
    }
}

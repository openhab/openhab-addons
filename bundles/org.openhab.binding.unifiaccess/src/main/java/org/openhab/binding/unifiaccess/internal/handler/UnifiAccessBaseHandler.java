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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiaccess.internal.api.UniFiAccessApiClient;
import org.openhab.binding.unifiaccess.internal.dto.Notification;
import org.openhab.binding.unifiaccess.internal.dto.Notification.DeviceUpdateData;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationState;
import org.openhab.binding.unifiaccess.internal.dto.Notification.LocationUpdateV2Data;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

/**
 * Base class for all UniFi Access device and door handlers.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class UnifiAccessBaseHandler extends BaseThingHandler {
    protected Map<String, State> stateCache = new HashMap<>();
    /* This is the universal ID for the device or door, will match locationId as well in API responses */
    protected String deviceId = "";

    public UnifiAccessBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateState(String channelUID, State state) {
        super.updateState(channelUID, state);
        stateCache.put(channelUID, state);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    protected void refreshState(String channelId) {
        State state = stateCache.get(channelId);
        if (state != null) {
            super.updateState(channelId, state);
        }
    }

    protected @Nullable UnifiAccessBridgeHandler getBridgeHandler() {
        var b = getBridge();
        if (b == null) {
            return null;
        }
        var h = b.getHandler();
        return (h instanceof UnifiAccessBridgeHandler) ? (UnifiAccessBridgeHandler) h : null;
    }

    protected @Nullable UniFiAccessApiClient getApiClient() {
        UnifiAccessBridgeHandler bridge = getBridgeHandler();
        return bridge != null ? bridge.getApiClient() : null;
    }

    // updates from the WebSocket

    protected void handleDeviceUpdate(DeviceUpdateData updateData) {
        if (!updateData.isConnected) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Device reported as offline");
        }
    }

    protected void handleDeviceUpdateV2(Notification.DeviceUpdateV2Data updateData) {
        if (!updateData.online) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Device reported as offline");
        }
    }

    protected void handleLocationUpdateV2(LocationUpdateV2Data locationUpdate) {
        if (locationUpdate.state != null) {
            handleLocationState(locationUpdate.state);
        }
    }

    protected abstract void handleLocationState(LocationState locationState);
}

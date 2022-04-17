/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.elroconnects.internal.handler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevice;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * The {@link ElroConnectsDeviceHandler} represents the thing handler for an ELRO Connects device.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class ElroConnectsDeviceHandler extends BaseThingHandler {

    protected int deviceId;

    public ElroConnectsDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        ElroConnectsDeviceConfiguration config = getConfigAs(ElroConnectsDeviceConfiguration.class);
        deviceId = config.deviceId;

        ElroConnectsBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler != null) {
            bridgeHandler.setDeviceHandler(deviceId, this);
            updateProperties(bridgeHandler);
            refreshChannels(bridgeHandler);
        }
    }

    @Override
    public void dispose() {
        ElroConnectsBridgeHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler != null) {
            bridgeHandler.unsetDeviceHandler(deviceId, this);
        }
    }

    /**
     * Get the bridge handler for this thing handler.
     *
     * @return {@link ElroConnectsBridgeHandler}, null if no bridge handler set
     */
    protected @Nullable ElroConnectsBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No bridge defined for device " + String.valueOf(deviceId));
            return null;
        }

        ElroConnectsBridgeHandler bridgeHandler = (ElroConnectsBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No bridge handler defined for device " + String.valueOf(deviceId));
            return null;
        }

        return bridgeHandler;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        ElroConnectsBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            if (command instanceof RefreshType) {
                refreshChannels(bridgeHandler);
            }
        }
    }

    /**
     * Update thing properties.
     *
     * @param bridgeHandler
     */
    protected void updateProperties(ElroConnectsBridgeHandler bridgeHandler) {
        ElroConnectsDevice device = bridgeHandler.getDevice(deviceId);
        if (device != null) {
            Map<String, String> properties = new HashMap<>();
            properties.put("deviceType", ElroConnectsUtil.stringOrEmpty(device.getDeviceType()));
            thing.setProperties(properties);
        }
    }

    /**
     * Refresh all thing channels.
     *
     * @param bridgeHandler
     */
    protected void refreshChannels(ElroConnectsBridgeHandler bridgeHandler) {
        ElroConnectsDevice device = bridgeHandler.getDevice(deviceId);
        if (device != null) {
            device.updateState();
        }
    }

    @Override
    public void updateState(String channelID, State state) {
        super.updateState(channelID, state);
    }

    @Override
    public void updateStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail,
            @Nullable String description) {
        super.updateStatus(thingStatus, thingStatusDetail, description);
    }

    @Override
    public void updateStatus(ThingStatus thingStatus) {
        super.updateStatus(thingStatus);
    }

    /**
     * Method to be called when an alarm event is received from the K1 hub. This should trigger a trigger channel. The
     * method should be implemented in subclasses for the appropriate trigger channel if it applies.
     */
    public void triggerAlarm() {
        // nothing by default
    }
}

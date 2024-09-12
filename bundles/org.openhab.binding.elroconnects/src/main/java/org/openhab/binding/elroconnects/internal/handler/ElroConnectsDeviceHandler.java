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
package org.openhab.binding.elroconnects.internal.handler;

import static org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.elroconnects.internal.ElroConnectsBindingConstants.ElroDeviceType;
import org.openhab.binding.elroconnects.internal.devices.ElroConnectsDevice;
import org.openhab.binding.elroconnects.internal.util.ElroConnectsUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
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
        if (bridgeHandler == null) {
            // Thing status has already been updated in getBridgeHandler()
            return;
        }

        if (bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            ElroConnectsDevice device = bridgeHandler.getDevice(deviceId);
            if (device != null) {
                ElroDeviceType deviceType = TYPE_MAP.get(device.getDeviceType());
                if ((deviceType == null) || !thing.getThingTypeUID().equals(THING_TYPE_MAP.get(deviceType))) {
                    String msg = String.format("@text/offline.invalid-device-type [ \"%s\" ]", deviceType);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                } else {
                    bridgeHandler.setDeviceHandler(deviceId, this);
                    updateProperties(bridgeHandler);
                    updateDeviceName(bridgeHandler);
                    refreshChannels(bridgeHandler);
                }
            } else {
                String msg = String.format("@text/offline.invalid-device-id [ \"%d\" ]", deviceId);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
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
            String msg = String.format("@text/offline.no-bridge [ \"%d\" ]", deviceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return null;
        }

        ElroConnectsBridgeHandler bridgeHandler = (ElroConnectsBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            String msg = String.format("@text/offline.no-bridge-handler [ \"%d\" ]", deviceId);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
            return null;
        }

        return bridgeHandler;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initialize();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
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

    protected void updateDeviceName(ElroConnectsBridgeHandler bridgeHandler) {
        ElroConnectsDevice device = bridgeHandler.getDevice(deviceId);
        String deviceName = thing.getLabel();
        if ((device != null) && (deviceName != null)) {
            device.updateDeviceName(deviceName);
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

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
package org.openhab.binding.tuya.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tuya.internal.TuyaDynamicCommandDescriptionProvider;
import org.openhab.binding.tuya.internal.TuyaDynamicStateDescriptionProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;

import com.google.gson.Gson;

/**
 * The {@link TuyaSubDeviceHandler} handles commands and state updates for a device that is reached through a gateway.
 * It has no connection of its own, all traffic is relayed by the {@link TuyaGatewayHandler} of its bridge and tagged
 * with the sub-device node id.
 *
 * @author Maciej Jarzebowski - Initial contribution
 */
@NonNullByDefault
public class TuyaSubDeviceHandler extends BaseTuyaDeviceHandler {

    public TuyaSubDeviceHandler(Thing thing, Gson gson,
            TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, gson, dynamicCommandDescriptionProvider, dynamicStateDescriptionProvider);
    }

    /**
     * @return the node id identifying this device towards its gateway, empty if it is not configured
     */
    String getSubDeviceId() {
        return configuration.subDeviceId;
    }

    @Override
    protected void initializeTransport() {
        if (configuration.subDeviceId.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.missing-sub-device-id");
            return;
        }

        // The gateway calls gatewayConnectionStatus() once this handler has been registered as its child.
        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Called by the gateway when the state of its connection changed, and once when this handler is registered as a
     * child of the gateway.
     *
     * @param status whether the gateway is connected
     * @param initialDelay how long to wait before querying the device, in milliseconds
     */
    void gatewayConnectionStatus(boolean status, int initialDelay) {
        if (status) {
            updateStatus(ThingStatus.ONLINE);

            onCommunicationEstablished(initialDelay);
            return;
        }

        onCommunicationLost();

        Bridge bridge = getBridge();
        if (bridge != null && bridge.getStatus() == ThingStatus.OFFLINE) {
            // The gateway cannot connect at all, e.g. because its IP address is not known yet
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        } else {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "@text/online.wait-for-device");
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);

            onCommunicationLost();
        }
        // Coming back online is driven by the gateway connection, see gatewayConnectionStatus().
    }

    @Override
    protected boolean canCommunicate() {
        TuyaGatewayHandler gatewayHandler = getGatewayHandler();
        return gatewayHandler != null && gatewayHandler.isConnected();
    }

    @Override
    protected void sendCommand(Map<Integer, @Nullable Object> command) {
        TuyaGatewayHandler gatewayHandler = getGatewayHandler();
        if (gatewayHandler != null) {
            gatewayHandler.sendCommand(configuration.subDeviceId, command);
        } else {
            logger.warn("{}: Setting {} failed. Gateway is not available.", thing.getUID().getId(), command);
        }
    }

    @Override
    protected void requestStatus() {
        TuyaGatewayHandler gatewayHandler = getGatewayHandler();
        if (gatewayHandler != null) {
            gatewayHandler.requestStatus(configuration.subDeviceId);
        }
    }

    @Override
    protected void refreshStatus() {
        // Gateways relay DP_QUERY for their sub-devices. DP_REFRESH is not defined for sub-devices, so a full query is
        // the only way to get up-to-date measurements.
        requestStatus();
    }

    @Override
    protected boolean refreshRepeatsFullQuery() {
        return true;
    }

    private @Nullable TuyaGatewayHandler getGatewayHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }

        return bridge.getHandler() instanceof TuyaGatewayHandler gatewayHandler ? gatewayHandler : null;
    }
}

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
package org.openhab.binding.insteon.internal.handler;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.insteon.internal.InsteonBinding;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;

/**
 * The {@link InsteonThingHandler} is the base handler for insteon things.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public abstract class InsteonThingHandler extends InsteonBaseHandler {

    public InsteonThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public InsteonBinding getInsteonBinding() {
        return getInsteonBridgeHandler().getInsteonBinding();
    }

    protected InsteonBridgeConfiguration getInsteonBridgeConfig() {
        return getInsteonBridgeHandler().getInsteonBridgeConfig();
    }

    protected InsteonBridgeHandler getInsteonBridgeHandler() {
        Bridge bridge = getBridge();
        Objects.requireNonNull(bridge);
        InsteonBridgeHandler handler = (InsteonBridgeHandler) bridge.getHandler();
        Objects.requireNonNull(handler);
        return handler;
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.CONFIGURATION_ERROR) {
            updateStatus();
        }
    }

    public abstract void bridgeThingUpdated();

    @Override
    public void updateStatus() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge selected.");
            return;
        }

        if (bridge.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        if (!getInsteonBinding().isModemDBComplete()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for modem database.");
            return;
        }

        InsteonDevice device = getDevice();
        if (device == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unable to determine device.");
            return;
        }

        if (!device.hasModemDBEntry() && !device.isModem() && !device.getAddress().isX10()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Device not found in modem database.");
            return;
        }

        if (device.isNotResponding() && !device.isBatteryPowered()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not responding.");
            return;
        }

        if (device.getProductData() == null) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for product data.");
            return;
        }

        if (device.getType() == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unsupported device.");
            return;
        }

        if (!device.getLinkDB().isComplete() && !device.isModem() && !device.getAddress().isX10()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.CONFIGURATION_PENDING, "Waiting for link database.");
            return;
        }

        if (getThing().getChannels().isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No available channels.");
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }
}

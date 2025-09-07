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
package org.openhab.binding.meross.internal.handler;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.config.MerossDeviceConfiguration;
import org.openhab.binding.meross.internal.exception.MerossMqttConnackException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;

/**
 * The {@link MerossDeviceHandler} is the abstract base class for Meross device handlers
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public abstract class MerossDeviceHandler extends BaseThingHandler {

    protected @NonNullByDefault({}) MerossDeviceConfiguration config;
    protected @Nullable MerossBridgeHandler merossBridgeHandler;

    public MerossDeviceHandler(Thing thing) {
        super(thing);
    }

    public void initializeDevice() {
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MerossBridgeHandler merossBridgeHandler)) {
            return;
        }

        this.merossBridgeHandler = merossBridgeHandler;
        var merossHttpConnector = merossBridgeHandler.getMerossHttpConnector();
        if (merossHttpConnector == null) {
            return;
        }

        String deviceUUID;
        try {
            Thing thing = getThing();
            String label = thing.getLabel();
            if (config.name.isEmpty()) {
                if (label != null) {
                    config.name = label;
                }
            }
            deviceUUID = merossHttpConnector.getDevUUIDByDevName(config.name);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }
        if (deviceUUID.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device found with name " + config.name);
            return;
        }
        var manager = MerossManager.newMerossManager(merossHttpConnector);
        try {
            int onlineStatus = manager.onlineStatus(config.name);
            initializeThing(onlineStatus);
        } catch (IOException | MerossMqttConnackException e) {
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        initializeBridge(bridge.getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            initializeBridge(bridgeStatusInfo.getStatus());
        }
    }

    public void initializeBridge(ThingStatus bridgeStatus) {
        if (bridgeStatus == ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    public void initializeThing(int status) {
        if (status == MerossEnum.OnlineStatus.UNKNOWN.value() || status == MerossEnum.OnlineStatus.NOT_ONLINE.value()
                || status == MerossEnum.OnlineStatus.UPGRADING.value()) {
            updateStatus(ThingStatus.UNKNOWN);
        } else if (status == MerossEnum.OnlineStatus.OFFLINE.value()) {
            updateStatus(ThingStatus.OFFLINE);
        } else if (status == MerossEnum.OnlineStatus.ONLINE.value()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}

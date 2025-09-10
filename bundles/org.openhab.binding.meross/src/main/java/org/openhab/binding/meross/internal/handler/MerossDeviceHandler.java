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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossManager;
import org.openhab.binding.meross.internal.api.MerossMqttConnector;
import org.openhab.binding.meross.internal.config.MerossDeviceConfiguration;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;

/**
 * The {@link MerossDeviceHandler} is the abstract base class for Meross device handlers
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public abstract class MerossDeviceHandler extends BaseThingHandler {

    protected @NonNullByDefault({}) MerossDeviceConfiguration config;
    protected @Nullable MerossBridgeHandler merossBridgeHandler;
    protected @Nullable MerossManager manager;

    public MerossDeviceHandler(Thing thing) {
        super(thing);
    }

    public void initializeDevice() {
        Bridge bridge = getBridge();
        if (bridge == null || !(bridge.getHandler() instanceof MerossBridgeHandler merossBridgeHandler)) {
            return;
        }
        this.merossBridgeHandler = merossBridgeHandler;
        MerossMqttConnector mqttConnector = merossBridgeHandler.getMerossMqttConnector();
        if (mqttConnector == null) {
            return;
        }
        ThingStatus bridgeStatus = bridge.getStatus();
        if (bridgeStatus.equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        Thing thing = getThing();
        String label = thing.getLabel();
        if (config.name.isEmpty()) {
            if (label != null) {
                config.name = label;
            }
        }
        String deviceUUID = merossBridgeHandler.getDevUUIDByDevName(config.name);
        if (deviceUUID.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "No device found with name " + config.name);
            return;
        }
        scheduler.submit(() -> {
            MerossManager manager = this.manager;
            manager = manager != null ? manager : new MerossManager(mqttConnector, deviceUUID, this);
            this.manager = manager;
            try {
                manager.initialize();
            } catch (MqttException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error for device with name " + config.name);
            }
        });
    }

    @Override
    public void dispose() {
        MerossManager manager = this.manager;
        if (manager != null) {
            manager.dispose();
            this.manager = null;
        }
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() != null) {
            ThingStatus bridgeStatus = bridge.getStatus();
            if (ThingStatus.OFFLINE.equals(bridgeStatus)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                return;
            }
            initializeDevice();
        }
    }

    public void setTingStatusFromMerossStatus(int status) {
        if (status == MerossEnum.OnlineStatus.UNKNOWN.value() || status == MerossEnum.OnlineStatus.NOT_ONLINE.value()
                || status == MerossEnum.OnlineStatus.UPGRADING.value()) {
            updateStatus(ThingStatus.UNKNOWN);
        } else if (status == MerossEnum.OnlineStatus.OFFLINE.value()) {
            updateStatus(ThingStatus.OFFLINE);
        } else if (status == MerossEnum.OnlineStatus.ONLINE.value()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public abstract void updateState(int deviceChannel, State state);
}

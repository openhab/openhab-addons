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

import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tuya.internal.TuyaDiscoveryService;
import org.openhab.binding.tuya.internal.actions.TuyaSmartLifeActions;
import org.openhab.binding.tuya.internal.cloud.ApiStatusCallback;
import org.openhab.binding.tuya.internal.cloud.TuyaSmartLifeAPI;
import org.openhab.binding.tuya.internal.config.SmartLifeConfiguration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link TuyaSmartLifeHandler} is responsible for handling cloud linkage via the Smart Life app.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public class TuyaSmartLifeHandler extends ProjectHandler implements ApiStatusCallback {
    private final Logger logger = LoggerFactory.getLogger(TuyaSmartLifeHandler.class);

    public TuyaSmartLifeHandler(Thing thing, HttpClient httpClient, Gson gson) {
        super(thing, httpClient, gson);
        this.api = new TuyaSmartLifeAPI(this, scheduler, gson, httpClient);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        SmartLifeConfiguration config = getConfigAs(SmartLifeConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        api.setConfiguration(config);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Not logged in");
    }

    @Override
    public void tuyaOpenApiStatus(boolean status) {
        if (!status) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Not logged in");
        } else {
            stopApiConnectFuture();

            if (thing.getStatus() != ThingStatus.ONLINE) {
                var discoveryService = this.discoveryService;
                if (discoveryService != null) {
                    discoveryService.startScan(true);
                }
            }

            updateStatus(ThingStatus.ONLINE);
        }
    }

    public String actionQrLogin() {
        tuyaOpenApiStatus(false);
        return ((TuyaSmartLifeAPI) api).getQrLoginText();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TuyaSmartLifeActions.class, TuyaDiscoveryService.class);
    }
}

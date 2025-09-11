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
package org.openhab.binding.tuya.internal.handler;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tuya.internal.TuyaDiscoveryService;
import org.openhab.binding.tuya.internal.cloud.ApiStatusCallback;
import org.openhab.binding.tuya.internal.cloud.TuyaOpenAPI;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceListInfo;
import org.openhab.binding.tuya.internal.cloud.dto.DeviceSchema;
import org.openhab.binding.tuya.internal.config.ProjectConfiguration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

import com.google.gson.Gson;

/**
 * The {@link ProjectHandler} is responsible for handling communication
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ProjectHandler extends BaseThingHandler implements ApiStatusCallback {
    private final TuyaOpenAPI api;
    private final Storage<String> storage;

    private @Nullable ScheduledFuture<?> apiConnectFuture;

    public ProjectHandler(Thing thing, HttpClient httpClient, Storage<String> storage, Gson gson) {
        super(thing);
        this.api = new TuyaOpenAPI(this, scheduler, gson, httpClient);
        this.storage = storage;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        ProjectConfiguration config = getConfigAs(ProjectConfiguration.class);

        if (!config.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        api.setConfiguration(config);
        updateStatus(ThingStatus.UNKNOWN);

        stopApiConnectFuture();
        apiConnectFuture = scheduler.schedule(api::login, 0, TimeUnit.SECONDS);
    }

    @Override
    public void tuyaOpenApiStatus(boolean status) {
        if (!status) {
            stopApiConnectFuture();
            apiConnectFuture = scheduler.schedule(api::login, 60, TimeUnit.SECONDS);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            stopApiConnectFuture();
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public TuyaOpenAPI getApi() {
        return api;
    }

    public Storage<String> getStorage() {
        return storage;
    }

    public CompletableFuture<List<DeviceListInfo>> getAllDevices(int page) {
        if (api.isConnected()) {
            return api.getDeviceList(page);
        }
        return CompletableFuture.failedFuture(new IllegalStateException("not connected"));
    }

    public CompletableFuture<DeviceSchema> getDeviceSchema(String deviceId) {
        if (api.isConnected()) {
            return api.getDeviceSchema(deviceId);
        }
        return CompletableFuture.failedFuture(new IllegalStateException("not connected"));
    }

    private void stopApiConnectFuture() {
        ScheduledFuture<?> apiConnectFuture = this.apiConnectFuture;
        if (apiConnectFuture != null) {
            apiConnectFuture.cancel(true);
            this.apiConnectFuture = null;
        }
    }

    @Override
    public void dispose() {
        stopApiConnectFuture();
        api.disconnect();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TuyaDiscoveryService.class);
    }
}

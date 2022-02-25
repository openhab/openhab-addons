/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sunsa.internal.bridge;

import java.util.List;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sunsa.internal.client.SunsaCloudService;
import org.openhab.binding.sunsa.internal.client.SunsaCloudUriProvider;
import org.openhab.binding.sunsa.internal.client.SunsaService;
import org.openhab.binding.sunsa.internal.domain.Device;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;

/**
 * @author jirom - Initial contribution
 */
@NonNullByDefault
public class SunsaCloudBridgeHandler extends SunsaBridgeHandler {
    private final SunsaService sunsaService;
    private @Nullable SunsaCloudBridgeConfiguration config;

    public SunsaCloudBridgeHandler(final Bridge bridge, final ClientBuilder clientBuilder) {
        super(bridge);
        this.sunsaService = new SunsaCloudService(clientBuilder, this::getUserId, this::getApiKey,
                new SunsaCloudUriProvider(this::getBaseUri));
    }

    @Override
    public void initialize() {
        config = getConfigAs(SunsaCloudBridgeConfiguration.class);

        if (config.isValid()) {
            updateStatus(ThingStatus.INITIALIZING);
            // Force an API call to verify we can make network calls.
            scheduler.execute(this::getDevices);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public SunsaService getSunsaService() {
        return sunsaService;
    }

    private String getApiKey() {
        return config != null ? config.apiKey : "";
    }

    private String getUserId() {
        return config != null ? config.userId : "";
    }

    private String getBaseUri() {
        return config != null ? config.baseUri : "";
    }

    @Override
    public List<Device> getDevices() throws SunsaException {
        return sunsaService.getDevices();
    }

    @Override
    public Device updateDevice(Device device) throws SunsaException {
        return sunsaService.updateDevice(device);
    }

    @Override
    public Device getDevice(String id) throws SunsaException {
        return sunsaService.getDevice(id);
    }

    @Override
    public int setDevicePosition(String deviceId, int rawPosition) {
        return sunsaService.setDevicePosition(deviceId, rawPosition);
    }
}

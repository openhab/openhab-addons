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
package org.openhab.binding.salus.internal.cloud.handler;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.SalusApi;
import org.openhab.binding.salus.internal.cloud.rest.HttpSalusApi;
import org.openhab.binding.salus.internal.handler.AbstractBridgeHandler;
import org.openhab.binding.salus.internal.handler.CloudApi;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public final class CloudBridgeHandler extends AbstractBridgeHandler<CloudBridgeConfig> implements CloudApi {

    public CloudBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge, httpClientFactory, CloudBridgeConfig.class);
    }

    @Override
    protected SalusApi newSalusApi(CloudBridgeConfig config, RestClient httpClient, GsonMapper gsonMapper) {
        return new HttpSalusApi(config.getUsername(), config.getPassword().getBytes(UTF_8), config.getUrl(), httpClient,
                gsonMapper);
    }

    @Override
    public Set<String> it600RequiredChannels() {
        return Set.of("ep_9:sIT600TH:LocalTemperature_x100", "ep_9:sIT600TH:HeatingSetpoint_x100",
                "ep_9:sIT600TH:SetHeatingSetpoint_x100", "ep_9:sIT600TH:HoldType", "ep_9:sIT600TH:SetHoldType",
                "ep_9:sIT600TH:RunningState");
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String channelPrefix() {
        return "ep_9";
    }
}

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
package org.openhab.binding.salus.internal.aws.handler;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.SalusApi;
import org.openhab.binding.salus.internal.aws.http.AwsSalusApi;
import org.openhab.binding.salus.internal.handler.AbstractBridgeHandler;
import org.openhab.binding.salus.internal.rest.GsonMapper;
import org.openhab.binding.salus.internal.rest.RestClient;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public final class AwsCloudBridgeHandler extends AbstractBridgeHandler<AwsCloudBridgeConfig> {
    private final HttpClientFactory httpClientFactory;

    public AwsCloudBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory) {
        super(bridge, httpClientFactory, AwsCloudBridgeConfig.class);
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    protected SalusApi newSalusApi(AwsCloudBridgeConfig config, RestClient httpClient, GsonMapper gsonMapper) {
        return new AwsSalusApi(httpClientFactory, config.getUsername(), config.getPassword().getBytes(UTF_8),
                config.getUrl(), httpClient, gsonMapper, config.getUserPoolId(), config.getIdentityPoolId(),
                config.getClientId(), config.getRegion(), config.getCompanyCode(), config.getAwsService());
    }

    @Override
    public Set<String> it600RequiredChannels() {
        return Set.of("ep9:sIT600TH:LocalTemperature_x100", "ep9:sIT600TH:HeatingSetpoint_x100",
                "ep9:sIT600TH:HoldType");
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public String channelPrefix() {
        return "ep9";
    }
}

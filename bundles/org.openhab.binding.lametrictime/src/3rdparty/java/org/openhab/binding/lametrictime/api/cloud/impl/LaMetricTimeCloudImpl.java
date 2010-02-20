/**
 * Copyright 2017-2018 Gregory Moyer and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.lametrictime.api.cloud.impl;

import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.openhab.binding.lametrictime.api.cloud.CloudConfiguration;
import org.openhab.binding.lametrictime.api.cloud.LaMetricTimeCloud;
import org.openhab.binding.lametrictime.api.cloud.model.IconFilter;
import org.openhab.binding.lametrictime.api.cloud.model.Icons;
import org.openhab.binding.lametrictime.api.common.impl.AbstractClient;
import org.openhab.binding.lametrictime.api.filter.LoggingFilter;
import org.openhab.binding.lametrictime.internal.GsonProvider;

public class LaMetricTimeCloudImpl extends AbstractClient implements LaMetricTimeCloud {
    private final CloudConfiguration config;

    public LaMetricTimeCloudImpl(CloudConfiguration config) {
        this.config = config;
    }

    public LaMetricTimeCloudImpl(CloudConfiguration config, ClientBuilder clientBuilder) {
        super(clientBuilder);
        this.config = config;
    }

    @Override
    public Icons getIcons() {
        return getClient().target(config.getBaseUri()).path("/icons").request(MediaType.APPLICATION_JSON_TYPE)
                .get(Icons.class);
    }

    @Override
    public Icons getIcons(IconFilter filter) {
        return getClient().target(config.getBaseUri()).path("/icons").queryParam("page", filter.getPage())
                .queryParam("page_size", filter.getPageSize()).queryParam("fields", filter.getFieldsString())
                .queryParam("order", filter.getOrderString()).request(MediaType.APPLICATION_JSON_TYPE).get(Icons.class);
    }

    @Override
    protected Client createClient() {
        ClientBuilder builder = getClientBuilder();

        // setup Gson (de)serialization
        GsonProvider<Object> gsonProvider = new GsonProvider<>();
        builder.register(gsonProvider);

        // turn on logging if requested
        if (config.isLogging()) {
            builder.register(
                    new LoggingFilter(Logger.getLogger(LaMetricTimeCloudImpl.class.getName()), config.getLogMax()));
        }

        return builder.build();
    }
}

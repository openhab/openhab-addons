/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal;

import static org.openhab.binding.cloudrain.internal.CloudrainBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPI;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIMockup;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIProxy;
import org.openhab.binding.cloudrain.internal.api.CloudrainAPIv1Impl;
import org.openhab.binding.cloudrain.internal.handler.CloudrainAccountHanlder;
import org.openhab.binding.cloudrain.internal.handler.CloudrainZoneHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link CloudrainHandlerFactory} is responsible for creating things and thing handlers of the Cloudrain binding.
 * Currently two things are supported: The Cloudrain Account acting as bridge for the Cloudrain ecosystem and Cloudrain
 * Zones which represent user-defined irrigation zones comprising of one or more valves controlled simultaneously.
 * Valves are not represented as things because the Cloudrain API does not offer access to valve details.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.cloudrain", service = ThingHandlerFactory.class)
public class CloudrainHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_ZONE);

    private ItemChannelLinkRegistry itemChannelLinkRegistry;
    private TimeZoneProvider timeZoneProvider;
    private CloudrainAPI cloudrainAPI;

    @Activate
    public CloudrainHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        this.timeZoneProvider = timeZoneProvider;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
        this.cloudrainAPI = createCloudrainAPI(httpClientFactory);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_ACCOUNT.equals(thing.getThingTypeUID())) {
            return new CloudrainAccountHanlder((Bridge) thing, cloudrainAPI);
        } else if (THING_TYPE_ZONE.equals(thing.getThingTypeUID())) {
            CloudrainZoneHandler zoneHanlder = new CloudrainZoneHandler(thing, cloudrainAPI, timeZoneProvider,
                    itemChannelLinkRegistry);
            return zoneHanlder;
        } else {
            return null;
        }
    }

    private CloudrainAPI createCloudrainAPI(HttpClientFactory httpClientFactory) {
        HttpClient httpClient = httpClientFactory.createHttpClient(CloudrainBindingConstants.BINDING_ID);
        httpClient.setConnectTimeout(CloudrainConfig.DEFAULT_CONNECTION_TIMEOUT * 1000L);
        httpClient.setIdleTimeout(httpClient.getConnectTimeout());
        CloudrainAPI realAPI = new CloudrainAPIv1Impl(httpClient);
        CloudrainAPI testAPI = new CloudrainAPIMockup();
        return new CloudrainAPIProxy(realAPI, testAPI);
    }
}

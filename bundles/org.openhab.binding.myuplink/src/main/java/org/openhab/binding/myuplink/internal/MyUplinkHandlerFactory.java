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
package org.openhab.binding.myuplink.internal;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.myuplink.internal.handler.MyUplinkAccountHandler;
import org.openhab.binding.myuplink.internal.handler.MyUplinkGenericDeviceHandler;
import org.openhab.binding.myuplink.internal.provider.ChannelFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link myUplinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.myuplink", service = ThingHandlerFactory.class)
public class MyUplinkHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MyUplinkHandlerFactory.class);

    /**
     * the shared http client
     */
    private final HttpClient httpClient;

    private final ChannelFactory channelFactory;

    @Activate
    public MyUplinkHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference ChannelFactory channelFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.channelFactory = channelFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new MyUplinkAccountHandler((Bridge) thing, httpClient);
        } else if (THING_TYPE_GENERIC_DEVICE.equals(thingTypeUID)) {
            return new MyUplinkGenericDeviceHandler(thing, channelFactory);
        } else {
            logger.warn("Unsupported Thing-Type: {}", thingTypeUID.getAsString());
        }

        return null;
    }
}

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
package org.openhab.binding.senseenergy.internal;

import static org.openhab.binding.senseenergy.internal.SenseEnergyBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.senseenergy.internal.handler.SenseEnergyBridgeHandler;
import org.openhab.binding.senseenergy.internal.handler.SenseEnergyMonitorHandler;
import org.openhab.binding.senseenergy.internal.handler.SenseEnergyProxyDeviceHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelGroupTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SenseEnergyHandlerFactory}
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.senseenergy", service = ThingHandlerFactory.class)
public class SenseEnergyHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(APIBRIDGE_THING_TYPE, MONITOR_THING_TYPE,
            PROXY_DEVICE_THING_TYPE);

    private final HttpClientFactory httpClientFactory;
    private final WebSocketFactory webSocketFactory;
    private final ChannelGroupTypeRegistry channelGroupTypeRegistry;
    private final ChannelTypeRegistry channelTypeRegistry;

    @Activate
    public SenseEnergyHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference WebSocketFactory webSocketFactory,
            final @Reference ChannelGroupTypeRegistry channelGroupTypeRegistry,
            final @Reference ChannelTypeRegistry channelTypeRegistry) {
        this.httpClientFactory = httpClientFactory;
        this.webSocketFactory = webSocketFactory;
        this.channelGroupTypeRegistry = channelGroupTypeRegistry;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (APIBRIDGE_THING_TYPE.equals(thingTypeUID)) {
            return new SenseEnergyBridgeHandler((Bridge) thing, this.httpClientFactory.getCommonHttpClient());
        } else if (MONITOR_THING_TYPE.equals(thingTypeUID)) {
            return new SenseEnergyMonitorHandler((Bridge) thing, webSocketFactory.getCommonWebSocketClient(),
                    channelGroupTypeRegistry, channelTypeRegistry);
        } else if (PROXY_DEVICE_THING_TYPE.equals(thingTypeUID)) {
            return new SenseEnergyProxyDeviceHandler(thing);
        }

        return null;
    }
}

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
package org.openhab.binding.tuya.internal;

import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_PROJECT;
import static org.openhab.binding.tuya.internal.TuyaBindingConstants.THING_TYPE_TUYA_DEVICE;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tuya.internal.handler.ProjectHandler;
import org.openhab.binding.tuya.internal.handler.TuyaDeviceHandler;
import org.openhab.binding.tuya.internal.local.UdpDiscoveryListener;
import org.openhab.binding.tuya.internal.util.SchemaDp;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

/**
 * The {@link TuyaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tuya", service = ThingHandlerFactory.class)
@SuppressWarnings("unused")
public class TuyaHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PROJECT,
            THING_TYPE_TUYA_DEVICE);
    private static final Type STORAGE_TYPE = TypeToken.getParameterized(List.class, SchemaDp.class).getType();

    public static final TuyaSchemaDB SCHEMAS = new TuyaSchemaDB();

    private final TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider;
    private final TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final UdpDiscoveryListener udpDiscoveryListener;
    private final EventLoopGroup eventLoopGroup;

    @Activate
    public TuyaHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TuyaDynamicCommandDescriptionProvider dynamicCommandDescriptionProvider,
            @Reference TuyaDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            @Reference StorageService storageService) throws InterruptedException {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.dynamicCommandDescriptionProvider = dynamicCommandDescriptionProvider;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        this.eventLoopGroup = new NioEventLoopGroup();
        this.udpDiscoveryListener = new UdpDiscoveryListener(eventLoopGroup);

        TuyaSchemaDB.setStorage(storageService, "org.openhab.binding.tuya.Schema");
    }

    @Deactivate
    public void deactivate() {
        udpDiscoveryListener.deactivate();
        eventLoopGroup.shutdownGracefully();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_PROJECT.equals(thingTypeUID)) {
            return new ProjectHandler(thing, httpClient, gson);
        } else if (THING_TYPE_TUYA_DEVICE.equals(thingTypeUID)) {
            return new TuyaDeviceHandler(thing, gson, dynamicCommandDescriptionProvider,
                    dynamicStateDescriptionProvider, eventLoopGroup, udpDiscoveryListener);
        }

        return null;
    }
}

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
package org.openhab.binding.roborock.internal;

import static org.openhab.binding.roborock.internal.RoborockBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RoborockHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.roborock", service = ThingHandlerFactory.class)
public class RoborockHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final StorageService storageService;
    private final ChannelTypeRegistry channelTypeRegistry;
    private final RoborockStateDescriptionOptionProvider stateDescriptionProvider;

    @Activate
    public RoborockHandlerFactory(@Reference StorageService storageService,
            @Reference HttpClientFactory httpClientFactory, ComponentContext componentContext,
            @Reference ChannelTypeRegistry channelTypeRegistry,
            @Reference RoborockStateDescriptionOptionProvider stateDescriptionProvider) {
        super.activate(componentContext);
        this.storageService = storageService;
        this.channelTypeRegistry = channelTypeRegistry;
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (ROBOROCK_ACCOUNT.equals(thingTypeUID)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new RoborockAccountHandler((Bridge) thing, storage, httpClientFactory.getCommonHttpClient());
        } else {
            return new RoborockVacuumHandler(thing, channelTypeRegistry, stateDescriptionProvider);
        }
    }
}

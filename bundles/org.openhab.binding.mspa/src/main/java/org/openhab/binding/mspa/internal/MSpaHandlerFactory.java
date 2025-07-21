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
package org.openhab.binding.mspa.internal;

import static org.openhab.binding.mspa.internal.MSpaConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mspa.internal.discovery.MSpaDiscoveryService;
import org.openhab.binding.mspa.internal.handler.MSpaOwnerAccount;
import org.openhab.binding.mspa.internal.handler.MSpaPool;
import org.openhab.binding.mspa.internal.handler.MSpaVisitorAccount;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MSpaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mspa", service = ThingHandlerFactory.class)
public class MSpaHandlerFactory extends BaseThingHandlerFactory {

    private final MSpaCommandOptionProvider commandOptions;
    private final MSpaDiscoveryService discovery;
    private final HttpClientFactory httpFactory;
    private final UnitProvider unitProvider;
    private final Storage<String> store;

    @Activate
    public MSpaHandlerFactory(@Reference HttpClientFactory httpFactory, @Reference StorageService storageService,
            @Reference MSpaDiscoveryService discovery, final @Reference UnitProvider unitProvider,
            final @Reference MSpaCommandOptionProvider commandOptions) {
        this.httpFactory = httpFactory;
        this.discovery = discovery;
        this.unitProvider = unitProvider;
        this.commandOptions = commandOptions;
        store = storageService.getStorage(BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_OWNER_ACCOUNT.equals(thingTypeUID)) {
            return new MSpaOwnerAccount((Bridge) thing, httpFactory.getCommonHttpClient(), discovery, store);
        } else if (THING_TYPE_VISITOR_ACCOUNT.equals(thingTypeUID)) {
            return new MSpaVisitorAccount((Bridge) thing, httpFactory.getCommonHttpClient(), discovery, store);
        } else if (THING_TYPE_POOL.equals(thingTypeUID)) {
            return new MSpaPool(thing, unitProvider, commandOptions);
        }
        return null;
    }
}

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
package org.openhab.binding.huesync.internal.factory;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.handler.HueSyncHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HueSyncHandlerFactory} is responsible for creating things and
 * thing
 * handlers.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.huesync", service = ThingHandlerFactory.class)
public class HueSyncHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;

    @Activate
    public HueSyncHandlerFactory(@Reference final HttpClientFactory httpClientFactory) throws Exception {
        this.httpClientFactory = httpClientFactory;
    }

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(HueSyncConstants.THING_TYPE_UID);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (HueSyncConstants.THING_TYPE_UID.equals(thingTypeUID)) {
            return new HueSyncHandler(thing, this.httpClientFactory);
        }

        return null;
    }
}

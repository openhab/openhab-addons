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
package org.openhab.binding.ecoflow.internal;

import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.THING_TYPE_API;
import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.THING_TYPE_DELTA2;
import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.THING_TYPE_DELTA2MAX;
import static org.openhab.binding.ecoflow.internal.EcoflowBindingConstants.THING_TYPE_POWERSTREAM;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecoflow.internal.handler.Delta2Handler;
import org.openhab.binding.ecoflow.internal.handler.EcoflowApiHandler;
import org.openhab.binding.ecoflow.internal.handler.PowerStreamHandler;
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

/**
 * The {@link EcoflowHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ecoflow", service = ThingHandlerFactory.class)
public class EcoflowHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_DELTA2,
            THING_TYPE_DELTA2MAX, THING_TYPE_POWERSTREAM);

    @Activate
    public EcoflowHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API.equals(thingTypeUID)) {
            return new EcoflowApiHandler((Bridge) thing, httpClientFactory.getCommonHttpClient());
        } else if (THING_TYPE_DELTA2MAX.equals(thingTypeUID)) {
            return new Delta2Handler(thing, true);
        } else if (THING_TYPE_DELTA2.equals(thingTypeUID)) {
            return new Delta2Handler(thing, false);
        } else {
            return new PowerStreamHandler(thing);
        }
    }
}

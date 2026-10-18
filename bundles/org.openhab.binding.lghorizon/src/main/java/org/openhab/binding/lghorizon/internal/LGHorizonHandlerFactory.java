/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lghorizon.internal;

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonAccountHandler;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonBoxHandler;
import org.openhab.binding.lghorizon.internal.handler.LGHorizonDynamicStateDescriptionProvider;
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
 * The {@link LGHorizonHandlerFactory} creates the account bridge handler and box thing handlers.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.lghorizon", service = ThingHandlerFactory.class)
public class LGHorizonHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Activate
    public LGHorizonHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference LGHorizonDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        this.httpClientFactory = httpClientFactory;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ACCOUNT.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new LGHorizonAccountHandler(bridge, httpClientFactory.getCommonHttpClient());
        }
        if (THING_TYPE_BOX.equals(thingTypeUID)) {
            return new LGHorizonBoxHandler(thing, dynamicStateDescriptionProvider);
        }
        return null;
    }
}

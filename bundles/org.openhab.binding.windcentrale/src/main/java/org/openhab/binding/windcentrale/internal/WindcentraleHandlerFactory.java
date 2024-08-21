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
package org.openhab.binding.windcentrale.internal;

import static org.openhab.binding.windcentrale.internal.WindcentraleBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.windcentrale.internal.handler.WindcentraleAccountHandler;
import org.openhab.binding.windcentrale.internal.handler.WindcentraleWindmillHandler;
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
 * The {@link WindcentraleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Wouter Born - Add support for new API with authentication
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.windcentrale")
@NonNullByDefault
public class WindcentraleHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;

    @Activate
    public WindcentraleHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            return new WindcentraleAccountHandler((Bridge) thing, httpClientFactory);
        } else if (thingTypeUID.equals(THING_TYPE_WINDMILL)) {
            return new WindcentraleWindmillHandler(thing);
        }

        return null;
    }
}

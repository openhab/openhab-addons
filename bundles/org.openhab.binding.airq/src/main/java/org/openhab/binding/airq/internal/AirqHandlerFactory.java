/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.airq.internal;

import static org.openhab.binding.airq.internal.AirqBindingConstants.THING_TYPE_AIRQ;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link AirqHandlerFactory} is responsible for creating the air-Q thing and its handlers.
 *
 * @author Aurelio Caliaro - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.airq", service = ThingHandlerFactory.class)
public class AirqHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_AIRQ);
    private final HttpClientFactory httpClientFactory;

    @Activate
    public AirqHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_AIRQ.equals(thingTypeUID)) {
            return new AirqHandler(thing, httpClientFactory.getCommonHttpClient());
        }
        return null;
    }
}

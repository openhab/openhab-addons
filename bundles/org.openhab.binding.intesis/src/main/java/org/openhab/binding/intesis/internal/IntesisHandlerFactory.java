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
package org.openhab.binding.intesis.internal;

import static org.openhab.binding.intesis.internal.IntesisBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.intesis.internal.handler.IntesisBoxHandler;
import org.openhab.binding.intesis.internal.handler.IntesisHomeHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link IntesisHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.intesis", service = ThingHandlerFactory.class)
public class IntesisHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final IntesisDynamicStateDescriptionProvider intesisStateDescriptionProvider;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_INTESISHOME, THING_TYPE_INTESISBOX).collect(Collectors.toSet()));

    @Activate
    public IntesisHandlerFactory(@Reference HttpClientFactory httpClientFactory, ComponentContext componentContext,
            final @Reference IntesisDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super.activate(componentContext);

        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.intesisStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_INTESISHOME.equals(thingTypeUID)) {
            return new IntesisHomeHandler(thing, httpClient, intesisStateDescriptionProvider);
        }

        if (THING_TYPE_INTESISBOX.equals(thingTypeUID)) {
            return new IntesisBoxHandler(thing, intesisStateDescriptionProvider);
        }

        return null;
    }
}

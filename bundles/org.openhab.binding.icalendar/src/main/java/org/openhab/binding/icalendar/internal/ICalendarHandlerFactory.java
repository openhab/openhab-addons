/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.icalendar.internal.handler.EventFilterHandler;
import org.openhab.binding.icalendar.internal.handler.ICalendarHandler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ICalendarHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Wodniok - Initial contribution
 * @author Andrew Fiddian-Green - EventPublisher code
 * @author Michael Wodniok - Added FilteredEvent item type/handler
 */
@NonNullByDefault
@Component(configurationPid = "binding.icalendar", service = ThingHandlerFactory.class)
public class ICalendarHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(Collections.singleton(THING_TYPE_CALENDAR), Collections.singleton(THING_TYPE_FILTERED_EVENTS))
            .flatMap(Set::stream).collect(Collectors.toSet());

    private final HttpClient sharedHttpClient;
    private final EventPublisher eventPublisher;

    @Activate
    public ICalendarHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        sharedHttpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (!supportsThingType(thingTypeUID)) {
            return null;
        }
        if (thingTypeUID.equals(THING_TYPE_CALENDAR)) {
            return new ICalendarHandler((Bridge) thing, sharedHttpClient, eventPublisher);
        } else if (thingTypeUID.equals(THING_TYPE_FILTERED_EVENTS)) {
            return new EventFilterHandler(thing);
        }
        return null;
    }
}

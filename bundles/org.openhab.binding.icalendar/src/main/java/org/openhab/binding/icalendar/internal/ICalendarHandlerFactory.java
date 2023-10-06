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
package org.openhab.binding.icalendar.internal;

import static org.openhab.binding.icalendar.internal.ICalendarBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.icalendar.internal.handler.EventFilterHandler;
import org.openhab.binding.icalendar.internal.handler.ICalendarHandler;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.i18n.TimeZoneProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            .of(Set.of(THING_TYPE_CALENDAR), Set.of(THING_TYPE_FILTERED_EVENTS)).flatMap(Set::stream)
            .collect(Collectors.toSet());
    private final Logger logger = LoggerFactory.getLogger(ICalendarHandlerFactory.class);

    private final HttpClient sharedHttpClient;
    private final EventPublisher eventPublisher;
    private final TimeZoneProvider tzProvider;

    @Activate
    public ICalendarHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference EventPublisher eventPublisher, @Reference TimeZoneProvider tzProvider) {
        this.eventPublisher = eventPublisher;
        sharedHttpClient = httpClientFactory.getCommonHttpClient();
        this.tzProvider = tzProvider;
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
            if (thing instanceof Bridge bridge) {
                return new ICalendarHandler(bridge, sharedHttpClient, eventPublisher, tzProvider);
            } else {
                logger.warn(
                        "The API of iCalendar has changed. You have to recreate the calendar according to the docs.");
            }
        } else if (thingTypeUID.equals(THING_TYPE_FILTERED_EVENTS)) {
            return new EventFilterHandler(thing, tzProvider);
        }
        return null;
    }
}

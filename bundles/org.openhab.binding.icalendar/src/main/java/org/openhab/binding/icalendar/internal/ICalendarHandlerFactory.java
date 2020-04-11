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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.icalendar.internal.handler.ICalendarHandler;
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
 */
@NonNullByDefault
@Component(configurationPid = "binding.icalendar", service = ThingHandlerFactory.class)
public class ICalendarHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_CALENDAR);

    private final Logger logger = LoggerFactory.getLogger(ICalendarHandlerFactory.class);
    private final HttpClient sharedHttpClient;
    private final EventPublisher eventPublisher;

    @Activate
    public ICalendarHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        sharedHttpClient = httpClientFactory.createHttpClient(BINDING_ID);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (!supportsThingType(thingTypeUID)) {
            return null;
        }
        try {
            if (!sharedHttpClient.isStarted()) {
                sharedHttpClient.start();
            }
            return new ICalendarHandler(thing, sharedHttpClient, eventPublisher);
        } catch (Exception e) {
            logger.warn("Failed to create handler for thing with uid {}.", thing.getUID());
            logger.debug("internal exception while creating or preparing handler.", e);
        }

        return null;
    }
}

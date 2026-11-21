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
package org.openhab.binding.robonect.internal;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.handler.RobonectHandler;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link RobonectHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marco Meyer - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.robonect")
public class RobonectHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AUTOMOWER);

    private HttpClientFactory httpClientFactory;
    private TimeZoneProvider timeZoneProvider;
    private RobonectPushServlet pushServlet;

    /**
     * Creates a handler factory with its required services.
     *
     * @param httpClientFactory the factory for HTTP clients
     * @param timeZoneProvider the provider of the default time zone
     * @param pushServlet the servlet receiving push notifications
     */
    @Activate
    public RobonectHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider, @Reference RobonectPushServlet pushServlet) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.pushServlet = pushServlet;
    }

    /**
     * Checks whether a Thing type is supported by this binding.
     *
     * @param thingTypeUID the Thing type identifier
     * @return {@code true} if the type is supported
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Creates a handler for a supported Thing.
     *
     * @param thing the Thing requiring a handler
     * @return the handler, or {@code null} for an unsupported type
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_AUTOMOWER)) {
            return new RobonectHandler(thing, httpClientFactory, timeZoneProvider, pushServlet);
        }

        return null;
    }
}

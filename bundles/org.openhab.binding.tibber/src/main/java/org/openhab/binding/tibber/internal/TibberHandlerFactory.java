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
package org.openhab.binding.tibber.internal;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tibber.internal.handler.TibberHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link TibberHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stian Kjoglum - Initial contribution
 * @author Bernd Weymann - Use HttpClientFactory, CronScheduler and TimeZoneProvider
 */
@NonNullByDefault
@Component(configurationPid = "binding.tibber", service = ThingHandlerFactory.class)
public class TibberHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpFactory;
    private final CronScheduler cron;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public TibberHandlerFactory(final @Reference HttpClientFactory httpFactory, final @Reference CronScheduler cron,
            final @Reference TimeZoneProvider timeZoneProvider) {
        this.httpFactory = httpFactory;
        this.cron = cron;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(TIBBER_THING_TYPE)) {
            return new TibberHandler(thing, httpFactory.getCommonHttpClient(), cron, bundleContext, timeZoneProvider);
        } else {
            return null;
        }
    }
}

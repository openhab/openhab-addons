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
package org.openhab.binding.entsoe.internal;

import static org.openhab.binding.entsoe.internal.EntsoeBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.entsoe.internal.handler.EntsoeHandler;
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
 * The {@link EntsoeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jørgen Melhus - Initial contribution
 * @author Bernd Weymann - Add TimeZoneProvider and cron scheduler
 */
@NonNullByDefault
@Component(configurationPid = "binding.entsoe", service = ThingHandlerFactory.class)
public class EntsoeHandlerFactory extends BaseThingHandlerFactory {

    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;
    private final CronScheduler cron;

    @Activate
    public EntsoeHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TimeZoneProvider timeZoneProvider, final @Reference CronScheduler cron) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.timeZoneProvider = timeZoneProvider;
        this.cron = cron;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_DAY_AHEAD.equals(thingTypeUID)) {
            return new EntsoeHandler(thing, httpClient, timeZoneProvider, cron);
        }
        return null;
    }
}

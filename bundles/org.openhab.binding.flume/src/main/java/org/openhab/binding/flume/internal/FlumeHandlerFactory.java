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
package org.openhab.binding.flume.internal;

import static org.openhab.binding.flume.internal.FlumeBindingConstants.*;

import java.util.Set;

import javax.measure.spi.SystemOfUnits;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.flume.internal.handler.FlumeBridgeHandler;
import org.openhab.binding.flume.internal.handler.FlumeDeviceHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.i18n.UnitProvider;
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
 * The {@link FlumeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.flume", service = ThingHandlerFactory.class)
public class FlumeHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_CLOUD, THING_TYPE_METER);

    private final HttpClientFactory httpClientFactory;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    public final SystemOfUnits systemOfUnits;

    @Activate
    public FlumeHandlerFactory(@Reference UnitProvider unitProvider, @Reference HttpClientFactory httpClientFactory,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider) {
        this.systemOfUnits = unitProvider.getMeasurementSystem();
        this.httpClientFactory = httpClientFactory;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_CLOUD.equals(thingTypeUID)) {
            return new FlumeBridgeHandler((Bridge) thing, systemOfUnits, this.httpClientFactory.getCommonHttpClient(),
                    i18nProvider, localeProvider);
        } else if (THING_TYPE_METER.equals(thingTypeUID)) {
            return new FlumeDeviceHandler(thing);
        }

        return null;
    }
}

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
package org.openhab.binding.argoclima.internal;

import static org.openhab.binding.argoclima.internal.ArgoClimaBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.argoclima.internal.handler.ArgoClimaHandlerLocal;
import org.openhab.binding.argoclima.internal.handler.ArgoClimaHandlerRemote;
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
 * The {@link ArgoClimaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Mateusz Bronk - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding." + ArgoClimaBindingConstants.BINDING_ID, service = ThingHandlerFactory.class)
public class ArgoClimaHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;
    private final ArgoClimaTranslationProvider i18nProvider;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ARGOCLIMA_LOCAL,
            THING_TYPE_ARGOCLIMA_REMOTE);

    @Activate
    public ArgoClimaHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TimeZoneProvider timeZoneProvider,
            final @Reference ArgoClimaTranslationProvider i18nProvider) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ARGOCLIMA_LOCAL.equals(thingTypeUID)) {
            return new ArgoClimaHandlerLocal(thing, httpClientFactory, timeZoneProvider, i18nProvider);
        }
        if (THING_TYPE_ARGOCLIMA_REMOTE.equals(thingTypeUID)) {
            return new ArgoClimaHandlerRemote(thing, httpClientFactory, timeZoneProvider, i18nProvider);
        }

        return null;
    }
}

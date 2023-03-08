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
package org.openhab.binding.ecovacs.internal;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.handler.EcovacsApiHandler;
import org.openhab.binding.ecovacs.internal.handler.EcovacsVacuumHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
 * The {@link EcovacsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ecovacs", service = ThingHandlerFactory.class)
public class EcovacsHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18Provider;
    private final EcovacsDynamicStateDescriptionProvider stateDescriptionProvider;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_VACUUM);

    @Activate
    public EcovacsHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference EcovacsDynamicStateDescriptionProvider stateDescriptionProvider,
            final @Reference LocaleProvider localeProvider, final @Reference TranslationProvider i18Provider) {
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.localeProvider = localeProvider;
        this.i18Provider = i18Provider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_API.equals(thingTypeUID)) {
            return new EcovacsApiHandler((Bridge) thing, httpClientFactory.getCommonHttpClient(), localeProvider);
        } else {
            return new EcovacsVacuumHandler(thing, i18Provider, localeProvider, stateDescriptionProvider);
        }
    }
}

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
package org.openhab.binding.panasonicbdp.internal;

import static org.openhab.binding.panasonicbdp.internal.PanaBlurayBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.panasonicbdp.internal.handler.PanaBlurayHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
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
 * The {@link PanaBlurayHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.panasonicbdp")
public class PanaBlurayHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public PanaBlurayHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (SUPPORTED_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            return new PanaBlurayHandler(thing, httpClient, translationProvider, localeProvider);
        }

        return null;
    }
}

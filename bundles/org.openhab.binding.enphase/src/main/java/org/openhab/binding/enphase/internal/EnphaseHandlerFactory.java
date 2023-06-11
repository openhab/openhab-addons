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
package org.openhab.binding.enphase.internal;

import static org.openhab.binding.enphase.internal.EnphaseBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.enphase.internal.handler.EnphaseInverterHandler;
import org.openhab.binding.enphase.internal.handler.EnphaseRelayHandler;
import org.openhab.binding.enphase.internal.handler.EnvoyBridgeHandler;
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
 * The {@link EnphaseHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.enphase", service = ThingHandlerFactory.class)
public class EnphaseHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ENPHASE_ENVOY,
            THING_TYPE_ENPHASE_INVERTER, THING_TYPE_ENPHASE_RELAY);

    private final MessageTranslator messageTranslator;
    private final HttpClient commonHttpClient;
    private final EnvoyHostAddressCache envoyHostAddressCache;

    @Activate
    public EnphaseHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference HttpClientFactory httpClientFactory,
            @Reference final EnvoyHostAddressCache envoyHostAddressCache) {
        messageTranslator = new MessageTranslator(localeProvider, i18nProvider);
        commonHttpClient = httpClientFactory.getCommonHttpClient();
        this.envoyHostAddressCache = envoyHostAddressCache;
    }

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(final Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ENPHASE_ENVOY.equals(thingTypeUID)) {
            return new EnvoyBridgeHandler((Bridge) thing, commonHttpClient, envoyHostAddressCache);
        } else if (THING_TYPE_ENPHASE_INVERTER.equals(thingTypeUID)) {
            return new EnphaseInverterHandler(thing, messageTranslator);
        } else if (THING_TYPE_ENPHASE_RELAY.equals(thingTypeUID)) {
            return new EnphaseRelayHandler(thing, messageTranslator);
        }

        return null;
    }
}

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
package org.openhab.binding.androidtv.internal;

import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryServiceRegistry;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AndroidTVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.androidtv", service = ThingHandlerFactory.class)
public class AndroidTVHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GOOGLETV, THING_TYPE_SHIELDTV,
            THING_TYPE_PHILIPSTV);

    private final AndroidTVDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final AndroidTVTranslationProvider translationProvider;
    private final DiscoveryServiceRegistry discoveryServiceRegistry;
    private final AndroidTVDynamicStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public AndroidTVHandlerFactory(
            final @Reference AndroidTVDynamicCommandDescriptionProvider commandDescriptionProvider,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider,
            final @Reference DiscoveryServiceRegistry discoveryServiceRegistry,
            final @Reference AndroidTVDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.translationProvider = new AndroidTVTranslationProvider(i18nProvider, localeProvider);
        this.discoveryServiceRegistry = discoveryServiceRegistry;
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        return new AndroidTVHandler(thing, commandDescriptionProvider, translationProvider, discoveryServiceRegistry,
                stateDescriptionProvider, thingTypeUID);
    }
}

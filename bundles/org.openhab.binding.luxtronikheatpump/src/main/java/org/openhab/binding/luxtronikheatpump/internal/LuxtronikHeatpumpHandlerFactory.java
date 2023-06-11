/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.luxtronikheatpump.internal;

import static org.openhab.binding.luxtronikheatpump.internal.LuxtronikHeatpumpBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link LuxtronikHeatpumpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Stefan Giehl - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.luxtronikheatpump", service = ThingHandlerFactory.class)
public class LuxtronikHeatpumpHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HEATPUMP);
    private final LuxtronikTranslationProvider translationProvider;

    @Activate
    public LuxtronikHeatpumpHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider i18nProvider, ComponentContext componentContext) {
        super.activate(componentContext);
        this.translationProvider = new LuxtronikTranslationProvider(getBundleContext().getBundle(), i18nProvider,
                localeProvider);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HEATPUMP.equals(thingTypeUID)) {
            return new LuxtronikHeatpumpHandler(thing, translationProvider);
        }

        return null;
    }
}

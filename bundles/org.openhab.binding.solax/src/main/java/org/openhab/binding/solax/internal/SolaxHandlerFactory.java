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
package org.openhab.binding.solax.internal;

import static org.openhab.binding.solax.internal.SolaxBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.handlers.SolaxCloudHandler;
import org.openhab.binding.solax.internal.handlers.SolaxLocalAccessChargerHandler;
import org.openhab.binding.solax.internal.handlers.SolaxLocalAccessInverterHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link SolaxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.solax", service = ThingHandlerFactory.class)
public class SolaxHandlerFactory extends BaseThingHandlerFactory {

    private TranslationProvider i18nProvider;
    private TimeZoneProvider timeZoneProvider;
    private LocaleProvider localeProvider;

    @Activate
    public SolaxHandlerFactory(final @Reference TranslationProvider i18nProvider,
            final @Reference TimeZoneProvider timeZoneProvider, final @Reference LocaleProvider localeProvider) {
        this.i18nProvider = i18nProvider;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_LOCAL_CONNECT_INVERTER.equals(thingTypeUID)) {
            return new SolaxLocalAccessInverterHandler(thing, i18nProvider, timeZoneProvider);
        } else if (THING_TYPE_CLOUD_CONNECT_INVERTER.equals(thingTypeUID)) {
            return new SolaxCloudHandler(thing, i18nProvider, timeZoneProvider, localeProvider);
        } else if (THING_TYPE_LOCAL_CONNECT_CHARGER.equals(thingTypeUID)) {
            return new SolaxLocalAccessChargerHandler(thing, i18nProvider, timeZoneProvider);
        }
        return null;
    }
}

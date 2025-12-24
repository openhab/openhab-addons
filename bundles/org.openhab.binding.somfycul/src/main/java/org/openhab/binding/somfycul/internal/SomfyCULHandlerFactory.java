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
package org.openhab.binding.somfycul.internal;

import static org.openhab.binding.somfycul.internal.SomfyCULBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.serial.SerialPortManager;
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
 * The {@link SomfyCULHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marc Klasser - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.somfycul", service = ThingHandlerFactory.class)
public class SomfyCULHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(CUL_DEVICE_THING_TYPE,
            SOMFY_DEVICE_THING_TYPE);

    private final SerialPortManager serialPortManager;
    private final LocaleProvider localeProvider;
    private final TranslationProvider i18nProvider;

    @Activate
    public SomfyCULHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference LocaleProvider localeProvider, final @Reference TranslationProvider i18nProvider) {
        this.serialPortManager = serialPortManager;
        this.localeProvider = localeProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(CUL_DEVICE_THING_TYPE) && thing instanceof Bridge bridge) {
            return new CULHandler(bridge, serialPortManager, localeProvider, i18nProvider);
        } else if (thingTypeUID.equals(SOMFY_DEVICE_THING_TYPE)) {
            return new SomfyCULHandler(thing, localeProvider, i18nProvider);
        }

        return null;
    }
}

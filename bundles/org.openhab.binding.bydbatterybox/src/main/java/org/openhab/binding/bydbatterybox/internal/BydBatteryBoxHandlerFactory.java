/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bydbatterybox.internal;

import static org.openhab.binding.bydbatterybox.internal.BydBatteryBoxBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bydbatterybox.internal.handler.BydBatteryBoxHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link BydBatteryBoxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.bydbatterybox", service = ThingHandlerFactory.class)
public class BydBatteryBoxHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_HV_PREMIUM);

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Bundle bundle;

    @Activate
    public BydBatteryBoxHandlerFactory(@Reference TranslationProvider i18nProvider,
            @Reference LocaleProvider localeProvider, ComponentContext componentContext) {
        super.activate(componentContext);
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.bundle = componentContext.getBundleContext().getBundle();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_HV_PREMIUM.equals(thing.getThingTypeUID())) {
            return new BydBatteryBoxHandler(thing, i18nProvider, localeProvider, bundle);
        }
        return null;
    }
}

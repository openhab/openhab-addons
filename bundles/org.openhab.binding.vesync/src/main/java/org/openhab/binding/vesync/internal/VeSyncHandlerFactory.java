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
package org.openhab.binding.vesync.internal;

import static org.openhab.binding.vesync.internal.VeSyncConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.vesync.internal.handlers.VeSyncBridgeHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirHumidifierHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceAirPurifierHandler;
import org.openhab.binding.vesync.internal.handlers.VeSyncDeviceOutletHandler;
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
 * The {@link org.openhab.binding.vesync.internal.VeSyncHandlerFactory} is responsible for creating
 * things and thing handlers.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.vesync", service = ThingHandlerFactory.class)
public class VeSyncHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE,
            THING_TYPE_AIR_PURIFIER, THING_TYPE_AIR_HUMIDIFIER, THING_TYPE_OUTLET);

    private final HttpClientFactory httpClientFactory;
    private final TranslationProvider translationProvider;
    private final LocaleProvider localeProvider;

    @Activate
    public VeSyncHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider translationProvider, @Reference LocaleProvider localeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.translationProvider = translationProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (VeSyncDeviceAirPurifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new VeSyncDeviceAirPurifierHandler(thing, translationProvider, localeProvider);
        } else if (VeSyncDeviceAirHumidifierHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new VeSyncDeviceAirHumidifierHandler(thing, translationProvider, localeProvider);
        } else if (VeSyncDeviceOutletHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new VeSyncDeviceOutletHandler(thing, translationProvider, localeProvider);
        } else if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            return new VeSyncBridgeHandler((Bridge) thing, httpClientFactory, translationProvider, localeProvider);
        }

        return null;
    }
}

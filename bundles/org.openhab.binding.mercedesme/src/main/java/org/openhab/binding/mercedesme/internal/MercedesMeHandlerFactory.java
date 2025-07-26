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
package org.openhab.binding.mercedesme.internal;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mercedesme.internal.discovery.MercedesMeDiscoveryService;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.binding.mercedesme.internal.handler.VehicleHandler;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MercedesMeHandlerFactory} is responsible for creating thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.mercedesme", service = ThingHandlerFactory.class)
public class MercedesMeHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BEV, THING_TYPE_COMB,
            THING_TYPE_HYBRID, THING_TYPE_ACCOUNT);

    private final HttpClientFactory httpClientFactory;
    private final LocaleProvider localeProvider;
    private final LocationProvider locationProvider;
    private final StorageService storageService;
    private final MercedesMeDiscoveryService discoveryService;
    private final MercedesMeCommandOptionProvider mmcop;
    private final MercedesMeStateOptionProvider mmsop;

    public static String ohVersion = "unknown";

    @Activate
    public MercedesMeHandlerFactory(@Reference HttpClientFactory hcf, @Reference StorageService storageService,
            final @Reference LocaleProvider lp, final @Reference LocationProvider locationP,
            final @Reference TimeZoneProvider tzp, final @Reference MercedesMeCommandOptionProvider cop,
            final @Reference MercedesMeStateOptionProvider sop, final @Reference UnitProvider up,
            final @Reference MercedesMeDiscoveryService discoveryService) {
        this.storageService = storageService;
        localeProvider = lp;
        locationProvider = locationP;
        mmcop = cop;
        mmsop = sop;
        this.discoveryService = discoveryService;
        Utils.initialize(tzp, lp);
        Mapper.initialize(up);
        httpClientFactory = hcf;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        Bundle[] bundleList = this.getBundleContext().getBundles();
        for (int i = 0; i < bundleList.length; i++) {
            if ("org.openhab.binding.mercedesme".equals(bundleList[i].getSymbolicName())) {
                ohVersion = bundleList[i].getVersion().toString();
            }
        }

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            return new AccountHandler((Bridge) thing, discoveryService, httpClientFactory.getCommonHttpClient(),
                    localeProvider, storageService);
        } else if (THING_TYPE_BEV.equals(thingTypeUID) || THING_TYPE_COMB.equals(thingTypeUID)
                || THING_TYPE_HYBRID.equals(thingTypeUID)) {
            return new VehicleHandler(thing, locationProvider, mmcop, mmsop);
        }
        return null;
    }

    public static String getVersion() {
        return ohVersion;
    }
}

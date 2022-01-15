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
package org.openhab.binding.mielecloud.internal.handler;

import static org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.*;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.webservice.DefaultMieleWebserviceFactory;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebserviceConfiguration;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebserviceFactory;
import org.openhab.binding.mielecloud.internal.webservice.language.CombiningLanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.language.OpenHabLanguageProvider;
import org.openhab.core.i18n.LocaleProvider;
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
 * Factory producing the {@link ThingHandler}s for all things supported by this binding.
 *
 * @author Roland Edelhoff - Initial contribution
 * @author Björn Lange - Added language provider, added support for multiple bridges
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.mielecloud")
public class MieleHandlerFactory extends BaseThingHandlerFactory {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_BRIDGE, THING_TYPE_WASHING_MACHINE,
            THING_TYPE_WASHER_DRYER, THING_TYPE_COFFEE_SYSTEM, THING_TYPE_FRIDGE_FREEZER, THING_TYPE_FRIDGE,
            THING_TYPE_FREEZER, THING_TYPE_OVEN, THING_TYPE_WINE_STORAGE, THING_TYPE_HOB, THING_TYPE_DRYER,
            THING_TYPE_DISHWASHER, THING_TYPE_HOOD, THING_TYPE_DISH_WARMER, THING_TYPE_ROBOTIC_VACUUM_CLEANER);

    private final HttpClientFactory httpClientFactory;
    private final OAuthTokenRefresher tokenRefresher;
    private final LocaleProvider localeProvider;

    private final MieleWebserviceFactory webserviceFactory = new DefaultMieleWebserviceFactory();

    @Activate
    public MieleHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference OAuthTokenRefresher tokenRefresher, @Reference LocaleProvider localeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.tokenRefresher = tokenRefresher;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    @Nullable
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return createBridgeHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WASHING_MACHINE) || thingTypeUID.equals(THING_TYPE_WASHER_DRYER)) {
            return new WashingDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_COFFEE_SYSTEM)) {
            return new CoffeeSystemThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_FRIDGE_FREEZER) || thingTypeUID.equals(THING_TYPE_FRIDGE)
                || thingTypeUID.equals(THING_TYPE_FREEZER)) {
            return new CoolingDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WINE_STORAGE)) {
            return new WineStorageDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_OVEN)) {
            return new OvenDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HOB)) {
            return new HobDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DISHWASHER)) {
            return new DishwasherDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DRYER)) {
            return new DryerDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_HOOD)) {
            return new HoodDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DISH_WARMER)) {
            return new DishWarmerDeviceThingHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_ROBOTIC_VACUUM_CLEANER)) {
            return new RoboticVacuumCleanerDeviceThingHandler(thing);
        }

        return null;
    }

    private ThingHandler createBridgeHandler(Thing thing) {
        CombiningLanguageProvider languageProvider = getLanguageProvider();
        Function<ScheduledExecutorService, MieleWebservice> webserviceFactoryFunction = scheduler -> webserviceFactory
                .create(MieleWebserviceConfiguration.builder().withHttpClientFactory(httpClientFactory)
                        .withLanguageProvider(languageProvider).withTokenRefresher(tokenRefresher)
                        .withServiceHandle(thing.getUID().getAsString()).withScheduler(scheduler).build());

        return new MieleBridgeHandler((Bridge) thing, webserviceFactoryFunction, tokenRefresher, languageProvider);
    }

    private CombiningLanguageProvider getLanguageProvider() {
        return new CombiningLanguageProvider(null, new OpenHabLanguageProvider(localeProvider));
    }
}

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
package org.openhab.binding.miele.internal;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.miele.internal.handler.CoffeeMachineHandler;
import org.openhab.binding.miele.internal.handler.DishwasherHandler;
import org.openhab.binding.miele.internal.handler.FridgeFreezerHandler;
import org.openhab.binding.miele.internal.handler.FridgeHandler;
import org.openhab.binding.miele.internal.handler.HobHandler;
import org.openhab.binding.miele.internal.handler.HoodHandler;
import org.openhab.binding.miele.internal.handler.MieleApplianceHandler;
import org.openhab.binding.miele.internal.handler.MieleBridgeHandler;
import org.openhab.binding.miele.internal.handler.OvenHandler;
import org.openhab.binding.miele.internal.handler.TumbleDryerHandler;
import org.openhab.binding.miele.internal.handler.WashingMachineHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MieleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 * @author Jacob Laursen - Refactored to use framework's HTTP client
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.miele")
public class MieleHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(MieleBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    MieleApplianceHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final HttpClient httpClient;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final TimeZoneProvider timeZoneProvider;

    @Activate
    public MieleHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider,
            final @Reference TimeZoneProvider timeZoneProvider, ComponentContext componentContext) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (MieleBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID mieleBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, mieleBridgeUID, null);
        }
        if (MieleApplianceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID mieleApplianceUID = getApplianceUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, mieleApplianceUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the miele binding.");
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (MieleBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            return new MieleBridgeHandler((Bridge) thing, httpClient);
        } else if (MieleApplianceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            if (thing.getThingTypeUID().equals(THING_TYPE_HOOD)) {
                return new HoodHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGEFREEZER)) {
                return new FridgeFreezerHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGE)) {
                return new FridgeHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_OVEN)) {
                return new OvenHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_HOB)) {
                return new HobHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_WASHINGMACHINE)) {
                return new WashingMachineHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DRYER)) {
                return new TumbleDryerHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DISHWASHER)) {
                return new DishwasherHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_COFFEEMACHINE)) {
                return new CoffeeMachineHandler(thing, i18nProvider, localeProvider, timeZoneProvider);
            }
        }

        return null;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String hostID = (String) configuration.get(HOST);
            thingUID = new ThingUID(thingTypeUID, hostID);
        }
        return thingUID;
    }

    private ThingUID getApplianceUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        String applianceId = (String) configuration.get(APPLIANCE_ID);

        if (thingUID == null) {
            if (bridgeUID == null) {
                thingUID = new ThingUID(thingTypeUID, applianceId);
            } else {
                thingUID = new ThingUID(thingTypeUID, bridgeUID, applianceId);
            }
        }
        return thingUID;
    }
}

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
package org.openhab.binding.miele.internal;

import static org.openhab.binding.miele.internal.MieleBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.miele.internal.discovery.MieleApplianceDiscoveryService;
import org.openhab.binding.miele.internal.handler.CoffeeMachineHandler;
import org.openhab.binding.miele.internal.handler.DishWasherHandler;
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
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MieleHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.miele")
public class MieleHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(MieleBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    MieleApplianceHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Activate
    public MieleHandlerFactory(final @Reference TranslationProvider i18nProvider,
            final @Reference LocaleProvider localeProvider, ComponentContext componentContext) {
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
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
    protected ThingHandler createHandler(Thing thing) {
        if (MieleBridgeHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            MieleBridgeHandler handler = new MieleBridgeHandler((Bridge) thing);
            registerApplianceDiscoveryService(handler);
            return handler;
        } else if (MieleApplianceHandler.SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            if (thing.getThingTypeUID().equals(THING_TYPE_HOOD)) {
                return new HoodHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGEFREEZER)) {
                return new FridgeFreezerHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_FRIDGE)) {
                return new FridgeHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_OVEN)) {
                return new OvenHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_HOB)) {
                return new HobHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_WASHINGMACHINE)) {
                return new WashingMachineHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DRYER)) {
                return new TumbleDryerHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_DISHWASHER)) {
                return new DishWasherHandler(thing, i18nProvider, localeProvider);
            }
            if (thing.getThingTypeUID().equals(THING_TYPE_COFFEEMACHINE)) {
                return new CoffeeMachineHandler(thing, i18nProvider, localeProvider);
            }
        }

        return null;
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            String hostID = (String) configuration.get(HOST);
            thingUID = new ThingUID(thingTypeUID, hostID);
        }
        return thingUID;
    }

    private ThingUID getApplianceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String applianceId = (String) configuration.get(APPLIANCE_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, applianceId, bridgeUID.getId());
        }
        return thingUID;
    }

    private synchronized void registerApplianceDiscoveryService(MieleBridgeHandler bridgeHandler) {
        MieleApplianceDiscoveryService discoveryService = new MieleApplianceDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MieleBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                MieleApplianceDiscoveryService service = (MieleApplianceDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (service != null) {
                    service.deactivate();
                }
            }
        }
    }
}

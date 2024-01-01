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
package org.openhab.binding.nobohub.internal;

import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.SUPPORTED_THING_TYPES_UIDS;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_COMPONENT;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_HUB;
import static org.openhab.binding.nobohub.internal.NoboHubBindingConstants.THING_TYPE_ZONE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.discovery.NoboThingDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NoboHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jørgen Austvik - Initial contribution
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.nobohub", service = ThingHandlerFactory.class)
public class NoboHubHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(NoboHubHandlerFactory.class);
    private final Map<ThingTypeUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_TYPES_UIDS = new HashSet<>(List.of(THING_TYPE_HUB));
    private @NonNullByDefault({}) WeekProfileStateDescriptionOptionsProvider stateDescriptionOptionsProvider;

    private final NoboHubTranslationProvider i18nProvider;

    @Activate
    public NoboHubHandlerFactory(
            final @Reference WeekProfileStateDescriptionOptionsProvider stateDescriptionOptionsProvider,
            final @Reference NoboHubTranslationProvider i18nProvider) {
        this.stateDescriptionOptionsProvider = stateDescriptionOptionsProvider;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HUB.equals(thingTypeUID)) {
            NoboHubBridgeHandler handler = new NoboHubBridgeHandler((Bridge) thing);
            registerDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            logger.debug("Setting WeekProfileStateDescriptionOptionsProvider for: {}", thing.getLabel());
            return new ZoneHandler(thing, i18nProvider, stateDescriptionOptionsProvider);
        } else if (THING_TYPE_COMPONENT.equals(thingTypeUID)) {
            return new ComponentHandler(thing, i18nProvider);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof NoboHubBridgeHandler bridgeHandler) {
            unregisterDiscoveryService(bridgeHandler);
        }
    }

    private synchronized void registerDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        NoboThingDiscoveryService discoveryService = new NoboThingDiscoveryService(bridgeHandler);
        bridgeHandler.setDicsoveryService(discoveryService);
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getThingTypeUID(), getBundleContext()
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDiscoveryService(NoboHubBridgeHandler bridgeHandler) {
        try {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs
                    .remove(bridgeHandler.getThing().getThingTypeUID());
            if (null != serviceReg) {
                NoboThingDiscoveryService service = (NoboThingDiscoveryService) getBundleContext()
                        .getService(serviceReg.getReference());
                serviceReg.unregister();
                if (null != service) {
                    service.deactivate();
                }
            }
        } catch (IllegalArgumentException iae) {
            logger.debug("Failed to unregister service", iae);
        }
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(WeekProfileStateDescriptionOptionsProvider provider) {
        this.stateDescriptionOptionsProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(WeekProfileStateDescriptionOptionsProvider provider) {
        this.stateDescriptionOptionsProvider = null;
    }
}

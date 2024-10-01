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
package org.openhab.binding.harmonyhub.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.harmonyhub.internal.discovery.HarmonyDeviceDiscoveryService;
import org.openhab.binding.harmonyhub.internal.handler.HarmonyDeviceHandler;
import org.openhab.binding.harmonyhub.internal.handler.HarmonyHubHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HarmonyHubHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dan Cunningham - Initial contribution
 * @author Wouter Born - Add null annotations
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.harmonyhub")
public class HarmonyHubHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(HarmonyHubHandler.SUPPORTED_THING_TYPES_UIDS.stream(),
                    HarmonyDeviceHandler.SUPPORTED_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final HttpClient httpClient;

    private final HarmonyHubDynamicTypeProvider dynamicTypeProvider;

    @Activate
    public HarmonyHubHandlerFactory(@Reference final HttpClientFactory httpClientFactory,
            @Reference HarmonyHubDynamicTypeProvider dynamicTypeProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.dynamicTypeProvider = dynamicTypeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HarmonyHubBindingConstants.HARMONY_HUB_THING_TYPE)) {
            HarmonyHubHandler harmonyHubHandler = new HarmonyHubHandler((Bridge) thing, dynamicTypeProvider,
                    httpClient);
            registerHarmonyDeviceDiscoveryService(harmonyHubHandler);
            return harmonyHubHandler;
        }

        if (thingTypeUID.equals(HarmonyHubBindingConstants.HARMONY_DEVICE_THING_TYPE)) {
            return new HarmonyDeviceHandler(thing, dynamicTypeProvider);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof HarmonyHubHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }

    /**
     * Adds HarmonyHubHandler to the discovery service to find Harmony Devices
     *
     * @param harmonyHubHandler
     */
    private synchronized void registerHarmonyDeviceDiscoveryService(HarmonyHubHandler harmonyHubHandler) {
        HarmonyDeviceDiscoveryService discoveryService = new HarmonyDeviceDiscoveryService(harmonyHubHandler);
        this.discoveryServiceRegs.put(harmonyHubHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}

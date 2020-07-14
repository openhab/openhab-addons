/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tr064.internal;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.tr064.profile.phonebook.PhonebookProvider;
import org.openhab.binding.tr064.profile.phonebook.PhonebookProfileFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * The {@link Tr064HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true, service = { ThingHandlerFactory.class }, configurationPid = "binding.tr064")
public class Tr064HandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(Tr064RootHandler.SUPPORTED_THING_TYPES.stream(), Tr064SubHandler.SUPPORTED_THING_TYPES.stream())
            .collect(Collectors.toSet());

    private final HttpClient httpClient;
    private final Tr064ChannelTypeProvider channelTypeProvider;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private @Nullable PhonebookProfileFactory phonebookProfileFactory = null;

    private final Set<PhonebookProvider> phonebookProviders = ConcurrentHashMap.newKeySet();
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new ConcurrentHashMap<>();

    @Activate
    public Tr064HandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference Tr064ChannelTypeProvider channelTypeProvider) {
        httpClient = httpClientFactory.getCommonHttpClient();
        this.channelTypeProvider = channelTypeProvider;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setPhonebookTransformationProfileFactory(
            PhonebookProfileFactory phonebookProfileFactory) {
        this.phonebookProfileFactory = phonebookProfileFactory;
        phonebookProviders.forEach(phonebookProfileFactory::registerPhonebookProvider);
    }

    public void unsetPhonebookTransformationProfileFactory(
            PhonebookProfileFactory phonebookProfileFactory) {
        this.phonebookProfileFactory = null;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (Tr064RootHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            Tr064RootHandler handler = new Tr064RootHandler((Bridge) thing, httpClient);
            registerDeviceDiscoveryService(handler);
            phonebookProviders.add(handler);
            PhonebookProfileFactory phonebookProfileFactory = this.phonebookProfileFactory;
            if (phonebookProfileFactory != null) {
                phonebookProfileFactory.registerPhonebookProvider(handler);
            }
            return handler;
        } else if (Tr064SubHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new Tr064SubHandler(thing);
        }

        return null;
    }

    @Override
    @SuppressWarnings("null")
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof Tr064RootHandler) {
            final ThingUID thingUID = thingHandler.getThing().getUID();
            final ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingUID);
            if (serviceReg != null) {
                serviceReg.unregister();
            }
            phonebookProviders.remove(thingHandler);
            PhonebookProfileFactory phonebookProfileFactory = this.phonebookProfileFactory;
            if (phonebookProfileFactory != null) {
                phonebookProfileFactory.unregisterPhonebookProvider((Tr064RootHandler) thingHandler);
            }
        }
    }

    /**
     * create and register a new discovery service for the given bridge handler
     *
     * @param bridgeHandler the bridgehandler (root device)
     */
    private void registerDeviceDiscoveryService(Tr064RootHandler bridgeHandler) {
        Tr064DiscoveryService discoveryService = new Tr064DiscoveryService(bridgeHandler);
        discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}

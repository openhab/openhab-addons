/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.discovery.SmartHomeDevicesDiscovery;
import org.openhab.binding.amazonechocontrol.internal.handler.AccountHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.EchoHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.FlashBriefingProfileHandler;
import org.openhab.binding.amazonechocontrol.internal.handler.SmartHomeDeviceHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
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
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AmazonEchoControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Geramb - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class,
        AmazonEchoControlHandlerFactory.class }, configurationPid = "binding.amazonechocontrol")
@NonNullByDefault
public class AmazonEchoControlHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(AmazonEchoControlHandlerFactory.class);
    private final Map<ThingUID, List<ServiceRegistration<?>>> discoveryServiceRegistrations = new HashMap<>();

    private final Set<AccountHandler> accountHandlers = new HashSet<>();
    private final HttpService httpService;
    private final StorageService storageService;
    private final BindingServlet bindingServlet;
    private final Gson gson;

    @Activate
    public AmazonEchoControlHandlerFactory(@Reference HttpService httpService,
            @Reference StorageService storageService) {
        this.storageService = storageService;
        this.httpService = httpService;
        this.gson = new Gson();
        this.bindingServlet = new BindingServlet(httpService);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_ECHO_THING_TYPES_UIDS.contains(thingTypeUID)
                || SUPPORTED_SMART_HOME_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        bindingServlet.dispose();
        super.deactivate(componentContext);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ACCOUNT)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            AccountHandler bridgeHandler = new AccountHandler((Bridge) thing, httpService, storage, gson);
            accountHandlers.add(bridgeHandler);
            registerDiscoveryService(bridgeHandler);
            bindingServlet.addAccountThing(thing);
            return bridgeHandler;
        } else if (thingTypeUID.equals(THING_TYPE_FLASH_BRIEFING_PROFILE)) {
            Storage<String> storage = storageService.getStorage(thing.getUID().toString(),
                    String.class.getClassLoader());
            return new FlashBriefingProfileHandler(thing, storage);
        } else if (SUPPORTED_ECHO_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new EchoHandler(thing, gson);
        } else if (SUPPORTED_SMART_HOME_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new SmartHomeDeviceHandler(thing, gson);
        }
        return null;
    }

    private synchronized void registerDiscoveryService(AccountHandler bridgeHandler) {
        List<ServiceRegistration<?>> discoveryServiceRegistration = Objects.requireNonNull(discoveryServiceRegistrations
                .computeIfAbsent(bridgeHandler.getThing().getUID(), k -> new ArrayList<>()));
        SmartHomeDevicesDiscovery smartHomeDevicesDiscovery = new SmartHomeDevicesDiscovery(bridgeHandler);
        smartHomeDevicesDiscovery.activate();
        discoveryServiceRegistration.add(bundleContext.registerService(DiscoveryService.class.getName(),
                smartHomeDevicesDiscovery, new Hashtable<>()));

        AmazonEchoDiscovery discoveryService = new AmazonEchoDiscovery(bridgeHandler);
        discoveryService.activate();
        discoveryServiceRegistration.add(
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AccountHandler) {
            accountHandlers.remove(thingHandler);
            BindingServlet bindingServlet = this.bindingServlet;
            bindingServlet.removeAccountThing(thingHandler.getThing());

            List<ServiceRegistration<?>> discoveryServiceRegistration = discoveryServiceRegistrations
                    .remove(thingHandler.getThing().getUID());
            if (discoveryServiceRegistration != null) {
                discoveryServiceRegistration.forEach(serviceReg -> {
                    AbstractDiscoveryService service = (AbstractDiscoveryService) bundleContext
                            .getService(serviceReg.getReference());
                    serviceReg.unregister();
                    if (service != null) {
                        if (service instanceof AmazonEchoDiscovery discovery) {
                            discovery.deactivate();
                        } else if (service instanceof SmartHomeDevicesDiscovery discovery) {
                            discovery.deactivate();
                        } else {
                            logger.warn("Found unknown discovery-service instance: {}", service);
                        }
                    }
                });
            }
        }
    }

    public Set<AccountHandler> getAccountHandlers() {
        return accountHandlers;
    }
}

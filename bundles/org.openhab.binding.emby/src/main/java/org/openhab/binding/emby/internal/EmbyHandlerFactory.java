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
package org.openhab.binding.emby.internal;

import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_CONTROLLER;
import static org.openhab.binding.emby.internal.EmbyBindingConstants.THING_TYPE_EMBY_DEVICE;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.emby.internal.discovery.EmbyClientDiscoveryService;
import org.openhab.binding.emby.internal.handler.EmbyBridgeHandler;
import org.openhab.binding.emby.internal.handler.EmbyDeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EmbyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.emby", configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class EmbyHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(EmbyHandlerFactory.class);

    private WebSocketFactory webSocketClientFactory;
    private TranslationProvider i18nProvider;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_EMBY_CONTROLLER, THING_TYPE_EMBY_DEVICE).collect(Collectors.toSet()));

    @Reference(target = "(component.factory=emby:client)")
    private @Nullable ComponentFactory<DiscoveryService> discoveryFactory;
    private final Map<ThingUID, ComponentInstance<DiscoveryService>> discoveryInstances = new HashMap<>();

    @Activate
    public EmbyHandlerFactory(@Reference WebSocketFactory webSocketClientFactory, ComponentContext componentContext,
            @Reference TranslationProvider i18nProvider) {
        super.activate(componentContext);
        this.webSocketClientFactory = webSocketClientFactory;
        this.i18nProvider = i18nProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_EMBY_DEVICE.equals(thing.getThingTypeUID())) {
            logger.debug("Creating EMBY Device Handler for {}.", thing.getLabel());
            return new EmbyDeviceHandler(thing, this.i18nProvider);
        }

        if (THING_TYPE_EMBY_CONTROLLER.equals(thing.getThingTypeUID())) {
            EmbyBridgeHandler bridgeHandler = new EmbyBridgeHandler((Bridge) thing,
                    webSocketClientFactory.getCommonWebSocketClient(), this.i18nProvider);
            Dictionary<String, Object> cfg = new Hashtable<>();
            cfg.put("bridgeUID", bridgeHandler.getThing().getUID().toString());

            ComponentFactory<DiscoveryService> factory = Objects.requireNonNull(discoveryFactory,
                    "discoveryFactory must be injected");
            @SuppressWarnings("null")
            ComponentInstance<DiscoveryService> ci = factory.newInstance(cfg);
            EmbyClientDiscoveryService discovery = (EmbyClientDiscoveryService) ci.getInstance();
            discovery.setBridge(bridgeHandler);
            bridgeHandler.setClientDiscoveryService(discovery);
            discoveryInstances.put(bridgeHandler.getThing().getUID(), ci);

            return bridgeHandler;
        }

        return null; // unknown thing-type
    }

    @Override
    public void removeHandler(ThingHandler handler) {
        ThingUID uid = handler.getThing().getUID();

        ComponentInstance<DiscoveryService> ci = discoveryInstances.remove(uid);
        if (ci != null) {
            EmbyClientDiscoveryService discovery = (EmbyClientDiscoveryService) ci.getInstance();

            // undo the manual wiring done in createHandler(…)
            if (handler instanceof EmbyBridgeHandler bridge) {
                discovery.clearBridge(bridge);
            }

            ci.dispose(); // shuts the DS component down
        }

        super.removeHandler(handler);
    }
}

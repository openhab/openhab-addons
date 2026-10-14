/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.growatt.internal.factory;

import static org.openhab.binding.growatt.internal.GrowattBindingConstants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.growatt.internal.discovery.GrowattDiscoveryService;
import org.openhab.binding.growatt.internal.handler.GrowattBridgeHandler;
import org.openhab.binding.growatt.internal.handler.GrowattInverterHandler;
import org.openhab.binding.growatt.internal.servlet.GrowattHttpServlet;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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
import org.osgi.service.servlet.whiteboard.HttpWhiteboardConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.Servlet;

/**
 * The {@link GrowattHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.growatt", service = ThingHandlerFactory.class)
public class GrowattHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_INVERTER);
    private static final String SERVLET_PATTERN = GrowattHttpServlet.PATH;

    private final Logger logger = LoggerFactory.getLogger(GrowattHandlerFactory.class);

    private final HttpClientFactory httpClientFactory;
    private final TranslationProvider i18nProvider;
    private final LocaleProvider localeProvider;
    private final Set<ThingUID> bridges = Collections.synchronizedSet(new HashSet<>());
    private final GrowattHttpServlet httpServlet = new GrowattHttpServlet();

    private @Nullable GrowattDiscoveryService discoveryService;
    private @Nullable ServiceRegistration<?> discoveryServiceRegistration;
    private @Nullable ServiceRegistration<Servlet> servletRegistration;

    @Activate
    public GrowattHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TranslationProvider i18nProvider, @Reference LocaleProvider localeProvider) {
        this.httpClientFactory = httpClientFactory;
        this.i18nProvider = i18nProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        Hashtable<String, Object> servletProperties = new Hashtable<>();
        servletProperties.put(HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN, SERVLET_PATTERN);
        servletRegistration = bundleContext.registerService(Servlet.class, httpServlet, servletProperties);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            discoveryRegister();
            bridges.add(thing.getUID());
            return new GrowattBridgeHandler((Bridge) thing, Objects.requireNonNull(httpServlet),
                    Objects.requireNonNull(discoveryService), httpClientFactory);
        }

        if (THING_TYPE_INVERTER.equals(thingTypeUID)) {
            return new GrowattInverterHandler(thing);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        bridges.clear();
        discoveryUnregister();
        ServiceRegistration<Servlet> servletRegistration = this.servletRegistration;
        if (servletRegistration != null) {
            servletRegistration.unregister();
            this.servletRegistration = null;
        }
        super.deactivate(componentContext);
    }

    private void discoveryRegister() {
        GrowattDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService == null) {
            discoveryService = new GrowattDiscoveryService(i18nProvider, localeProvider);
            this.discoveryService = discoveryService;
        }
        ServiceRegistration<?> temp = this.discoveryServiceRegistration;
        if (temp == null) {
            temp = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
            this.discoveryServiceRegistration = temp;
        }
    }

    private void discoveryUnregister() {
        ServiceRegistration<?> discoveryServiceRegistration = this.discoveryServiceRegistration;
        if (discoveryServiceRegistration != null) {
            discoveryServiceRegistration.unregister();
        }
        this.discoveryService = null;
        this.discoveryServiceRegistration = null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof GrowattBridgeHandler) {
            bridges.remove(thingHandler.getThing().getUID());
            if (bridges.isEmpty()) {
                discoveryUnregister();
            }
        }
        super.removeHandler(thingHandler);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
}

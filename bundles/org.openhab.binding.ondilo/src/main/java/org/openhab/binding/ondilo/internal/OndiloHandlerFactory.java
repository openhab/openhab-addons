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
package org.openhab.binding.ondilo.internal;

import static org.openhab.binding.ondilo.internal.OndiloBindingConstants.*;

import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ondilo.internal.discovery.OndiloDiscoveryService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
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
 * The {@link OndiloHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ondilo", service = ThingHandlerFactory.class)
public class OndiloHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(OndiloHandlerFactory.class);
    private final OAuthFactory oAuthFactory;
    private final LocaleProvider localeProvider;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_ONDILO);
    private @Nullable ServiceRegistration<?> ondiloDiscoveryServiceRegistration;
    private @Nullable OndiloDiscoveryService discoveryService;

    @Activate
    public OndiloHandlerFactory(@Reference OAuthFactory oAuthFactory, @Reference LocaleProvider localeProvider) {
        this.oAuthFactory = oAuthFactory;
        this.localeProvider = localeProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID)) {
            OndiloBridgeHandler handler = new OndiloBridgeHandler((Bridge) thing, this.oAuthFactory);
            registerOndiloDiscoveryService(handler);
            return handler;
        } else if (THING_TYPE_ONDILO.equals(thingTypeUID)) {
            return new OndiloHandler(thing, localeProvider);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof OndiloBridgeHandler) {
            unregisterOndiloDiscoveryService();
        }
    }

    private void registerOndiloDiscoveryService(OndiloBridgeHandler handler) {
        logger.trace("Registering OndiloDiscoveryService for {}", handler.getThing().getUID());
        OndiloDiscoveryService discoveryService = new OndiloDiscoveryService(handler);
        this.ondiloDiscoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
        discoveryService.startBackgroundDiscovery();
        this.discoveryService = discoveryService;
    }

    private void unregisterOndiloDiscoveryService() {
        logger.trace("Unregistering OndiloDiscoveryService");
        OndiloDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.stopBackgroundDiscovery();
            this.discoveryService = null;
        }
        ServiceRegistration<?> ondiloDiscoveryServiceRegistration = this.ondiloDiscoveryServiceRegistration;
        if (ondiloDiscoveryServiceRegistration != null) {
            ondiloDiscoveryServiceRegistration.unregister();
            this.ondiloDiscoveryServiceRegistration = null;
        }
    }
}

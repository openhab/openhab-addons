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
package org.openhab.binding.velux.internal.factory;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.discovery.VeluxDiscoveryService;
import org.openhab.binding.velux.internal.handler.VeluxBindingHandler;
import org.openhab.binding.velux.internal.handler.VeluxBridgeHandler;
import org.openhab.binding.velux.internal.handler.VeluxHandler;
import org.openhab.binding.velux.internal.utils.Localization;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VeluxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, name = "binding.velux")
public class VeluxHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(VeluxHandlerFactory.class);

    // Class internal

    private @Nullable ServiceRegistration<?> discoveryServiceRegistration = null;
    private @Nullable VeluxDiscoveryService discoveryService = null;

    private Set<VeluxBindingHandler> veluxBindingHandlers = new HashSet<>();
    private Set<VeluxBridgeHandler> veluxBridgeHandlers = new HashSet<>();
    private Set<VeluxHandler> veluxHandlers = new HashSet<>();

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;

    private @Nullable static VeluxHandlerFactory activeInstance = null;

    // Private

    private void registerDeviceDiscoveryService(VeluxBridgeHandler bridgeHandler) {
        logger.trace("registerDeviceDiscoveryService({}) called.", bridgeHandler);
        VeluxDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService == null) {
            discoveryService = this.discoveryService = new VeluxDiscoveryService(localization);
        }
        discoveryService.addBridge(bridgeHandler);
        if (discoveryServiceRegistration == null) {
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<>());
        }
    }

    private synchronized void unregisterDeviceDiscoveryService(VeluxBridgeHandler bridgeHandler) {
        logger.trace("unregisterDeviceDiscoveryService({}) called.", bridgeHandler);
        VeluxDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.removeBridge(bridgeHandler);
            if (discoveryService.isEmpty()) {
                ServiceRegistration<?> discoveryServiceRegistration = this.discoveryServiceRegistration;
                if (discoveryServiceRegistration != null) {
                    discoveryServiceRegistration.unregister();
                    this.discoveryServiceRegistration = null;
                }
            }
        }
    }

    private @Nullable ThingHandler createBindingHandler(Thing thing) {
        logger.trace("createBindingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        VeluxBindingHandler veluxBindingHandler = new VeluxBindingHandler(thing, localization);
        veluxBindingHandlers.add(veluxBindingHandler);
        return veluxBindingHandler;
    }

    private @Nullable ThingHandler createBridgeHandler(Thing thing) {
        logger.trace("createBridgeHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        VeluxBridgeHandler veluxBridgeHandler = new VeluxBridgeHandler((Bridge) thing, localization);
        veluxBridgeHandlers.add(veluxBridgeHandler);
        registerDeviceDiscoveryService(veluxBridgeHandler);
        return veluxBridgeHandler;
    }

    private @Nullable ThingHandler createThingHandler(Thing thing) {
        logger.trace("createThingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        VeluxHandler veluxHandler = new VeluxHandler(thing, localization);
        veluxHandlers.add(veluxHandler);
        return veluxHandler;
    }

    private void updateBindingState() {
        veluxBindingHandlers.forEach((VeluxBindingHandler veluxBindingHandler) -> {
            veluxBindingHandler.updateBindingState(veluxBridgeHandlers.size(), veluxHandlers.size());
        });
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void updateLocalization() {
        if (localization == Localization.UNKNOWN && localeProvider != null && i18nProvider != null) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    // Constructor

    @Activate
    public VeluxHandlerFactory(final @Reference LocaleProvider givenLocaleProvider,
            final @Reference TranslationProvider givenI18nProvider) {
        logger.trace("VeluxHandlerFactory(locale={},translation={}) called.", givenLocaleProvider, givenI18nProvider);
        localeProvider = givenLocaleProvider;
        i18nProvider = givenI18nProvider;
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider givenLocaleProvider) {
        logger.trace("setLocaleProvider(): provided locale={}.", givenLocaleProvider);
        localeProvider = givenLocaleProvider;
        updateLocalization();
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider givenI18nProvider) {
        logger.trace("setTranslationProvider(): provided translation={}.", givenI18nProvider);
        i18nProvider = givenI18nProvider;
        updateLocalization();
    }

    // Utility methods

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = VeluxBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID)
                || VeluxBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)
                || VeluxBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID);
        logger.trace("supportsThingType({}) called and returns {}.", thingTypeUID, result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingHandler resultHandler = null;
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle Binding creation
        if (VeluxBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID)) {
            resultHandler = createBindingHandler(thing);
        } else
        // Handle Bridge creation
        if (VeluxBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)) {
            resultHandler = createBridgeHandler(thing);
        } else
        // Handle creation of Things behind the Bridge
        if (VeluxBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID)) {
            resultHandler = createThingHandler(thing);
        } else {
            logger.warn("createHandler({}) failed: ThingHandler not found for {}.", thingTypeUID, thing.getLabel());
        }
        updateBindingState();
        return resultHandler;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        // Handle Binding removal
        if (thingHandler instanceof VeluxBindingHandler) {
            logger.trace("removeHandler() removing information element '{}'.", thingHandler.toString());
            veluxBindingHandlers.remove(thingHandler);
        } else
        // Handle Bridge removal
        if (thingHandler instanceof VeluxBridgeHandler) {
            logger.trace("removeHandler() removing bridge '{}'.", thingHandler.toString());
            veluxBridgeHandlers.remove(thingHandler);
            unregisterDeviceDiscoveryService((VeluxBridgeHandler) thingHandler);
        } else
        // Handle removal of Things behind the Bridge
        if (thingHandler instanceof VeluxHandler) {
            logger.trace("removeHandler() removing thing '{}'.", thingHandler.toString());
            veluxHandlers.remove(thingHandler);
        }
        updateBindingState();
        super.removeHandler(thingHandler);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        activeInstance = this;
        super.activate(componentContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        activeInstance = null;
        super.deactivate(componentContext);
    }

    public static void refreshBindingInfo() {
        VeluxHandlerFactory instance = VeluxHandlerFactory.activeInstance;
        if (instance != null) {
            instance.updateBindingState();
        }
    }
}

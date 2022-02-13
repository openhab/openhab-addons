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
package org.openhab.binding.icloud.internal;

import static org.openhab.binding.icloud.internal.ICloudBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.icloud.internal.discovery.ICloudDeviceDiscovery;
import org.openhab.binding.icloud.internal.handler.ICloudAccountBridgeHandler;
import org.openhab.binding.icloud.internal.handler.ICloudDeviceHandler;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ICloudHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.icloud")
public class ICloudHandlerFactory extends BaseThingHandlerFactory {
    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();
    private LocaleProvider localeProvider;
    private TranslationProvider i18nProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ICLOUD)) {
            ICloudAccountBridgeHandler bridgeHandler = new ICloudAccountBridgeHandler((Bridge) thing);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        }

        if (thingTypeUID.equals(THING_TYPE_ICLOUDDEVICE)) {
            return new ICloudDeviceHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ICloudAccountBridgeHandler) {
            unregisterDeviceDiscoveryService((ICloudAccountBridgeHandler) thingHandler);
        }
    }

    private synchronized void registerDeviceDiscoveryService(ICloudAccountBridgeHandler bridgeHandler) {
        ICloudDeviceDiscovery discoveryService = new ICloudDeviceDiscovery(bridgeHandler, bundleContext.getBundle(),
                i18nProvider, localeProvider);
        discoveryService.activate();
        this.discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }

    private synchronized void unregisterDeviceDiscoveryService(ICloudAccountBridgeHandler bridgeHandler) {
        ServiceRegistration<?> serviceRegistration = this.discoveryServiceRegistrations
                .get(bridgeHandler.getThing().getUID());
        if (serviceRegistration != null) {
            ICloudDeviceDiscovery discoveryService = (ICloudDeviceDiscovery) bundleContext
                    .getService(serviceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            serviceRegistration.unregister();
            discoveryServiceRegistrations.remove(bridgeHandler.getThing().getUID());
        }
    }

    @Reference
    protected void setLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = localeProvider;
    }

    protected void unsetLocaleProvider(LocaleProvider localeProvider) {
        this.localeProvider = null;
    }

    @Reference
    public void setTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = i18nProvider;
    }

    public void unsetTranslationProvider(TranslationProvider i18nProvider) {
        this.i18nProvider = null;
    }
}

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
package org.openhab.binding.gree.internal;

import static org.openhab.binding.gree.internal.GreeBindingConstants.*;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.gree.internal.discovery.GreeDiscoveryService;
import org.openhab.binding.gree.internal.handler.GreeHandler;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link GreeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author John Cunha - Initial contribution
 * @author Markus Michels - Refactoring, adapted to OH 2.5x
 */
@NonNullByDefault
@Component(configurationPid = "binding." + BINDING_ID, service = ThingHandlerFactory.class)
public class GreeHandlerFactory extends BaseThingHandlerFactory {
    private @Nullable ServiceRegistration<?> serviceRegistration;
    private @Nullable LocaleProvider localeProvider;
    private @Nullable TranslationProvider i18nProvider;
    private @Nullable NetworkAddressService networkAddressService;

    @Override
    public void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        GreeDiscoveryService discoveryService = new GreeDiscoveryService(bundleContext.getBundle(), i18nProvider,
                localeProvider, networkAddressService);
        this.serviceRegistration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<String, Object>());
        discoveryService.activate();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_GREEAIRCON.equals(thing.getThingTypeUID())) {
            String defBroadcastIp = "";
            if (networkAddressService != null) {
                String baddress = networkAddressService.getConfiguredBroadcastAddress();
                defBroadcastIp = baddress != null ? baddress : "";
            }
            return new GreeHandler(thing, defBroadcastIp);
        }

        return null;
    }

    private synchronized void unregisterDeviceDiscoveryService() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            GreeDiscoveryService discoveryService = (GreeDiscoveryService) bundleContext
                    .getService(serviceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
        }
    }

    public void dispose() {
        unregisterDeviceDiscoveryService();
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

    @Reference
    public void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    public void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}

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
package org.openhab.binding.withings.internal;

import static org.openhab.binding.withings.internal.WithingsBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.withings.internal.discovery.WithingsDiscoveryService;
import org.openhab.binding.withings.internal.handler.PersonThingHandler;
import org.openhab.binding.withings.internal.handler.ScaleThingHandler;
import org.openhab.binding.withings.internal.handler.SleepMonitorThingHandler;
import org.openhab.binding.withings.internal.handler.WithingsBridgeHandler;
import org.openhab.binding.withings.internal.service.AccessTokenInitializableService;
import org.openhab.binding.withings.internal.service.AccessTokenServiceImpl;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.withings")
public class WithingsHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();
    private final AccessTokenInitializableService accessTokenService;
    private final HttpClient httpClient;
    private final LocaleProvider localeProvider;
    private final TranslationProvider translationProvider;

    @Activate
    public WithingsHandlerFactory(final @Reference OAuthFactory oAuthFactory,
            final @Reference HttpClientFactory httpClientFactory, final @Reference LocaleProvider localeProvider,
            final @Reference TranslationProvider translationProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.localeProvider = localeProvider;
        this.translationProvider = translationProvider;
        this.accessTokenService = new AccessTokenServiceImpl(oAuthFactory);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(APIBRIDGE_THING_TYPE)) {
            WithingsBridgeHandler bridgeHandler = new WithingsBridgeHandler((Bridge) thing, accessTokenService,
                    httpClient);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (thingTypeUID.equals(SCALE_THING_TYPE)) {
            return new ScaleThingHandler(thing);
        } else if (thingTypeUID.equals(SLEEP_MONITOR_THING_TYPE)) {
            return new SleepMonitorThingHandler(thing);
        } else if (thingTypeUID.equals(PERSON_THING_TYPE)) {
            return new PersonThingHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof WithingsBridgeHandler) {
            ThingUID thingUID = thingHandler.getThing().getUID();
            unregisterDeviceDiscoveryService(thingUID);
        }
    }

    private synchronized void registerDeviceDiscoveryService(WithingsBridgeHandler bridgeHandler) {
        if (bundleContext != null) {
            WithingsDiscoveryService discoveryService = new WithingsDiscoveryService(bridgeHandler, accessTokenService,
                    httpClient, localeProvider, translationProvider);
            Map<String, Object> configProperties = new HashMap<>();
            configProperties.put(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false);
            discoveryService.activate(configProperties);
            discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                    .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
        }
    }

    private synchronized void unregisterDeviceDiscoveryService(ThingUID thingUID) {
        ServiceRegistration<?> serviceReg = discoveryServiceRegs.remove(thingUID);
        if (serviceReg != null) {
            WithingsDiscoveryService service = (WithingsDiscoveryService) bundleContext
                    .getService(serviceReg.getReference());
            serviceReg.unregister();
            if (service != null) {
                service.deactivate();
            }
        }
    }
}

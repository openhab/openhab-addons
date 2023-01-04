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
package org.openhab.binding.hdpowerview.internal;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hdpowerview.internal.discovery.HDPowerViewDeviceDiscoveryService;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewHubHandler;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewRepeaterHandler;
import org.openhab.binding.hdpowerview.internal.handler.HDPowerViewShadeHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HDPowerViewHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hdpowerview")
public class HDPowerViewHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClient httpClient;
    private final HDPowerViewTranslationProvider translationProvider;

    @Activate
    public HDPowerViewHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            final @Reference TranslationProvider i18nProvider, final @Reference LocaleProvider localeProvider,
            ComponentContext componentContext) {
        super.activate(componentContext);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.translationProvider = new HDPowerViewTranslationProvider(getBundleContext().getBundle(), i18nProvider,
                localeProvider);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HDPowerViewBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (HDPowerViewBindingConstants.THING_TYPE_HUB.equals(thingTypeUID)) {
            HDPowerViewHubHandler handler = new HDPowerViewHubHandler((Bridge) thing, httpClient, translationProvider);
            registerService(new HDPowerViewDeviceDiscoveryService(handler));
            return handler;
        } else if (HDPowerViewBindingConstants.THING_TYPE_SHADE.equals(thingTypeUID)) {
            return new HDPowerViewShadeHandler(thing);
        } else if (HDPowerViewBindingConstants.THING_TYPE_REPEATER.equals(thingTypeUID)) {
            return new HDPowerViewRepeaterHandler(thing);
        }

        return null;
    }

    private void registerService(DiscoveryService discoveryService) {
        bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
    }
}

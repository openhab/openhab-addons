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
package org.openhab.binding.shelly.internal;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.smarthome.core.i18n.LocaleProvider;
import org.eclipse.smarthome.core.i18n.TranslationProvider;
import org.eclipse.smarthome.core.net.HttpServiceUtil;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.binding.shelly.internal.handler.ShellyLightHandler;
import org.openhab.binding.shelly.internal.handler.ShellyProtectedHandler;
import org.openhab.binding.shelly.internal.handler.ShellyRelayHandler;
import org.openhab.binding.shelly.internal.util.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyUtils;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.annotations.NonNull;

/**
 * The {@link ShellyHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = { ThingHandlerFactory.class, ShellyHandlerFactory.class }, configurationPid = "binding.shelly")
public class ShellyHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(ShellyHandlerFactory.class);
    private final HttpClient httpClient;
    private final ShellyTranslationProvider messages;
    private final ShellyCoapServer coapServer;
    private final Set<ShellyBaseHandler> deviceListeners = new ConcurrentHashSet<>();
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ShellyBindingConstants.SUPPORTED_THING_TYPES_UIDS;
    private ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    private String localIP = "";
    private int httpPort = -1;

    /**
     * Activate the bundle: save properties
     *
     * @param componentContext
     * @param configProperties set of properties from cfg (use same names as in
     *            thing config)
     */
    @Activate
    public ShellyHandlerFactory(@Reference NetworkAddressService networkAddressService,
            @Reference LocaleProvider localeProvider, @Reference TranslationProvider i18nProvider,
            @Reference HttpClientFactory httpClientFactory, ComponentContext componentContext,
            Map<String, Object> configProperties) {
        logger.debug("Activate Shelly HandlerFactory");
        super.activate(componentContext);

        messages = new ShellyTranslationProvider(bundleContext.getBundle(), i18nProvider, localeProvider);
        localIP = ShellyUtils.getString(networkAddressService.getPrimaryIpv4HostAddress().toString());

        this.httpClient = httpClientFactory.getCommonHttpClient();
        httpPort = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
        if (httpPort == -1) {
            httpPort = 8080;
        }
        logger.debug("Using OH HTTP port {}", httpPort);

        this.coapServer = new ShellyCoapServer();

        // Save bindingConfig & pass it to all registered listeners
        bindingConfig.updateFromProperties(configProperties);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        String thingType = thingTypeUID.getId();
        ShellyBaseHandler handler = null;

        if (thingType.equals(THING_TYPE_SHELLYPROTECTED_STR)) {
            logger.debug("{}: Create new thing of type {} using ShellyProtectedHandler", thing.getLabel(),
                    thingTypeUID.toString());
            handler = new ShellyProtectedHandler(thing, messages, bindingConfig, coapServer, localIP, httpPort,
                    httpClient);
        } else if (thingType.equals(THING_TYPE_SHELLYBULB.getId()) || thingType.equals(THING_TYPE_SHELLYDUO.getId())
                || thingType.equals(THING_TYPE_SHELLYRGBW2_COLOR.getId())
                || thingType.equals(THING_TYPE_SHELLYRGBW2_WHITE.getId())) {
            logger.debug("{}: Create new thing of type {} using ShellyLightHandler", thing.getLabel(),
                    thingTypeUID.toString());
            handler = new ShellyLightHandler(thing, messages, bindingConfig, coapServer, localIP, httpPort, httpClient);
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("{}: Create new thing of type {} using ShellyRelayHandler", thing.getLabel(),
                    thingTypeUID.toString());
            handler = new ShellyRelayHandler(thing, messages, bindingConfig, coapServer, localIP, httpPort, httpClient);
        }

        if (handler != null) {
            deviceListeners.add(handler);
            return handler;
        }

        logger.debug("Unable to create Thing Handler instance!");
        return null;
    }

    /**
     * Remove handler of things.
     */
    @Override
    protected synchronized void removeHandler(@NonNull ThingHandler thingHandler) {
        if (thingHandler instanceof ShellyBaseHandler) {
            deviceListeners.remove(thingHandler);
        }
    }

    /**
     * Dispatch event to registered devices.
     *
     * @param deviceName
     * @param componentIndex Index of component, e.g. 2 for relay2
     * @param eventType Type of event, e.g. light
     * @param parameters Input parameters from URL, e.g. on sensor reports
     */
    public void onEvent(String ipAddress, String deviceName, String componentIndex, String eventType,
            Map<String, String> parameters) {
        logger.trace("{}: Dispatch event to thing handler", deviceName);
        for (ShellyBaseHandler listener : deviceListeners) {
            if (listener.onEvent(ipAddress, deviceName, componentIndex, eventType, parameters)) {
                // event processed
                return;
            }
        }
    }

    public ShellyBindingConfiguration getBindingConfig() {
        return bindingConfig;
    }
}

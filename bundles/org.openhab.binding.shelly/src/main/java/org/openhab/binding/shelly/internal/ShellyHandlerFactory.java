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
package org.openhab.binding.shelly.internal;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.coap.ShellyCoapServer;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBaseHandler;
import org.openhab.binding.shelly.internal.handler.ShellyLightHandler;
import org.openhab.binding.shelly.internal.handler.ShellyManagerInterface;
import org.openhab.binding.shelly.internal.handler.ShellyProtectedHandler;
import org.openhab.binding.shelly.internal.handler.ShellyRelayHandler;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyUtils;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.HttpServiceUtil;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ShellyBindingConstants.SUPPORTED_THING_TYPES_UIDS;

    private final Logger logger = LoggerFactory.getLogger(ShellyHandlerFactory.class);
    private final HttpClient httpClient;
    private final ShellyTranslationProvider messages;
    private final ShellyCoapServer coapServer;

    private final Map<String, ShellyBaseHandler> deviceListeners = new ConcurrentHashMap<>();
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
            @Reference ShellyTranslationProvider translationProvider, @Reference HttpClientFactory httpClientFactory,
            ComponentContext componentContext, Map<String, Object> configProperties) {
        logger.debug("Activate Shelly HandlerFactory");
        super.activate(componentContext);
        messages = translationProvider;
        // Save bindingConfig & pass it to all registered listeners
        bindingConfig.updateFromProperties(configProperties);

        localIP = bindingConfig.localIP;
        if (localIP.isEmpty()) {
            localIP = ShellyUtils.getString(networkAddressService.getPrimaryIpv4HostAddress());
        }
        if (localIP.isEmpty()) {
            logger.warn("{}", messages.get("message.init.noipaddress"));
        }

        this.httpClient = httpClientFactory.getCommonHttpClient();
        httpPort = HttpServiceUtil.getHttpServicePort(componentContext.getBundleContext());
        if (httpPort == -1) {
            httpPort = 8080;
        }
        logger.debug("Using OH HTTP port {}", httpPort);

        this.coapServer = new ShellyCoapServer();
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
        } else if (thingType.equals(THING_TYPE_SHELLYBULB_STR) || thingType.equals(THING_TYPE_SHELLYDUO_STR)
                || thingType.equals(THING_TYPE_SHELLYRGBW2_COLOR_STR)
                || thingType.equals(THING_TYPE_SHELLYRGBW2_WHITE_STR)
                || thingType.equals(THING_TYPE_SHELLYDUORGBW_STR)) {
            logger.debug("{}: Create new thing of type {} using ShellyLightHandler", thing.getLabel(),
                    thingTypeUID.toString());
            handler = new ShellyLightHandler(thing, messages, bindingConfig, coapServer, localIP, httpPort, httpClient);
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            logger.debug("{}: Create new thing of type {} using ShellyRelayHandler", thing.getLabel(),
                    thingTypeUID.toString());
            handler = new ShellyRelayHandler(thing, messages, bindingConfig, coapServer, localIP, httpPort, httpClient);
        }

        if (handler != null) {
            String uid = thing.getUID().getAsString();
            deviceListeners.put(uid, handler);
            logger.debug("Thing handler for uid {} added, total things = {}", uid, deviceListeners.size());
            return handler;
        }

        logger.debug("Unable to create Thing Handler instance!");
        return null;
    }

    public Map<String, ShellyManagerInterface> getThingHandlers() {
        return new HashMap<>(deviceListeners);
    }

    /**
     * Remove handler of things.
     */
    @Override
    protected synchronized void removeHandler(@NonNull ThingHandler thingHandler) {
        if (thingHandler instanceof ShellyBaseHandler) {
            String uid = thingHandler.getThing().getUID().getAsString();
            deviceListeners.remove(uid);
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
        for (Map.Entry<String, ShellyBaseHandler> listener : deviceListeners.entrySet()) {
            ShellyBaseHandler thingHandler = listener.getValue();
            if (thingHandler.onEvent(ipAddress, deviceName, componentIndex, eventType, parameters)) {
                // event processed
                return;
            }
        }
    }

    public ShellyBindingConfiguration getBindingConfig() {
        return bindingConfig;
    }
}

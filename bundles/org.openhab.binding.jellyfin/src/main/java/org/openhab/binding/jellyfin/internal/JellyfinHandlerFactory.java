/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal;

import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.BINDING_ID;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.SUPPORTED_THING_TYPES;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.THING_TYPE_CLIENT;
import static org.openhab.binding.jellyfin.internal.JellyfinBindingConstants.THING_TYPE_SERVER;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jellyfin.internal.handler.JellyfinClientHandler;
import org.openhab.binding.jellyfin.internal.handler.JellyfinServerHandler;
import org.openhab.binding.jellyfin.internal.servlet.JellyfinBridgeServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link JellyfinHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.jellyfin", service = ThingHandlerFactory.class)
public class JellyfinHandlerFactory extends BaseThingHandlerFactory {
    private final HttpService httpService;
    private final Logger logger = LoggerFactory.getLogger(JellyfinHandlerFactory.class);
    private final Map<ThingUID, JellyfinBridgeServlet> servletRegistrations = new HashMap<>();

    @Activate
    public JellyfinHandlerFactory(@Reference HttpService httpService) {
        this.httpService = httpService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_SERVER.equals(thingTypeUID)) {
            var serverHandler = new JellyfinServerHandler((Bridge) thing);
            registerAuthenticationServlet(serverHandler);
            return serverHandler;
        }
        if (THING_TYPE_CLIENT.equals(thingTypeUID)) {
            return new JellyfinClientHandler(thing);
        }
        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof JellyfinServerHandler serverHandler) {
            unregisterAuthenticationServlet(serverHandler);
        }
        super.removeHandler(thingHandler);
    }

    private synchronized void registerAuthenticationServlet(JellyfinServerHandler bridgeHandler) {
        var auth = new JellyfinBridgeServlet(bridgeHandler);
        try {
            httpService.registerServlet(getAuthenticationServletPath(bridgeHandler), auth, null,
                    httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Register servlet fails", e);
        }
        servletRegistrations.put(bridgeHandler.getThing().getUID(), auth);
    }

    private synchronized void unregisterAuthenticationServlet(JellyfinServerHandler bridgeHandler) {
        var loginServlet = servletRegistrations.get(bridgeHandler.getThing().getUID());
        if (loginServlet != null) {
            httpService.unregister(getAuthenticationServletPath(bridgeHandler));
        }
    }

    private String getAuthenticationServletPath(JellyfinServerHandler bridgeHandler) {
        return new StringBuilder().append("/").append(BINDING_ID).append("/")
                .append(bridgeHandler.getThing().getUID().getId()).toString();
    }
}

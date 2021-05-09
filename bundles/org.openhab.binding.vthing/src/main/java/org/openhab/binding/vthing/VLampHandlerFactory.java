/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.vthing;

import static org.openhab.binding.vthing.VLampBindingConstants.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
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
 * The {@link VLampHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Juergen Weber - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.vthing", service = ThingHandlerFactory.class)
public class VLampHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_VLAMP);

    private final Logger logger = LoggerFactory.getLogger(VLampHandlerFactory.class);

    private final HttpService httpService;

    private WebsocketServlet websocketServlet;

    static Map<Session, ThingUID> thingsForSessions = new ConcurrentHashMap<>();

    static Map<ThingUID, VLampHandler> handlersForThings = new ConcurrentHashMap<>();

    private static @Nullable VLampHandlerFactory instance = null;

    private static final String NO_THING = VLampBindingConstants.BINDING_ID + ":" + VLampBindingConstants.THING_ID
            + ":_NULL_";

    static void onUpdate(ThingUID uid, String message) {
        instance.logger.debug("onUpdate: {}", message);

        for (Map.Entry<Session, ThingUID> entry : thingsForSessions.entrySet()) {
            if (entry.getValue().equals(uid)) {
                Session session = entry.getKey();
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    instance.logger.error(uid.toString(), e);
                }
            }
        }
    }

    static void onOpenWebsocket(Session session) {

        ThingUID thingUID = new ThingUID(NO_THING);

        thingsForSessions.put(session, thingUID);
    }

    static void onCloseWebsocket(Session session) {
        thingsForSessions.remove(session);
    }

    static void onThingNamed(Session session, String thingID) {

        ThingUID thingUID = new ThingUID(thingID);

        thingsForSessions.put(session, thingUID);

        VLampHandler handler = handlersForThings.get(thingUID);

        handler.triggerUpdate(thingUID);
    }

    @Activate
    public VLampHandlerFactory(@Reference HttpService httpService) {
        this.httpService = httpService;
        logger.debug("HttpService: {}", httpService);

        instance = this;

        websocketServlet = new WebsocketServlet();

        try {
            httpService.registerServlet("/vlampws", websocketServlet, null, null);

            httpService.registerResources("/vlamp", "/webcontent", new VLampHttpContext());

        } catch (ServletException | NamespaceException e) {
            logger.error("Servlet", e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_VLAMP.equals(thingTypeUID)) {
            VLampHandler handler = new VLampHandler(thing);
            handlersForThings.put(thing.getUID(), handler);
            return handler;
        }

        return null;
    }
}

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
package org.openhab.binding.valloxmv.internal;

import static org.openhab.binding.valloxmv.internal.ValloxMVBindingConstants.THING_TYPE_VALLOXMV;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ValloxMVHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Björn Brings - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.valloxmv")
@NonNullByDefault()
public class ValloxMVHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_VALLOXMV);

    private final WebSocketClient webSocketClient;

    @Activate
    public ValloxMVHandlerFactory(@Reference final WebSocketFactory webSocketFactory) {
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_VALLOXMV.equals(thingTypeUID)) {
            return new ValloxMVHandler(thing, webSocketClient);
        }

        return null;
    }
}

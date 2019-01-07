/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.valloxmv.internal;

import static org.openhab.binding.valloxmv.internal.ValloxMVBindingConstants.THING_TYPE_VALLOXMV;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.WebSocketFactory;
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
    private @Nullable WebSocketClient webSocketClient;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_VALLOXMV);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_VALLOXMV.equals(thingTypeUID)) {
            if (webSocketClient != null) {
                return new ValloxMVHandler(thing, webSocketClient);
            } else {
                return null;
            }
        }

        return null;
    }

    @Reference
    protected void setHttpClientFactory(WebSocketFactory webSocketFactory) {
        this.webSocketClient = webSocketFactory.getCommonWebSocketClient();
    }

    protected void unsetHttpClientFactory(WebSocketFactory webSocketFactory) {
        this.webSocketClient = null;
    }
}

/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.zwavejs.internal.handler;

import static org.openhab.binding.zwavejs.internal.BindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.type.ZwaveJSTypeGenerator;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ZwaveJSHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.zwavejs", service = ThingHandlerFactory.class)
public class ZwaveJSHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_GATEWAY, THING_TYPE_NODE);

    private WebSocketFactory webSocketFactory;
    private ZwaveJSTypeGenerator typeGenerator;

    @Activate
    public ZwaveJSHandlerFactory(final @Reference WebSocketFactory webSocketFactory,
            final @Reference ZwaveJSTypeGenerator typeGenerator) {
        this.webSocketFactory = webSocketFactory;
        this.typeGenerator = typeGenerator;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            return new ZwaveJSBridgeHandler((Bridge) thing, webSocketFactory);
        } else if (THING_TYPE_NODE.equals(thingTypeUID)) {
            return new ZwaveJSNodeHandler(thing, typeGenerator);
        }

        return null;
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal;

import static org.openhab.binding.hasslink.internal.HassLinkBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.handler.HassLinkBridgeHandler;
import org.openhab.binding.hasslink.internal.handler.HassLinkDeviceHandler;
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
 * The {@link HassLinkHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.hasslink", service = ThingHandlerFactory.class)
public class HassLinkHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_BRIDGE, THING_TYPE_DEVICE);

    private final WebSocketFactory webSocketFactory;
    private final HassLinkDynamicStateDescriptionProvider stateDescriptionProvider;
    private final HassLinkDynamicCommandDescriptionProvider commandDescriptionProvider;

    @Activate
    public HassLinkHandlerFactory(@Reference WebSocketFactory webSocketFactory,
            @Reference HassLinkDynamicStateDescriptionProvider stateDescriptionProvider,
            @Reference HassLinkDynamicCommandDescriptionProvider commandDescriptionProvider) {
        this.webSocketFactory = webSocketFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_BRIDGE.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new HassLinkBridgeHandler(bridge, webSocketFactory);
        }
        if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new HassLinkDeviceHandler(thing, stateDescriptionProvider, commandDescriptionProvider);
        }

        return null;
    }
}

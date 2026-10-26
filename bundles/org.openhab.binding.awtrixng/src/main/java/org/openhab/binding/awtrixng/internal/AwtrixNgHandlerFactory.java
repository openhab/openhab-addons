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
package org.openhab.binding.awtrixng.internal;

import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgAppHandler;
import org.openhab.binding.awtrixng.internal.handler.AwtrixNgBridgeHandler;
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
 * The {@link AwtrixNgHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Keith T. Garner - Initial contribution
 */
@Component(service = ThingHandlerFactory.class)
@NonNullByDefault
public class AwtrixNgHandlerFactory extends BaseThingHandlerFactory {

    private final AwtrixNgStateDescriptionProvider stateDescriptionProvider;

    @Activate
    public AwtrixNgHandlerFactory(final @Reference AwtrixNgStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_CLOCK.equals(thing.getThingTypeUID())) {
            return new AwtrixNgBridgeHandler((Bridge) thing, stateDescriptionProvider);
        } else if (THING_TYPE_APP.equals(thing.getThingTypeUID())) {
            return new AwtrixNgAppHandler(thing);
        }
        return null;
    }
}

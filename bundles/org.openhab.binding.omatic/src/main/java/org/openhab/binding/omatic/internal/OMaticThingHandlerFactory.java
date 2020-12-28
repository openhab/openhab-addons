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
package org.openhab.binding.omatic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omatic.internal.event.OMaticEventSubscriber;
import org.openhab.binding.omatic.internal.handler.OMaticMachineThingHandler;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link OMaticThingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@Component(service = { ThingHandlerFactory.class }, configurationPid = "binding.omatic")
@NonNullByDefault
public class OMaticThingHandlerFactory extends BaseThingHandlerFactory {

    private final OMaticEventSubscriber eventSubscriber;
    private final ItemRegistry itemRegistry;

    @Activate
    public OMaticThingHandlerFactory(final @Reference OMaticEventSubscriber eventSubscriber,
            final @Reference ItemRegistry itemRegistry) {
        this.eventSubscriber = eventSubscriber;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return OMaticMachineThingHandler.supportsThingType(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (OMaticMachineThingHandler.supportsThingType(thingTypeUID)) {
            return new OMaticMachineThingHandler(thing, eventSubscriber, itemRegistry);
        }
        return null;
    }
}

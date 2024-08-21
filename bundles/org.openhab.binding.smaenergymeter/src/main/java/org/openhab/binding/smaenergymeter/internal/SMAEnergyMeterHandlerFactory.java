/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.smaenergymeter.internal;

import static org.openhab.binding.smaenergymeter.internal.SMAEnergyMeterBindingConstants.*;

import org.openhab.binding.smaenergymeter.internal.handler.SMAEnergyMeterHandler;
import org.openhab.binding.smaenergymeter.internal.packet.PacketListenerRegistry;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link SMAEnergyMeterHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Osman Basha - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.smaenergymeter")
public class SMAEnergyMeterHandlerFactory extends BaseThingHandlerFactory {

    private final PacketListenerRegistry packetListenerRegistry;

    @Activate
    public SMAEnergyMeterHandlerFactory(@Reference PacketListenerRegistry packetListenerRegistry) {
        this.packetListenerRegistry = packetListenerRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ENERGY_METER)) {
            return new SMAEnergyMeterHandler(thing, packetListenerRegistry);
        }

        return null;
    }
}

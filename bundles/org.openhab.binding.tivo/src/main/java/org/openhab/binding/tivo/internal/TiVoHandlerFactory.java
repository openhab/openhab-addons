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
package org.openhab.binding.tivo.internal;

import static org.openhab.binding.tivo.internal.TiVoBindingConstants.THING_TYPE_TIVO;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tivo.internal.handler.TiVoHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link TiVoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - minor updates, removal of unused DiscoveryService functionality.
 * @author Michael Lobstein - Updated for OH3
 */

@NonNullByDefault
@Component(configurationPid = "binding.tivo", service = ThingHandlerFactory.class)
public class TiVoHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_TIVO);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_TIVO)) {
            return new TiVoHandler(thing);
        }
        return null;
    }
}

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
package org.openhab.binding.pentair.internal.factory;

import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pentair.internal.handler.PentairControllerHandler;
import org.openhab.binding.pentair.internal.handler.PentairIPBridgeHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliChemHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliChlorHandler;
import org.openhab.binding.pentair.internal.handler.PentairIntelliFloHandler;
import org.openhab.binding.pentair.internal.handler.PentairSerialBridgeHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
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
 * The {@link PentairHandlerFactory} is responsible for creating thing
 * handlers.
 *
 * @author Jeff James - Initial contribution
 */

@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pentair")
public class PentairHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(IP_BRIDGE_THING_TYPE,
            SERIAL_BRIDGE_THING_TYPE, CONTROLLER_THING_TYPE, INTELLIFLO_THING_TYPE, INTELLICHLOR_THING_TYPE,
            INTELLICHEM_THING_TYPE);

    private final SerialPortManager serialPortManager;

    @Activate
    public PentairHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        // Obtain the serial port manager service using an OSGi reference
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(IP_BRIDGE_THING_TYPE)) {
            return new PentairIPBridgeHandler((Bridge) thing);
        } else if (thingTypeUID.equals(SERIAL_BRIDGE_THING_TYPE)) {
            return new PentairSerialBridgeHandler((Bridge) thing, serialPortManager);
        } else if (thingTypeUID.equals(CONTROLLER_THING_TYPE)) {
            return new PentairControllerHandler(thing);
        } else if (thingTypeUID.equals(INTELLIFLO_THING_TYPE)) {
            return new PentairIntelliFloHandler(thing);
        } else if (thingTypeUID.equals(INTELLICHLOR_THING_TYPE)) {
            return new PentairIntelliChlorHandler(thing);
        } else if (thingTypeUID.equals(INTELLICHEM_THING_TYPE)) {
            return new PentairIntelliChemHandler(thing);
        }

        return null;
    }
}

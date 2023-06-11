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
package org.openhab.binding.regoheatpump.internal;

import static org.openhab.binding.regoheatpump.internal.RegoHeatPumpBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.regoheatpump.internal.handler.IpHusdataHandler;
import org.openhab.binding.regoheatpump.internal.handler.IpRego6xxHeatPumpHandler;
import org.openhab.binding.regoheatpump.internal.handler.SerialHusdataHandler;
import org.openhab.binding.regoheatpump.internal.handler.SerialRego6xxHeatPumpHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link RegoHeatPumpHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.regoheatpump")
public class RegoHeatPumpHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(THING_TYPE_IP_REGO6XX, THING_TYPE_SERIAL_REGO6XX, THING_TYPE_IP_HUSDATA, THING_TYPE_SERIAL_HUSDATA)
            .collect(Collectors.toSet()));

    private final SerialPortManager serialPortManager;

    @Activate
    public RegoHeatPumpHandlerFactory(final @Reference SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_IP_REGO6XX)) {
            return new IpRego6xxHeatPumpHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_SERIAL_REGO6XX)) {
            return new SerialRego6xxHeatPumpHandler(thing, serialPortManager);
        }

        if (thingTypeUID.equals(THING_TYPE_IP_HUSDATA)) {
            return new IpHusdataHandler(thing);
        }

        if (thingTypeUID.equals(THING_TYPE_SERIAL_HUSDATA)) {
            return new SerialHusdataHandler(thing, serialPortManager);
        }

        return null;
    }
}

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
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pioneeravr.internal.PioneerAvrBindingConstants;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link AvrHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Antoine Besnard - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pioneeravr")
public class AvrHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(PioneerAvrBindingConstants.IP_AVR_THING_TYPE, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2014,
                    PioneerAvrBindingConstants.IP_AVR_THING_TYPE2015, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2016,
                    PioneerAvrBindingConstants.IP_AVR_THING_TYPE2017, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2018,
                    PioneerAvrBindingConstants.IP_AVR_THING_TYPE2019, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2020,
                    PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE,
                    PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE).collect(Collectors.toSet()));

    private SerialPortManager serialPortManager;

    @Activate
    public AvrHandlerFactory(ComponentContext componentContext, final @Reference SerialPortManager serialPortManager) {
        super.activate(componentContext);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2014)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2015)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2016)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2017)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2018)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2019)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2020)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE)) {
            return new IpAvrHandler(thing);
        } else if (thingTypeUID.equals(PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE)) {
            return new SerialAvrHandler(thing, serialPortManager);
        }

        return null;
    }
}

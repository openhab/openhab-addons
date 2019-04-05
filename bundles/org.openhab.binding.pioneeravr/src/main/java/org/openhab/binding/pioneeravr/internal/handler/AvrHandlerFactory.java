/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.pioneeravr.internal.PioneerAvrBindingConstants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AvrHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Antoine Besnard - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.pioneeravr")
public class AvrHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(PioneerAvrBindingConstants.IP_AVR_THING_TYPE, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2014,
                    PioneerAvrBindingConstants.IP_AVR_THING_TYPE2015, PioneerAvrBindingConstants.IP_AVR_THING_TYPE2016,
                    PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE,
                    PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE).collect(Collectors.toSet()));

    protected void activate(ComponentContext componentContext, Map<String, Object> configProps) {
        super.activate(componentContext);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2014)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2015)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_THING_TYPE2016)
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE)) {
            return new IpAvrHandler(thing);
        } else if (thingTypeUID.equals(PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE)) {
            return new SerialAvrHandler(thing);
        }

        return null;
    }
}

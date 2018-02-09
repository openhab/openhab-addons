/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pioneeravr.internal.handler;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.pioneeravr.PioneerAvrBindingConstants;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Sets;

/**
 * The {@link AvrHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Antoine Besnard - Initial contribution
 */
public class AvrHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(
            PioneerAvrBindingConstants.IP_AVR_THING_TYPE, PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE,
            PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE);

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
                || thingTypeUID.equals(PioneerAvrBindingConstants.IP_AVR_UNSUPPORTED_THING_TYPE)) {
            return new IpAvrHandler(thing);
        } else if (thingTypeUID.equals(PioneerAvrBindingConstants.SERIAL_AVR_THING_TYPE)) {
            return new SerialAvrHandler(thing);
        }

        return null;
    }
}

/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.internal;

import static org.openhab.binding.mox.MoxBindingConstants.THING_TYPE_GATEWAY;
import static org.openhab.binding.mox.MoxBindingConstants.THING_TYPE_1G_ONOFF;
import static org.openhab.binding.mox.MoxBindingConstants.THING_TYPE_1G_DIMMER;
import static org.openhab.binding.mox.MoxBindingConstants.THING_TYPE_1G_FAN;
import static org.openhab.binding.mox.MoxBindingConstants.THING_TYPE_1G_CURTAIN;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mox.handler.MoxGatewayHandler;
import org.openhab.binding.mox.handler.MoxModuleHandler;

import com.google.common.collect.Sets;

/**
 * The {@link MoxHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public class MoxHandlerFactory extends BaseThingHandlerFactory {
    
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_GATEWAY, THING_TYPE_1G_ONOFF, THING_TYPE_1G_DIMMER, THING_TYPE_1G_FAN, THING_TYPE_1G_CURTAIN);
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
    	ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new MoxGatewayHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_1G_ONOFF)) {
            return new MoxModuleHandler(thing);
	    } else if (thingTypeUID.equals(THING_TYPE_1G_DIMMER)) {
	        return new MoxModuleHandler(thing);
	    } else if (thingTypeUID.equals(THING_TYPE_1G_FAN)) {
	        return new MoxModuleHandler(thing);
	    } else if (thingTypeUID.equals(THING_TYPE_1G_CURTAIN)) {
	        return new MoxModuleHandler(thing);
	    }

        return null;
    }
    
}

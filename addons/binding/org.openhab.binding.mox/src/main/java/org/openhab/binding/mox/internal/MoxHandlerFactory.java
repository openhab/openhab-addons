/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.internal;

import com.google.common.collect.Sets;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mox.handler.MoxGatewayHandler;
import org.openhab.binding.mox.handler.MoxModuleHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.openhab.binding.mox.MoxBindingConstants.*;

/**
 * The {@link MoxHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public class MoxHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(MoxHandlerFactory.class);

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
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new MoxModuleHandler(thing);
	    } 
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID,
                             Configuration configuration,
                             ThingUID thingUID,
                             ThingUID bridgeUID) {
        if (THING_TYPE_GATEWAY.equals(thingTypeUID)) {
            logger.debug("Create Bridge {}:{}", thingTypeUID, thingUID);
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            if (bridgeUID==null) {
                bridgeUID = new ThingUID(THING_TYPE_GATEWAY, "221");
            }
            logger.debug("Create thing with {} with bridge {}", thingTypeUID, bridgeUID);
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        return null;
    }


}

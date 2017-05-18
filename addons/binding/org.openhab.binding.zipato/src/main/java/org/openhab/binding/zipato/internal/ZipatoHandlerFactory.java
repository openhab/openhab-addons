/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zipato.internal;

import static org.openhab.binding.zipato.ZipatoBindingConstants.*;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.zipato.ZipatoBindingConstants;
import org.openhab.binding.zipato.handler.ZipatoControllerHandler;
import org.openhab.binding.zipato.handler.ZipatoThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZipatoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Hartwig - Initial contribution
 */
public class ZipatoHandlerFactory extends BaseThingHandlerFactory {
    private static final Logger logger = LoggerFactory.getLogger(ZipatoHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        if (thingTypeUID.equals(ZIPATO_THING_SENSOR_UID) || thingTypeUID.equals(ZIPATO_THING_SWITCH_UID)) {
            return true;
        }
        return ZipatoBindingConstants.BINDING_ID.equals(thingTypeUID.getBindingId());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (logger.isTraceEnabled()) {
            logger.trace("handler call: " + thing);
        }
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(ZIPATO_CONTROLLER_UID)) {
            return new ZipatoControllerHandler((Bridge) thing);
        }
        return new ZipatoThingHandler(thing);
    }
}

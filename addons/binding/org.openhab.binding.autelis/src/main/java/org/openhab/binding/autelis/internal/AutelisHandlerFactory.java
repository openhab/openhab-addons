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
package org.openhab.binding.autelis.internal;

import static org.openhab.binding.autelis.AutelisBindingConstants.SUPPORTED_THING_TYPES_UIDS;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.autelis.handler.AutelisHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AutelisHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Dan Cunningham - Initial contribution
 */
public class AutelisHandlerFactory extends BaseThingHandlerFactory {
    private Logger logger = LoggerFactory.getLogger(AutelisHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new AutelisHandler(thing);
        }
        return null;
    }
}

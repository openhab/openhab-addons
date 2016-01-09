/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.passthru.internal;

import static org.openhab.binding.passthru.PassthruBindingConstants.PASSTHRU_THING_TYPE_UID;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.passthru.handler.PassthruHandler;

/**
 * The {@link PassthruHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author J. Geyer - Initial contribution
 */
public class PassthruHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(PASSTHRU_THING_TYPE_UID);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(PASSTHRU_THING_TYPE_UID)) {
            return new PassthruHandler(thing);
        }

        return null;
    }
}

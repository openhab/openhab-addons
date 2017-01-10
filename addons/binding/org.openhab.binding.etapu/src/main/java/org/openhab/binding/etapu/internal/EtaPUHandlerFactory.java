/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.etapu.internal;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.etapu.handler.EtaPUHandler;

/**
 * The {@link EtaPUHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Huber - Initial contribution
 */
public class EtaPUHandlerFactory extends BaseThingHandlerFactory {

    // List of all Thing Type UIDs
    public final static ThingTypeUID ETA_THING_TYPE = new ThingTypeUID("etapu", "etapu");

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ETA_THING_TYPE.equals(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        if (supportsThingType(thing.getThingTypeUID())) {
            return new EtaPUHandler(thing);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    }

}

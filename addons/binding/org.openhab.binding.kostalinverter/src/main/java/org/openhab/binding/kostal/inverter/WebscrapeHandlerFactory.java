/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.kostal.inverter;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

/**
 * @author Christian Schneider
 */
public class WebscrapeHandlerFactory extends BaseThingHandlerFactory {
    public static final ThingTypeUID KOSTAL_INVERTER = new ThingTypeUID("kostalinverter", "kostalinverter");

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(KOSTAL_INVERTER);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new WebscrapeHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    }

}

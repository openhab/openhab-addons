/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton.internal;

import static org.openhab.binding.amazondashbutton.AmazonDashButtonBindingConstants.DASH_BUTTON_THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.amazondashbutton.handler.AmazonDashButtonHandler;

/**
 * The {@link AmazonDashButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Libutzki - Initial contribution
 */
public class AmazonDashButtonHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(DASH_BUTTON_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DASH_BUTTON_THING_TYPE)) {
            return new AmazonDashButtonHandler(thing);
        }

        return null;
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vallox.internal;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.vallox.ValloxBindingConstants;
import org.openhab.binding.vallox.handler.ValloxSerialHandler;

import com.google.common.collect.Lists;

/**
 * The {@link ValloxHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Hauke Fuhrmann - Initial contribution
 */
public class ValloxThingHandlerFactory extends BaseThingHandlerFactory {

    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(ValloxBindingConstants.THING_TYPE_VALLOX_SERIAL);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(ValloxBindingConstants.THING_TYPE_VALLOX_SERIAL)) {
            return new ValloxSerialHandler(thing);
        }

        return null;
    }

}

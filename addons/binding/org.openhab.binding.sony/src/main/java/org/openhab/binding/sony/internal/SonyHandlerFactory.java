/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.sony.internal.dial.DialConstants;
import org.openhab.binding.sony.internal.dial.DialHandler;
import org.openhab.binding.sony.internal.ircc.IrccConstants;
import org.openhab.binding.sony.internal.ircc.IrccHandler;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebConstants;
import org.openhab.binding.sony.internal.scalarweb.ScalarWebHandler;
import org.openhab.binding.sony.internal.simpleip.SimpleIpConstants;
import org.openhab.binding.sony.internal.simpleip.SimpleIpHandler;

import com.google.common.collect.ImmutableSet;

// TODO: Auto-generated Javadoc
/**
 * The {@link SonyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Tim Roberts - Initial contribution
 */
public class SonyHandlerFactory extends BaseThingHandlerFactory {

    /** The Constant SUPPORTED_THING_TYPES_UIDS. */
    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
            SimpleIpConstants.THING_TYPE_SIMPLEIP, IrccConstants.THING_TYPE_IRCC, DialConstants.THING_TYPE_DIAL,
            ScalarWebConstants.THING_TYPE_SCALAR);

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory#supportsThingType(org.eclipse.smarthome.core.thing.ThingTypeUID)
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /* (non-Javadoc)
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory#createHandler(org.eclipse.smarthome.core.thing.Thing)
     */
    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(SimpleIpConstants.THING_TYPE_SIMPLEIP)) {
            return new SimpleIpHandler(thing);
        } else if (thingTypeUID.equals(IrccConstants.THING_TYPE_IRCC)) {
            return new IrccHandler(thing);
        } else if (thingTypeUID.equals(DialConstants.THING_TYPE_DIAL)) {
            return new DialHandler(thing);
        } else if (thingTypeUID.equals(ScalarWebConstants.THING_TYPE_SCALAR)) {
            return new ScalarWebHandler(thing);
        }

        return null;
    }
}

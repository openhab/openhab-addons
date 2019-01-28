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
package org.openhab.binding.lgtvserial.internal;

import static org.openhab.binding.lgtvserial.internal.LgTvSerialBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.lgtvserial.internal.handler.LgTvSerialHandler;
import org.openhab.binding.lgtvserial.internal.protocol.serial.SerialCommunicatorFactory;

/**
 * The {@link LgTvSerialHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Added communicator to support daisy chained TV
 */
public class LgTvSerialHandlerFactory extends BaseThingHandlerFactory {

    private static final SerialCommunicatorFactory FACTORY = new SerialCommunicatorFactory();

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_GENERIC);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_LV_SERIES);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_LVx55_SERIES);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_LK_SERIES);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_M6503C);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_LGTV_PW_SERIES);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (this.supportsThingType(thingTypeUID)) {
            return new LgTvSerialHandler(thing, FACTORY);
        }

        return null;
    }
}

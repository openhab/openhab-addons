/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal;

import static org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.stiebeleltron.internal.handler.StiebelEltronHandler;
import org.openhab.binding.modbus.stiebeleltron.internal.handler.StiebelEltronHandlerIsgSgReadyEm;
import org.openhab.binding.modbus.stiebeleltron.internal.handler.StiebelEltronHandlerWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.handler.StiebelEltronHandlerWpm3;
import org.openhab.binding.modbus.stiebeleltron.internal.handler.StiebelEltronHandlerWpm3i;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link StiebelEltronHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Added new things for WPM3 and WPM3i compatible heat pumps according to
 *         Stiebel-Eltron Software Documentation/ Software extension for Internet Service Gateway (ISG) / Modbus TCP/IP
 *         Version 9535
 *
 */
@NonNullByDefault
@Component(configurationPid = "binding.stiebeleltron", service = ThingHandlerFactory.class)
public class StiebelEltronHandlerFactory extends BaseThingHandlerFactory {

    @SuppressWarnings("serial")
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>() {
        {
            add(THING_TYPE_HEATPUMP);
            add(THING_TYPE_STIEBELELTRON_HEATPUMP_WPMSYSTEM);
            add(THING_TYPE_STIEBELELTRON_HEATPUMP_WPM3);
            add(THING_TYPE_STIEBELELTRON_HEATPUMP_WPM3I);
            add(THING_TYPE_STIEBELELTRON_ISG_SG_READY_EM);
        }
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_HEATPUMP.equals(thingTypeUID)) {
            return new StiebelEltronHandler(thing);
        } else if (THING_TYPE_STIEBELELTRON_HEATPUMP_WPMSYSTEM.equals(thingTypeUID)) {
            return new StiebelEltronHandlerWpm(thing);
        } else if (THING_TYPE_STIEBELELTRON_HEATPUMP_WPM3.equals(thingTypeUID)) {
            return new StiebelEltronHandlerWpm3(thing);
        } else if (THING_TYPE_STIEBELELTRON_HEATPUMP_WPM3I.equals(thingTypeUID)) {
            return new StiebelEltronHandlerWpm3i(thing);
        } else if (THING_TYPE_STIEBELELTRON_ISG_SG_READY_EM.equals(thingTypeUID)) {
            return new StiebelEltronHandlerIsgSgReadyEm(thing);
        }

        return null;
    }
}

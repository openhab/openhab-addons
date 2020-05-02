/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal;

import static org.openhab.binding.bsblan.internal.BsbLanBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.bsblan.internal.handler.BsbLanBridgeHandler;
import org.openhab.binding.bsblan.internal.handler.BsbLanParameterHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BsbLanHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.bsblan")
public class BsbLanHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>() {

        private static final long serialVersionUID = 1L;
        {
            add(THING_TYPE_PARAMETER);
            add(THING_TYPE_BRIDGE);
        }
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_PARAMETER)) {
            return new BsbLanParameterHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new BsbLanBridgeHandler((Bridge) thing);
        }
        return null;
    }
}

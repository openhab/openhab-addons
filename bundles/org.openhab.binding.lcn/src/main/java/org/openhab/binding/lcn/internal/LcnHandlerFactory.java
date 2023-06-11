/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal;

import static org.openhab.binding.lcn.internal.LcnBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link LcnHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Fabian Wolter - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.lcn", service = ThingHandlerFactory.class)
public class LcnHandlerFactory extends BaseThingHandlerFactory {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_PCK_GATEWAY,
            THING_TYPE_MODULE, THING_TYPE_GROUP);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_GROUP.equals(thingTypeUID)) {
            return new LcnGroupHandler(thing);
        }

        if (THING_TYPE_MODULE.equals(thingTypeUID)) {
            return new LcnModuleHandler(thing);
        }

        if (THING_TYPE_PCK_GATEWAY.equals(thingTypeUID)) {
            return new PckGatewayHandler((Bridge) thing);
        }
        return null;
    }
}

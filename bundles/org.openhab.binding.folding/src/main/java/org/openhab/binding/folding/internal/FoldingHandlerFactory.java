/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.folding.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.folding.internal.handler.FoldingClientHandler;
import org.openhab.binding.folding.internal.handler.SlotHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FoldingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.folding")
public class FoldingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(FoldingBindingConstants.THING_TYPE_CLIENT, FoldingBindingConstants.THING_TYPE_SLOT));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FoldingBindingConstants.THING_TYPE_CLIENT)) {
            return new FoldingClientHandler((Bridge) thing);
        } else if (thingTypeUID.equals(FoldingBindingConstants.THING_TYPE_SLOT)) {
            return new SlotHandler(thing);
        }

        return null;
    }
}

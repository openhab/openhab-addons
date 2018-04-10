/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.folding.FoldingBindingConstants;
import org.openhab.binding.folding.handler.FoldingClientHandler;
import org.openhab.binding.folding.handler.SlotHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link FoldingHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marius Bjoernstad - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.folding")
public class FoldingHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<ThingTypeUID>(
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

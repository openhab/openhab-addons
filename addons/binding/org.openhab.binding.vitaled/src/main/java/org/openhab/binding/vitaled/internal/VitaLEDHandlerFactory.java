/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.vitaled.internal;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.vitaled.VitaLEDBindingConstants;
import org.openhab.binding.vitaled.handler.VitaLEDHandler;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Lists;

/**
 * The {@link VitaLEDHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Salein - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.vitaled")
@NonNullByDefault
public class VitaLEDHandlerFactory extends BaseThingHandlerFactory {
    private static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(VitaLEDBindingConstants.THING_TYPE_VITA_LED);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // create VitaLED thing
        VitaLEDHandler handler = new VitaLEDHandler(thing);
        return handler;
    }
}

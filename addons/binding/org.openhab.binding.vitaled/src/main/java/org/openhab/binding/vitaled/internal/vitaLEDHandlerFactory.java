/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.openhab.binding.vitaled.vitaLEDBindingConstants;
import org.openhab.binding.vitaled.handler.vitaLEDHandler;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.Lists;

/**
 * The {@link vitaLEDHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcel Salein - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.vitaled")
@NonNullByDefault
public class vitaLEDHandlerFactory extends BaseThingHandlerFactory {

    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(vitaLEDBindingConstants.THING_TYPE_VITA_LED);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        // ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        // return null;

        // create VitaLED thing
        vitaLEDHandler handler = new vitaLEDHandler(thing);
        return handler;

    }
}

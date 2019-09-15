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
package org.openhab.binding.senechome.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.senechome.internal.SenecHomeBindingConstants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.Gson;

/**
 * The {@link SenecHomeHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Steven Schwarznau - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.senechome", service = ThingHandlerFactory.class)
public class SenecHomeHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(SenecHomeBindingConstants.THING_TYPE_SENEC_HOME_BATTERY);
    
    private SenecHomeApiFactory apiFactory;

    @Activate
    public SenecHomeHandlerFactory(@Reference SenecHomeApiFactory apiFactory) {
        this.apiFactory = apiFactory;
    }
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SenecHomeBindingConstants.THING_TYPE_SENEC_HOME_BATTERY.equals(thingTypeUID)) {
            return new SenecHomeHandler(thing, new Gson(), apiFactory);
        }

        return null;
    }
}

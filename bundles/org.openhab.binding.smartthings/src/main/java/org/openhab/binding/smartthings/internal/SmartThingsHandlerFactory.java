/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.internal.SmartThingsBindingConstants.THING_TYPE_APPLIANCE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.ocf.ApplianceHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * Creates standalone handlers for Samsung OCF appliances.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class)
public class SmartThingsHandlerFactory extends BaseThingHandlerFactory {
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_APPLIANCE.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        return supportsThingType(thing.getThingTypeUID()) ? new ApplianceHandler(thing) : null;
    }
}

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
package org.openhab.binding.loxone.internal;

import java.util.Set;

import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory responsible for creating Loxone things (Miniservers) and their handlers ({@link LxServerHandler}
 *
 * @author Pawel Pieczul - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.loxone")
public class LxHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(LxBindingConstants.THING_TYPE_MINISERVER);

    private LxDynamicStateDescriptionProvider dynamicStateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID uid = thing.getThingTypeUID();
        if (uid.equals(LxBindingConstants.THING_TYPE_MINISERVER)) {
            return new LxServerHandler(thing, dynamicStateDescriptionProvider);
        }
        return null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(LxDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = provider;
    }

    protected void unsetDynamicStateDescriptionProvider(LxDynamicStateDescriptionProvider provider) {
        this.dynamicStateDescriptionProvider = null;
    }
}

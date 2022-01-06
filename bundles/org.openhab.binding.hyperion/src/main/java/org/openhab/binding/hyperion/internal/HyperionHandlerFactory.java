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
package org.openhab.binding.hyperion.internal;

import org.openhab.binding.hyperion.internal.handler.HyperionHandler;
import org.openhab.binding.hyperion.internal.handler.HyperionNgHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link HyperionHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Walters - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.hyperion")
public class HyperionHandlerFactory extends BaseThingHandlerFactory {

    private HyperionStateDescriptionProvider stateDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return HyperionBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(HyperionBindingConstants.THING_TYPE_SERVER_V1)) {
            return new HyperionHandler(thing);
        } else if (thingTypeUID.equals(HyperionBindingConstants.THING_TYPE_SERVER_NG)) {
            return new HyperionNgHandler(thing, stateDescriptionProvider);
        }

        return null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(HyperionStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(HyperionStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = null;
    }
}

/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazondashbutton.internal;

import static org.openhab.binding.amazondashbutton.internal.AmazonDashButtonBindingConstants.DASH_BUTTON_THING_TYPE;

import java.util.Collections;
import java.util.Set;

import org.openhab.binding.amazondashbutton.internal.handler.AmazonDashButtonHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link AmazonDashButtonHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Oliver Libutzki - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.amazondashbutton")
public class AmazonDashButtonHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(DASH_BUTTON_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(DASH_BUTTON_THING_TYPE)) {
            return new AmazonDashButtonHandler(thing);
        }

        return null;
    }
}

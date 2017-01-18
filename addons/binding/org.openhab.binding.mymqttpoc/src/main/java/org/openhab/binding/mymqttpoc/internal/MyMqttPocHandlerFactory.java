/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mymqttpoc.internal;

import static org.openhab.binding.mymqttpoc.MqttBindingConstants.THING_TYPE_MQTT_CLIENT;

import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mymqttpoc.handler.MqttPublisherHandler;

import com.google.common.collect.Sets;

/**
 * The {@link MyMqttPocHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class MyMqttPocHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets
            .newHashSet(MqttPublisherHandler.SUPPORTED_THING_TYPES);

    // Collections.singleton(THING_TYPE_MQTT_CLIENT);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_MQTT_CLIENT)) {
            return new MqttPublisherHandler(thing);
        }

        return null;
    }
}

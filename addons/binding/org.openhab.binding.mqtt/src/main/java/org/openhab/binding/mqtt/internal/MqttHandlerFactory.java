/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqtt.internal;

import static org.openhab.binding.mqtt.MqttBindingConstants.TOPIC_ID;

import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mqtt.handler.MqttBridgeHandler;
import org.openhab.binding.mqtt.handler.MqttHandler;

import com.google.common.collect.Sets;

/**
 * The {@link MqttHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Marcus of Wetware Labs - Initial contribution
 */
public class MqttHandlerFactory extends BaseThingHandlerFactory {

    // private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_TOPIC);
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(MqttBridgeHandler.SUPPORTED_THING_TYPES,
            MqttHandler.SUPPORTED_THING_TYPES);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    private ThingUID getBridgeThingUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration) {
        if (thingUID == null) {
            // String serialNumber = (String) configuration.get(SERIAL_NUMBER);
            // thingUID = new ThingUID(thingTypeUID, serialNumber);
            thingUID = new ThingUID(thingTypeUID, "1");
        }
        return thingUID;
    }

    private ThingUID getTopicUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
            ThingUID bridgeUID) {
        String topicId = (String) configuration.get(TOPIC_ID);

        if (thingUID == null) {
            thingUID = new ThingUID(thingTypeUID, topicId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MqttBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            MqttBridgeHandler handler = new MqttBridgeHandler((Bridge) thing);
            return handler;
        } else if (MqttHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new MqttHandler(thing);
        } else {
            return null;
        }

    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (MqttBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID mqttBridgeUID = getBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, mqttBridgeUID, null);
        }
        if (MqttHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            ThingUID topicId = getTopicUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, topicId, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the MQTT binding.");
    }

}

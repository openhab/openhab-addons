/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.*;

import java.util.Collection;
import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.homie.handler.HomieBridgeHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link HomieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieHandlerFactory extends BaseThingHandlerFactory {
    private static Logger logger = LoggerFactory.getLogger(HomieHandlerFactory.class);

    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(HOMIE_DEVICE_THING_TYPE, HOMIE_NODE_THING_TYPE);

    private MqttConnection mqttconnection = MqttConnection.getInstance();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);

        Dictionary<String, Object> properties = componentContext.getProperties();

        String brokerURL = (String) properties.get("mqttbrokerurl");
        String basetopic = (String) properties.get("basetopic");

        if (StringUtils.isNotBlank(brokerURL) && StringUtils.isNotBlank(basetopic)) {
            // mqttconnection = new MqttConnection(brokerURL, basetopic);
        }

    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(HOMIE_DEVICE_THING_TYPE)) {
            logger.info("Create homie thing for " + thing.toString());
            return new HomieBridgeHandler((Bridge) thing, mqttconnection);
        }
        return null;
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        Thing result = super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);

        return result;
    }

    public MqttConnection getMqttConnection() {
        return mqttconnection;
    }

}

/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mailserver.internal;

import static org.openhab.binding.mailserver.MailServerBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.mailserver.handler.MailServerBridgeHandler;
import org.openhab.binding.mailserver.handler.MailServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MailServerHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jereme Guenther - Initial contribution
 */
public class MailServerHandlerFactory extends BaseThingHandlerFactory {

    private Logger logger = LoggerFactory.getLogger(MailServerHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (thingTypeUID.equals(THING_BRIDGE) || thingTypeUID.equals(THING_MailBox));
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (thingTypeUID.equals(THING_BRIDGE)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (thingTypeUID.equals(THING_MailBox)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        try {
            if (thingTypeUID.equals(THING_BRIDGE)) {
                Bridge bdg = (Bridge) thing;
                return new MailServerBridgeHandler(bdg);
            } else if (thingTypeUID.equals(THING_MailBox)) {
                return new MailServerHandler(thing);
            }
        } catch (Exception e) {
            logger.debug("MAIL SERVER createHandler: '{}'", e.getMessage());

        }

        return null;
    }
}

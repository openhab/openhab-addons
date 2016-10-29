/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24.internal;

import java.util.Collection;
import java.util.function.Function;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.handler.rf24Dht11Handler;
import org.openhab.binding.rf24.handler.rf24OnOffHandler;

import com.google.common.collect.Lists;

/**
 * The {@link rf24HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24HandlerFactory extends BaseThingHandlerFactory {

    private final static Collection<ThingHandlerPair> SUPPORTED_THING_TYPES_UIDS = Lists.newArrayList(
            new ThingHandlerPair(rf24BindingConstants.RF24_DHT11_THING_TYPE, new Function<Thing, BaseThingHandler>() {

                @Override
                public BaseThingHandler apply(Thing t) {
                    return new rf24Dht11Handler(t);
                }
            }),
            new ThingHandlerPair(rf24BindingConstants.RF24_ON_OFF_THING_TYPE, new Function<Thing, BaseThingHandler>() {

                @Override
                public BaseThingHandler apply(Thing t) {
                    return new rf24OnOffHandler(t);
                }

            }));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        for (ThingHandlerPair thingHandlerPair : SUPPORTED_THING_TYPES_UIDS) {
            ThingTypeUID map = thingHandlerPair.thingTypeUID;
            if (map.equals(thingTypeUID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        for (ThingHandlerPair thingHandlerPair : SUPPORTED_THING_TYPES_UIDS) {
            if (thingHandlerPair.thingTypeUID.equals(thingTypeUID)) {
                return thingHandlerPair.handler.apply(thing);
            }
        }
        return null;
    }

    private static class ThingHandlerPair {
        private final ThingTypeUID thingTypeUID;
        private final Function<Thing, ? extends BaseThingHandler> handler;

        public ThingHandlerPair(ThingTypeUID thingTypeUID, Function<Thing, ? extends BaseThingHandler> handler) {
            this.thingTypeUID = thingTypeUID;
            this.handler = handler;
        }
    }
}

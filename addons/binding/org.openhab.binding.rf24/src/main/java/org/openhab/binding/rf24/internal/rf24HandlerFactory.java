/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24.internal;

import java.util.Collection;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.rf24.rf24BindingConstants;
import org.openhab.binding.rf24.handler.rf24BaseHandler;
import org.openhab.binding.rf24.wifi.StubWiFi;
import org.openhab.binding.rf24.wifi.WiFi;
import org.osgi.service.component.ComponentContext;

import com.google.common.collect.Lists;

/**
 * The {@link rf24HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24HandlerFactory extends BaseThingHandlerFactory {

    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(rf24BindingConstants.RF24_RECIVER_THING_TYPE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        for (ThingTypeUID thing : SUPPORTED_THING_TYPES_UIDS) {
            if (thing.equals(thingTypeUID)) {
                return true;
            }
        }
        return false;
    }

    private final WiFi wifi;

    public rf24HandlerFactory() {
        wifi = new StubWiFi();
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        wifi.init();
        super.activate(componentContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        wifi.close();
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        return new rf24BaseHandler(thing, wifi);
    }
}

/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.internal.kostal.inverter;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * @author Christian Schneider - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.kostalinverter")
public class WebscrapeHandlerFactory extends BaseThingHandlerFactory {
    public static final ThingTypeUID KOSTAL_INVERTER = new ThingTypeUID("kostalinverter", "kostalinverter");

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return thingTypeUID.equals(KOSTAL_INVERTER);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        if (supportsThingType(thing.getThingTypeUID())) {
            return new WebscrapeHandler(thing);
        }
        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
    }

}

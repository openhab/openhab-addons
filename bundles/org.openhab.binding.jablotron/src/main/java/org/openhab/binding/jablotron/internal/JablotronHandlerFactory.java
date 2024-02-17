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
package org.openhab.binding.jablotron.internal;

import static org.openhab.binding.jablotron.JablotronBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.jablotron.internal.handler.JablotronBridgeHandler;
import org.openhab.binding.jablotron.internal.handler.JablotronJa100FHandler;
import org.openhab.binding.jablotron.internal.handler.JablotronJa100Handler;
import org.openhab.binding.jablotron.internal.handler.JablotronOasisHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link JablotronHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.jablotron", service = ThingHandlerFactory.class)
public class JablotronHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;

    @Activate
    public JablotronHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_BRIDGE.equals(thingTypeUID) || SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new JablotronBridgeHandler((Bridge) thing, httpClientFactory.getCommonHttpClient());
        } else if (thingTypeUID.equals(THING_TYPE_OASIS)) {
            return new JablotronOasisHandler(thing, "OASIS");
        } else if (thingTypeUID.equals(THING_TYPE_JA100)) {
            return new JablotronJa100Handler(thing, "JA100");
        } else if (thingTypeUID.equals(THING_TYPE_JA100F)) {
            return new JablotronJa100FHandler(thing, "JA100F");
        }
        return null;
    }
}

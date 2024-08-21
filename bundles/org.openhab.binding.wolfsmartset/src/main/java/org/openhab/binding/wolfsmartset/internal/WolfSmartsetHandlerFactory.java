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
package org.openhab.binding.wolfsmartset.internal;

import static org.openhab.binding.wolfsmartset.internal.WolfSmartsetBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.wolfsmartset.internal.handler.WolfSmartsetAccountBridgeHandler;
import org.openhab.binding.wolfsmartset.internal.handler.WolfSmartsetSystemBridgeHandler;
import org.openhab.binding.wolfsmartset.internal.handler.WolfSmartsetUnitThingHandler;
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
 * The {@link WolfSmartsetHandlerFactory} is responsible for creating thing handlers
 * for the account bridge, system bridge, and unit thing.
 *
 * @author Bo Biene - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.wolfsmartset", service = ThingHandlerFactory.class)
public class WolfSmartsetHandlerFactory extends BaseThingHandlerFactory {
    private final HttpClient httpClient;

    @Activate
    public WolfSmartsetHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (SUPPORTED_ACCOUNT_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new WolfSmartsetAccountBridgeHandler((Bridge) thing, httpClient);
        }
        if (SUPPORTED_SYSTEM_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new WolfSmartsetSystemBridgeHandler((Bridge) thing);
        }
        if (SUPPORTED_UNIT_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new WolfSmartsetUnitThingHandler(thing);
        }
        return null;
    }
}

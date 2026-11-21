/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.myvaillant.internal;

import static org.openhab.binding.myvaillant.internal.MyVaillantBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myvaillant.internal.handler.AccountHandler;
import org.openhab.binding.myvaillant.internal.handler.CircuitHandler;
import org.openhab.binding.myvaillant.internal.handler.DeviceHandler;
import org.openhab.binding.myvaillant.internal.handler.DhwHandler;
import org.openhab.binding.myvaillant.internal.handler.SystemHandler;
import org.openhab.binding.myvaillant.internal.handler.ZoneHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
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
 * Creates the handlers of the myVAILLANT binding.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.myvaillant", service = ThingHandlerFactory.class)
public class MyVaillantHandlerFactory extends BaseThingHandlerFactory {

    private final HttpClientFactory httpClientFactory;
    private final TimeZoneProvider timeZoneProvider;
    private final Storage<String> requestCountStorage;

    @Activate
    public MyVaillantHandlerFactory(@Reference HttpClientFactory httpClientFactory,
            @Reference TimeZoneProvider timeZoneProvider, @Reference StorageService storageService) {
        this.httpClientFactory = httpClientFactory;
        this.timeZoneProvider = timeZoneProvider;
        this.requestCountStorage = storageService.getStorage(BINDING_ID + ".requestCounts");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (THING_TYPE_ACCOUNT.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new AccountHandler(bridge, httpClientFactory, timeZoneProvider, requestCountStorage);
        } else if (THING_TYPE_SYSTEM.equals(thingTypeUID) && thing instanceof Bridge bridge) {
            return new SystemHandler(bridge);
        } else if (THING_TYPE_ZONE.equals(thingTypeUID)) {
            return new ZoneHandler(thing);
        } else if (THING_TYPE_CIRCUIT.equals(thingTypeUID)) {
            return new CircuitHandler(thing);
        } else if (THING_TYPE_DHW.equals(thingTypeUID)) {
            return new DhwHandler(thing);
        } else if (THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return new DeviceHandler(thing);
        }
        return null;
    }
}

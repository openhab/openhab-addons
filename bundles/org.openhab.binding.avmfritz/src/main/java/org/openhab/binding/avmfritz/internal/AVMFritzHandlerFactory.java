/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.avmfritz.internal;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzButtonHandler;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzColorLightDeviceHandler;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzHeatingDeviceHandler;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzHeatingGroupHandler;
import org.openhab.binding.avmfritz.internal.handler.BoxHandler;
import org.openhab.binding.avmfritz.internal.handler.DeviceHandler;
import org.openhab.binding.avmfritz.internal.handler.GroupHandler;
import org.openhab.binding.avmfritz.internal.handler.Powerline546EHandler;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AVMFritzHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Robert Bausdorf - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.avmfritz")
@NonNullByDefault
public class AVMFritzHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzHandlerFactory.class);

    private final HttpClient httpClient;
    private final AVMFritzDynamicCommandDescriptionProvider commandDescriptionProvider;

    @Activate
    public AVMFritzHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference AVMFritzDynamicCommandDescriptionProvider stateDescriptionProvider) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.commandDescriptionProvider = stateDescriptionProvider;
    }

    /**
     * Provides the supported thing types
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * Create handler of things.
     */
    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            return new BoxHandler((Bridge) thing, httpClient, commandDescriptionProvider);
        } else if (POWERLINE546E_STANDALONE_THING_TYPE.equals(thingTypeUID)) {
            return new Powerline546EHandler((Bridge) thing, httpClient, commandDescriptionProvider);
        } else if (SUPPORTED_LIGHTING_THING_TYPES.contains(thingTypeUID)) {
            return new AVMFritzColorLightDeviceHandler(thing);
        } else if (SUPPORTED_BUTTON_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new AVMFritzButtonHandler(thing);
        } else if (SUPPORTED_HEATING_THING_TYPES.contains(thingTypeUID)) {
            return new AVMFritzHeatingDeviceHandler(thing);
        } else if (SUPPORTED_DEVICE_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new DeviceHandler(thing);
        } else if (GROUP_HEATING_THING_TYPE.equals(thingTypeUID)) {
            return new AVMFritzHeatingGroupHandler(thing);
        } else if (SUPPORTED_GROUP_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new GroupHandler(thing);
        } else {
            logger.error("ThingHandler not found for {}", thingTypeUID);
        }
        return null;
    }
}

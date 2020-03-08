/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.ojelectronics.internal;

import static org.openhab.binding.ojelectronics.internal.OJElectronicsBindingConstants.THING_TYPE_BRIDGE;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create OJElectronicsBridgeHandler
 *
 * @author Christian Kittel - Initial Contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.ojelectronics", service = ThingHandlerFactory.class)
public class OJElectronicsBridgeHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(OJElectronicsBridgeHandlerFactory.class);

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BRIDGE);

    private final HttpClient httpClient;

    /**
     * Creates a new factory
     *
     * @param httpClientFactory Factory for HttpClient
     */
    @Activate
    public OJElectronicsBridgeHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    /**
     * Supported things for this factory
     */
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (SUPPORTED_THING_TYPES_UIDS.contains(thing.getThingTypeUID())) {
            logger.trace("OJElectronicsBridgeHandlerFactory.createHandler({})", thing);
            OJElectronicsBridgeHandler handler = new OJElectronicsBridgeHandler((Bridge) thing, httpClient);
            return handler;
        }
        return null;
    }

}
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
package org.openhab.binding.tailwind.internal.factory;

import static org.openhab.binding.tailwind.internal.TailwindBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.tailwind.internal.handler.TailwindHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TailwindHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Dave J. Schoepel - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.tailwind", service = ThingHandlerFactory.class)
public class TailwindHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(TailwindHandlerFactory.class);
    private final @NonNullByDefault({}) HttpClient httpClient;
    // private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_TAILWIND);

    @Activate
    public TailwindHandlerFactory(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("supportsThingType is using thingTypeUID: {}", thingTypeUID);
        if (SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID)) {
            if (logger.isDebugEnabled()) {
                logger.debug("supportsThingType found thingTypeUID: {} in SUPPORTED_THING_TYPE_UIDS.", thingTypeUID);
            } // If debug, log which thing type is used
        }
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (logger.isDebugEnabled()) {
            logger.debug("createHandler is comparing {} to list of supported thing types: {}", thingTypeUID,
                    SUPPORTED_THING_TYPE_UIDS);
        }

        if (SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID)) {
            Map<String, String> properties = new HashMap<>(2);
            properties = thing.getProperties();
            logger.debug("Properties size is: {}", properties.size());
            if (properties.isEmpty()) {
                Map<String, String> defaultProperties = new HashMap<>(2);
                // Store the properties of the mDNS query in the thing
                defaultProperties.put(Thing.PROPERTY_MAC_ADDRESS, "000000000000");
                defaultProperties.put(Thing.PROPERTY_MODEL_ID, "iQ3");
                defaultProperties.put(Thing.PROPERTY_VENDOR, "tailwind");
                defaultProperties.put(Thing.PROPERTY_HARDWARE_VERSION, "");
                defaultProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, "");
                defaultProperties.put(TAILWIND_PROPERTY_MAX_DOORS, "3");
                defaultProperties.put(TAILWIND_HTTP_SERVER_URL, "");
                thing.setProperties(defaultProperties);
            }
            return new TailwindHandler(thing, httpClient);
        }
        return null;
    }
}

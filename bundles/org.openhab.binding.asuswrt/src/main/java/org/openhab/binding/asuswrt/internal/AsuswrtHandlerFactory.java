/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.asuswrt.internal;

import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingConstants.*;
import static org.openhab.binding.asuswrt.internal.constants.AsuswrtBindingSettings.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link asuswrtHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.asuswrt", service = ThingHandlerFactory.class)
public class AsuswrtHandlerFactory extends BaseThingHandlerFactory {
    private final Logger logger = LoggerFactory.getLogger(AsuswrtHandlerFactory.class);
    private final HttpClient httpClient;

    public AsuswrtHandlerFactory() {
        // create new httpClient
        httpClient = new HttpClient();
        httpClient.setFollowRedirects(false);
        httpClient.setMaxConnectionsPerDestination(HTTP_MAX_CONNECTIONS);
        httpClient.setMaxRequestsQueuedPerDestination(HTTP_MAX_QUEUED_REQUESTS);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.error("cannot start httpClient");
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_ROUTER.equals(thingTypeUID)) {
            return new AsuswrtRouter(thing, this.httpClient);
        }
        return null;
    }
}

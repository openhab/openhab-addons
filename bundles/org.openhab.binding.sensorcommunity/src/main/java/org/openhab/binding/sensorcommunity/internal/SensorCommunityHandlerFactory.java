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
package org.openhab.binding.sensorcommunity.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sensorcommunity.internal.handler.ConditionHandler;
import org.openhab.binding.sensorcommunity.internal.handler.HTTPHandler;
import org.openhab.binding.sensorcommunity.internal.handler.NoiseHandler;
import org.openhab.binding.sensorcommunity.internal.handler.PMHandler;
import org.openhab.core.io.net.http.HttpClientFactory;
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
 * The {@link SensorCommunityHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sensorcommunity", service = ThingHandlerFactory.class)
public class SensorCommunityHandlerFactory extends BaseThingHandlerFactory {
    protected final Logger logger = LoggerFactory.getLogger(SensorCommunityHandlerFactory.class);

    @Activate
    public SensorCommunityHandlerFactory(final @Reference HttpClientFactory httpClientFactory) {
        HTTPHandler.init(httpClientFactory.getCommonHttpClient());
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return (thingTypeUID.equals(SensorCommunityBindingConstants.THING_TYPE_PARTICULATE)
                || thingTypeUID.equals(SensorCommunityBindingConstants.THING_TYPE_CONDITIONS)
                || thingTypeUID.equals(SensorCommunityBindingConstants.THING_TYPE_NOISE));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(SensorCommunityBindingConstants.THING_TYPE_PARTICULATE)) {
            return new PMHandler(thing);
        } else if (thing.getThingTypeUID().equals(SensorCommunityBindingConstants.THING_TYPE_CONDITIONS)) {
            return new ConditionHandler(thing);
        } else if (thing.getThingTypeUID().equals(SensorCommunityBindingConstants.THING_TYPE_NOISE)) {
            return new NoiseHandler(thing);
        }
        logger.info("Handler for {} not found", thing.getThingTypeUID());
        return null;
    }
}

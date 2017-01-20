/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie.internal;

import static org.openhab.binding.homie.HomieBindingConstants.HOMIE_DEVICE_THING_TYPE;

import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.homie.HomieChannelTypeProvider;
import org.openhab.binding.homie.handler.HomieDeviceHandler;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * The {@link HomieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieHandlerFactory extends BaseThingHandlerFactory {
    private static Logger logger = LoggerFactory.getLogger(HomieHandlerFactory.class);

    private final static Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Lists
            .newArrayList(HOMIE_DEVICE_THING_TYPE);

    private HomieConfiguration configuration;

    private HomieChannelTypeProvider provider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    protected void setChannelProvider(HomieChannelTypeProvider provider) {
        this.provider = provider;
    }

    protected void unsetChannelProvider(HomieChannelTypeProvider provider) {
        this.provider = null;
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        configuration = new HomieConfiguration(componentContext.getProperties());

        DeviceDiscoveryService service = new DeviceDiscoveryService(configuration);
        bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<String, Object>());
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (thingTypeUID.equals(HOMIE_DEVICE_THING_TYPE)) {
            logger.info("Create homie thing for " + thing.toString());

            HomieDeviceHandler handler = new HomieDeviceHandler(thing, provider, configuration);
            return handler;
        }
        return null;
    }

}

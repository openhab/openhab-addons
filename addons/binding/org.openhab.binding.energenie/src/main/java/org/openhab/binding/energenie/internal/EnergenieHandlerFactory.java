/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal;

import static org.openhab.binding.energenie.EnergenieBindingConstants.*;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.energenie.handler.EnergenieGatewayHandler;
import org.openhab.binding.energenie.handler.EnergenieSubdevicesHandler;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandler;
import org.openhab.binding.energenie.internal.api.manager.FailingRequestHandlerImpl;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.internal.discovery.EnergenieDiscoveryService;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * The {@link EnergenieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Svilen Valkanov - Initial contribution
 */
public class EnergenieHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_GATEWAY,
            THING_TYPE_ENERGY_MONITOR, THING_TYPE_MOTION_SENSOR, THING_TYPE_OPEN_SENSOR);

    RestClient client;
    ThingRegistry registry;
    Logger logger = LoggerFactory.getLogger(EnergenieHandlerFactory.class);

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        String user = (String) properties.get("user");
        String password = (String) properties.get("password");
        registerEnergenieDiscoveryService(user, password);
    }

    private void registerEnergenieDiscoveryService(String user, String password) {
        EnergenieApiConfiguration config = new EnergenieApiConfiguration(user, password);
        FailingRequestHandler handler = new FailingRequestHandlerImpl();
        EnergenieApiManager apiManager = new EnergenieApiManagerImpl(config, client, handler);
        EnergenieDiscoveryService service = new EnergenieDiscoveryService(apiManager, registry);
        bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<String, Object>());
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    public void setRestClient(RestClient client) {
        this.client = client;
    }

    public void unsetRestClient() {
        this.client = null;
    }

    public void setThingRegistry(ThingRegistry registry) {
        this.registry = registry;
    }

    public void unsetThingRegistry() {
        this.registry = null;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new EnergenieGatewayHandler((Bridge) thing);
        } else {
            return new EnergenieSubdevicesHandler(thing);
        }
    }
}

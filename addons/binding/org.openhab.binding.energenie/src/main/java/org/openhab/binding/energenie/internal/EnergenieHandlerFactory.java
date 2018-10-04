/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.energenie.handler.EnergenieGatewayHandler;
import org.openhab.binding.energenie.handler.EnergenieSubdevicesHandler;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiConfiguration;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManager;
import org.openhab.binding.energenie.internal.api.manager.EnergenieApiManagerImpl;
import org.openhab.binding.energenie.internal.discovery.EnergenieDiscoveryService;
import org.openhab.binding.energenie.internal.rest.RestClient;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import com.google.common.collect.Sets;

/**
 * The {@link EnergenieHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Svilen Valkanov - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.energenie")
public class EnergenieHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_GATEWAY,
            THING_TYPE_ENERGY_MONITOR, THING_TYPE_MOTION_SENSOR, THING_TYPE_OPEN_SENSOR);

    private RestClient restClient;
    private EnergenieApiConfiguration apiConfig;

    @Override
    @Activate
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        Dictionary<String, Object> properties = componentContext.getProperties();
        String user = (String) properties.get("user");
        String password = (String) properties.get("password");
        apiConfig = new EnergenieApiConfiguration(user, password);
        registerEnergenieDiscoveryService(apiConfig);
    }

    private void registerEnergenieDiscoveryService(EnergenieApiConfiguration config) {
        EnergenieApiManager apiManager = new EnergenieApiManagerImpl(config, restClient);
        EnergenieDiscoveryService service = new EnergenieDiscoveryService(apiManager);
        bundleContext.registerService(DiscoveryService.class.getName(), service, new Hashtable<String, Object>());
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC)
    public void setRestClient(RestClient client) {
        this.restClient = client;
    }

    public void unsetRestClient(RestClient client) {
        this.restClient = null;
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_GATEWAY)) {
            return new EnergenieGatewayHandler((Bridge) thing, apiConfig, restClient);
        } else {
            return new EnergenieSubdevicesHandler(thing);
        }
    }

    public void setApiConfig(EnergenieApiConfiguration apiConfig) {
        this.apiConfig = apiConfig;
    }
}

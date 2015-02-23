/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal;

import java.util.Hashtable;
import java.util.Set;

import org.openhab.binding.rfxcom.RFXComBindingConstants;
import org.openhab.binding.rfxcom.discovery.RFXComDeviceDiscoveryService;
import org.openhab.binding.rfxcom.handler.RFXComBridgeHandler;
import org.openhab.binding.rfxcom.handler.RFXComHandler;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link RFXComHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComHandlerFactory extends BaseThingHandlerFactory {
    
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(RFXComHandlerFactory.class);
	private ServiceRegistration<?> discoveryServiceReg;

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES = Sets.union(
			RFXComBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS,
			RFXComBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS);
	
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (RFXComBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID)) {
    		RFXComBridgeHandler handler = new RFXComBridgeHandler((Bridge) thing);
			registerDeviceDiscoveryService(handler);
			return handler;
		} else if (supportsThingType(thingTypeUID)) {
			return new RFXComHandler(thing);
		}

        return null;
    }

	@Override
	protected void removeHandler(ThingHandler thingHandler) {
		if (this.discoveryServiceReg != null) {
			RFXComDeviceDiscoveryService service = (RFXComDeviceDiscoveryService) bundleContext
					.getService(discoveryServiceReg.getReference());
			service.deactivate();
			discoveryServiceReg.unregister();
			discoveryServiceReg = null;
		}
		super.removeHandler(thingHandler);
	}

	private void registerDeviceDiscoveryService(RFXComBridgeHandler rfxcomBridgeHandler) {
		RFXComDeviceDiscoveryService discoveryService = new RFXComDeviceDiscoveryService(rfxcomBridgeHandler);
		discoveryService.activate();
		this.discoveryServiceReg = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
				new Hashtable<String, Object>());
	}

}


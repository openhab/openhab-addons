/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openhab.binding.pulseaudio.PulseaudioBindingConstants;
import org.openhab.binding.pulseaudio.handler.PulseaudioBridgeHandler;
import org.openhab.binding.pulseaudio.handler.PulseaudioHandler;
import org.openhab.binding.pulseaudio.internal.discovery.PulseaudioDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;

import com.google.common.collect.Sets;

/**
 * The {@link PulseaudioHandlerFactory} is responsible for creating things and thing 
 * handlers.
 * 
 * @author Tobias Bräutigam - Initial contribution
 */
public class PulseaudioHandlerFactory extends BaseThingHandlerFactory {
	private Logger logger = LoggerFactory.getLogger(PulseaudioHandlerFactory.class);
    
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.union(
    		PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS,
    		PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS);
    
    private Map<ThingHandler,ServiceRegistration<?>> discoveryServiceReg = new HashMap<ThingHandler,ServiceRegistration<?>>();
    
    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }
    
    @Override
	public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
			ThingUID bridgeUID) {
    	if (PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
    		return super.createThing(thingTypeUID, configuration, thingUID, null);
    	}
    	if (PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
    		ThingUID deviceUID = getPulseaudioDeviceUID(thingTypeUID, thingUID, configuration, bridgeUID);
    		return super.createThing(thingTypeUID, configuration, deviceUID, bridgeUID);
    	}
    	throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the binding.");
    }
    
    private void registerDeviceDiscoveryService(PulseaudioBridgeHandler paBridgeHandler) {
    	PulseaudioDeviceDiscoveryService discoveryService = new PulseaudioDeviceDiscoveryService(paBridgeHandler);
    	discoveryService.activate();
    	this.discoveryServiceReg.put(paBridgeHandler,bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
				new Hashtable<String, Object>()));
	}
    
    private ThingUID getPulseaudioDeviceUID(ThingTypeUID thingTypeUID, ThingUID thingUID, Configuration configuration,
			ThingUID bridgeUID) {
		if (thingUID == null) {
			String name = (String) configuration.get(PulseaudioBindingConstants.DEVICE_PARAMETER_NAME);
			thingUID = new ThingUID(thingTypeUID, name, bridgeUID.getId());
		}
		return thingUID;
	}
    
    @Override
	protected void removeHandler(ThingHandler thingHandler) {
		if (this.discoveryServiceReg.containsKey(thingHandler)) {
			PulseaudioDeviceDiscoveryService service = (PulseaudioDeviceDiscoveryService) bundleContext
					.getService(discoveryServiceReg.get(thingHandler).getReference());
			service.deactivate();
			discoveryServiceReg.get(thingHandler).unregister();
			discoveryServiceReg.remove(thingHandler);
		}
		super.removeHandler(thingHandler);
	}

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (PulseaudioBridgeHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
        	PulseaudioBridgeHandler handler = new PulseaudioBridgeHandler((Bridge)thing);
        	registerDeviceDiscoveryService(handler);
        	return handler;
        }
        else if (PulseaudioHandler.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            return new PulseaudioHandler(thing);
        }
        
        return null;
    }
}


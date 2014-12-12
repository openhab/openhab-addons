/**
* Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*/

package org.openhab.binding.BluetoothLE.internal;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.DiscoveryServiceRegistry;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.BluetoothLE.BluetoothLEBindingConstants;
import org.openhab.binding.BluetoothLE.discovery.BluetoothLEDiscoveryService;
import org.openhab.binding.BluetoothLE.handler.BluetoothLENonConnHandler;

/**
 * The {@link BluetoothLEHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Patrick Ammann - Initial contribution
 */
public class BluetoothLEHandlerFactory extends BaseThingHandlerFactory {
	private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = BluetoothLEBindingConstants.SUPPORTED_THING_TYPES_UIDS;
	
	private BluetoothLEDiscoveryService discoveryService;
	private DiscoveryServiceRegistry discoveryServiceRegistry;

	@Override
	public boolean supportsThingType(ThingTypeUID thingTypeUID) {
		return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
	}
	
	@Override
	protected ThingHandler createHandler(Thing thing) {
		ThingTypeUID thingTypeUID = thing.getThingTypeUID();
		if (BluetoothLEBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
			return new BluetoothLENonConnHandler(thing, discoveryService, discoveryServiceRegistry);
		}
		return null;
	}
	
	protected void setDiscoveryService(BluetoothLEDiscoveryService discoverySservice) {
		this.discoveryService = discoverySservice;
	}
	
	protected void unsetDiscoveryService(BluetoothLEDiscoveryService discoverySservice) {
		this.discoveryService = null;
	}
	
    protected void setDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
        this.discoveryServiceRegistry = discoveryServiceRegistry;
    }
    
    protected void unsetDiscoveryServiceRegistry(DiscoveryServiceRegistry discoveryServiceRegistry) {
    	this.discoveryServiceRegistry = null;
    }
}

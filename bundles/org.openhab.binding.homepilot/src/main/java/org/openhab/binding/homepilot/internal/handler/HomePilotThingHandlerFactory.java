/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.homepilot.internal.handler;

import static org.openhab.binding.homepilot.HomePilotBindingConstants.BINDING_ID;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.homepilot.internal.discovery.HomePilotDeviceDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link HomePilotThingHandlerFactory} is responsible for creating things
 * and thing handlers.
 *
 * @author Steffen Stundzig - Initial contribution
 */
@Component(configurationPid = "binding.homepilot", service = ThingHandlerFactory.class, immediate = true)
public class HomePilotThingHandlerFactory extends BaseThingHandlerFactory {

	private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

	@Override
	public boolean supportsThingType(ThingTypeUID thingTypeUID) {
		return BINDING_ID.equals(thingTypeUID.getBindingId());
	}

	@Override
	protected ThingHandler createHandler(Thing thing) {
		if (thing instanceof Bridge) {
			final HomePilotBridgeHandler bridgeHandler = new HomePilotBridgeHandler((Bridge) thing);
			registerDiscoveryService(bridgeHandler);
			return bridgeHandler;

		} else {
			return new HomePilotThingHandler(thing);
		}
	}

	private synchronized void registerDiscoveryService(HomePilotBridgeHandler bridgeHandler) {
		HomePilotDeviceDiscoveryService discoveryService = new HomePilotDeviceDiscoveryService(bridgeHandler);

		this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), getBundleContext()
				.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
		discoveryService.startScan(null);
		discoveryService.waitForScanFinishing();
		bridgeHandler.initialize();
	}

	@Override
	protected synchronized void removeHandler(ThingHandler thingHandler) {
		if (thingHandler instanceof HomePilotThingHandler) {
			ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
			if (serviceReg != null) {
				serviceReg.unregister();
			}
		}
	}
}

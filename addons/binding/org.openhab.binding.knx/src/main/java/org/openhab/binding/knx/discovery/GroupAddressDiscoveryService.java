/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.handler.physical.GroupAddressThingHandler;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.IndividualAddress;
import com.google.common.collect.Sets;

/**
 * The {@link GroupAddressDiscoveryService} class provides a discovery
 * mechanism for KNX Group Addresses
 * 
 * @author  Karel Goderis - Initial contribution
 */
public class GroupAddressDiscoveryService extends AbstractDiscoveryService implements KNXBusListener {

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(
			KNXBindingConstants.THING_TYPE_GROUPADDRESS);

	private final static int SEARCH_TIME = 180;
	private boolean searchOngoing = false;


	private KNXBridgeBaseThingHandler bridgeHandler;


	public GroupAddressDiscoveryService(KNXBridgeBaseThingHandler bridgeHandler) throws IllegalArgumentException {
		super(SUPPORTED_THING_TYPES_UIDS,SEARCH_TIME,false);
		this.bridgeHandler = bridgeHandler;
	}

	@Override
	protected void startScan() {
		searchOngoing = true;
	}

	@Override
	protected void stopScan() {
		searchOngoing = false;
	}

	public void activate() {
		bridgeHandler.registerKNXBusListener(this);
	}

	public void deactivate() {
		bridgeHandler.unregisterKNXBusListener(this);
	}

	@Override
	public void onActivity(IndividualAddress source, GroupAddress destination,
			byte[] asdu) {
		if(searchOngoing) {
			ThingUID bridgeUID = bridgeHandler.getThing().getUID();
			ThingUID thingUID = new ThingUID(KNXBindingConstants.THING_TYPE_GROUPADDRESS, destination.toString().replaceAll("/", "_"), bridgeUID.getId());

			Map<String, Object> properties = new HashMap<>(1);
			properties.put(GroupAddressThingHandler.ADDRESS, destination.toString());
			properties.put(GroupAddressThingHandler.READ,Boolean.FALSE);
			DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
					.withProperties(properties)
					.withBridge(bridgeUID)
					.withLabel("Group Address "+destination.toString())
					.build();

			thingDiscovered(discoveryResult);
		}
	}

}

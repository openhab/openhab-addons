/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonos.discovery;

import static org.openhab.binding.sonos.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;
import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.FRIENDLY_NAME;
import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.UDN;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.discovery.*;

/**
 * The {@link ZonePlayerDiscoveryParticipant} is responsible processing the
 * results of searches for UPNP devices
 * 
 * @author Karel Goderis - Initial contribution
 */
public class ZonePlayerDiscoveryParticipant implements UpnpDiscoveryParticipant {

	private Logger logger = LoggerFactory
			.getLogger(ZonePlayerDiscoveryParticipant.class);

	@Override
	public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
		return Collections.singleton(ZONEPLAYER_THING_TYPE_UID);
	}

	@Override
	public DiscoveryResult createResult(RemoteDevice device) {
		ThingUID uid = getThingUID(device);
		if (uid != null) {
			Map<String, Object> properties = new HashMap<>(3);
			String label = "Sonos device";
			try {
				label = device.getDetails().getModelDetails().getModelName();
			} catch(Exception e) {
				// ignore and use default label
			}
			properties.put(UDN, device.getIdentity().getUdn()
					.getIdentifierString());

			DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel(label).build();

			logger.debug(
					"Created a DiscoveryResult for device '{}' with UDN '{}'",
					device.getDetails().getFriendlyName(), device.getIdentity()
					.getUdn().getIdentifierString());
			return result;
		} else {
			return null;
		}
	}

	@Override
	public ThingUID getThingUID(RemoteDevice device) {
		if (device != null) {
			if(device.getDetails().getManufacturerDetails().getManufacturer() != null) {
				if (device.getDetails().getManufacturerDetails().getManufacturer()
						.toUpperCase().contains("SONOS")) {
					logger.debug(
							"Discovered a Sonos Zone Player thing with UDN '{}'",
							device.getIdentity().getUdn().getIdentifierString());
					return new ThingUID(ZONEPLAYER_THING_TYPE_UID, device
							.getIdentity().getUdn().getIdentifierString());
				} 
			}
		}
		return null;
	}
}

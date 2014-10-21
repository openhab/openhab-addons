package org.openhab.binding.sonos.discovery;

import static org.openhab.binding.sonos.SonosBindingConstants.ZONEPLAYER_THING_TYPE_UID;
import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.FRIENDLY_NAME;
import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.UDN;
import static org.openhab.binding.sonos.config.ZonePlayerConfiguration.DEVICE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.jupnp.model.meta.RemoteDevice;
import org.openhab.binding.sonos.handler.ZonePlayerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.smarthome.config.discovery.*;

public class ZonePlayerDiscoveryParticipant implements UpnpDiscoveryParticipant {
	
	private Logger logger = LoggerFactory.getLogger(ZonePlayerDiscoveryParticipant.class);

	
	@Override
	public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
		return Collections.singleton(ZONEPLAYER_THING_TYPE_UID);
	}

	@Override
	public DiscoveryResult createResult(RemoteDevice device) {
		ThingUID uid = getThingUID(device);
		if(uid!=null) {
	        Map<String, Object> properties = new HashMap<>(3);
	        properties.put(FRIENDLY_NAME, device.getDetails().getFriendlyName());
	        properties.put(UDN, device.getIdentity().getUdn().getIdentifierString());
//	        properties.put(DEVICE,device);

	        DiscoveryResult result = DiscoveryResultBuilder.create(uid)
					.withProperties(properties)
					.withLabel(device.getDetails().getFriendlyName())
					.build();
	        
	        logger.debug("Created a DiscoveryResult for device '{}' with UDN '{}'",device.getDetails().getFriendlyName(),device.getIdentity().getUdn().getIdentifierString());
	        return result;
		} else {
			return null;
		}
	}

	@Override
	public ThingUID getThingUID(RemoteDevice device) {
		if(device.getDetails().getManufacturerDetails().getManufacturer().toUpperCase().contains("SONOS")) {
			logger.debug("Discovered a Sonos Zone Player thing with UDN '{}'",device.getIdentity().getUdn().getIdentifierString());
			return new ThingUID(ZONEPLAYER_THING_TYPE_UID, device.getIdentity().getUdn().getIdentifierString());
		} else {
			return null;
		}
	}

}

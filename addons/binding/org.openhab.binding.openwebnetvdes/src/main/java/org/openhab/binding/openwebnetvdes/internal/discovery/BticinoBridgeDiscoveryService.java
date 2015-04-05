package org.openhab.binding.openwebnetvdes.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants;

public class BticinoBridgeDiscoveryService extends AbstractDiscoveryService {

	public BticinoBridgeDiscoveryService(Set<ThingTypeUID> supportedThingTypes,
			int timeout) throws IllegalArgumentException {
		super(OpenWebNetVdesBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS, 15);
		// TODO Auto-generated constructor stub
	}
		
	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return OpenWebNetVdesBindingConstants.SUPPORTED_BRIDGE_THING_TYPES_UIDS;
	}


	@Override
	protected void startScan() {
		
		String ipAddress = "192.168.0.108";
		Map<String, Object> properties = new HashMap<>();
		properties.put(OpenWebNetVdesBindingConstants.IP_ADDRESS, ipAddress);
		//properties.put(MaxBinding.SERIAL_NUMBER, cubeSerialNumber);
		ThingUID uid = new ThingUID(OpenWebNetVdesBindingConstants.IP_2WIRE_INTERFACE_THING_TYPE, ipAddress);
		if (uid != null) {
			DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
					.withLabel("MAX! Cube LAN Gateway").build();
			thingDiscovered(result);
		}

	}

	
}

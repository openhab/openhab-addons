package org.openhab.binding.openwebnetvdes.internal.discovery;

import static org.openhab.binding.openwebnetvdes.OpenWebNetVdesBindingConstants.*;

import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openwebnetvdes.devices.BticinoDevice;
import org.openhab.binding.openwebnetvdes.handler.DeviceStatusListener;
import org.openhab.binding.openwebnetvdes.handler.Ip2WireBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BticinoDeviceDiscoveryService extends AbstractDiscoveryService
		implements DeviceStatusListener {

	private final static Logger logger = LoggerFactory.getLogger(BticinoDeviceDiscoveryService.class);

	private Ip2WireBridgeHandler ip2WireBridgeHandler;

	public BticinoDeviceDiscoveryService(Ip2WireBridgeHandler ip2WireBridgeHandler) {
		super(SUPPORTED_DEVICE_THING_TYPES_UIDS, 10, true);
		this.ip2WireBridgeHandler = ip2WireBridgeHandler;
	}

	public void activate() {
		ip2WireBridgeHandler.registerDeviceStatusListener(this);
	}

	public void deactivate() {
		ip2WireBridgeHandler.unregisterDeviceStatusListener(this);
	}

	@Override
	public Set<ThingTypeUID> getSupportedThingTypes() {
		return SUPPORTED_DEVICE_THING_TYPES_UIDS;
	}

	@Override
	public void onDeviceAdded(Bridge bridge, BticinoDevice device) {
		logger.trace("Adding new Bticino {} with id '{}' to smarthome inbox", device.getType(), device.getWhereAddress());
		ThingUID thingUID = null;
		switch (device.getType()) {
		case VIDEO_CAMERA_ENTRANCE_PANEL:
			thingUID = new ThingUID(VIDEO_CAMERA_ENTRANCE_PANEL_THING_TYPE, bridge.getUID(), 
					String.valueOf(device.getWhereAddress()));
			break;
		case INDOOR_CAMERA:
			thingUID = new ThingUID(APARTMENT_CAMERA_THING_TYPE, bridge.getUID(), 
					String.valueOf(device.getWhereAddress()));
			break;
		case DOOR_LOCK_ACTUATOR:
			thingUID = new ThingUID(DOOR_LOCK_ACTUATOR_THING_TYPE, bridge.getUID(),
					String.valueOf(device.getWhereAddress()));
			break;
		default:
			break;
		}
		if (thingUID != null) {
			DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
					.withProperty(OWN_WHERE_ADDRESS, device.getWhereAddress()).withBridge(bridge.getUID())
					.withLabel(device.getType().toString())
					.build();
			thingDiscovered(discoveryResult);
		} else {
			logger.debug("Discovered Btcino device is unsupported: type '{}' with WHERE #'{}'", device.getType(),
					device.getWhereAddress());
		}
	}

	@Override
	protected void startScan() {
		// this can be ignored here as we discover via the bridge
	}

	@Override
	public void onDeviceStateChanged(ThingUID bridge, BticinoDevice device) {
		// this can be ignored here
	}

	@Override
	public void onDeviceRemoved(Ip2WireBridgeHandler bridge, BticinoDevice device) {
		// this can be ignored here
	}

}

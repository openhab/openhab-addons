package org.openhab.binding.insteonplm.internal.discovery;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.handler.InsteonPLMBridgeHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceType;
import org.openhab.binding.insteonplm.internal.device.DeviceType.FeatureGroup;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Interfaces with the discovery system to discover exciting new insteon devices.
 */
public class InsteonDiscoveryHandler extends AbstractDiscoveryService {
    private Logger logger = LoggerFactory.getLogger(InsteonPLMBridgeHandler.class);
    InsteonPLMBridgeHandler bridge;

    public InsteonDiscoveryHandler(InsteonPLMBridgeHandler bridge, int timeout) throws IllegalArgumentException {
        super(timeout);
    }

    @Override
    protected void startScan() {
        bridge.startScan();
    }

    /**
     * Discovered a thing! Now let the system know about it.
     *
     * @param address The address of the thing
     * @param productKey The product key of the thing
     */
    public void doThingDiscovered(InsteonAddress address, String productKey) {
        DeviceType deviceType = new DeviceType(productKey);
        Set<String> features = Sets.newHashSet();
        Set<String> groups = Sets.newHashSet();
        for (Entry<String, String> fe : deviceType.getFeatures().entrySet()) {
            if (!bridge.getDeviceFeatureFactory().isDeviceFeature(fe.getValue())) {
                logger.error("device type {} references unknown feature: {}", deviceType, fe.getValue());
            } else {
                features.add(fe.getKey());
            }
        }
        for (Entry<String, FeatureGroup> fe : deviceType.getFeatureGroups().entrySet()) {
            FeatureGroup fg = fe.getValue();
            if (!bridge.getDeviceFeatureFactory().isDeviceFeature(fg.getType())) {
                logger.error("device type {} references unknown feature group: {}", deviceType, fg.getType());
            }
            groups.add(fe.getKey());
        }

        // Make the discovery result up.
        ThingUID thingUID = new ThingUID(InsteonPLMBindingConstants.BINDING_ID, bridge.getThing().getUID(),
                address.toString());
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_ADDRESS, address.toString());
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_FEATURES, features);
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_FEATURE_GROUPS, groups);
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY, productKey);
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(deviceType.getThingType()).withLabel(deviceType.getDefaultLabel(address))
                .withBridge(bridge.getThing().getUID()).withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }
}

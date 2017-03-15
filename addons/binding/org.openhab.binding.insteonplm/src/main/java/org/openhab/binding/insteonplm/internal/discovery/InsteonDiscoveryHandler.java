package org.openhab.binding.insteonplm.internal.discovery;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ThingType;
import org.openhab.binding.insteonplm.InsteonPLMBindingConstants;
import org.openhab.binding.insteonplm.handler.InsteonPLMBridgeHandler;
import org.openhab.binding.insteonplm.internal.config.InsteonConfigProvider;
import org.openhab.binding.insteonplm.internal.config.InsteonProduct;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

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
    public void doThingDiscovered(InsteonAddress address, String productKey, int category, int subCategory) {
        InsteonProduct product = InsteonConfigProvider.getInsteonProduct(category, subCategory);
        if (product == null) {
            logger.error("Unable to find product for {} {}", category, subCategory);
            return;
        }

        // Make the discovery result up.
        ThingUID thingUID = new ThingUID(InsteonPLMBindingConstants.BINDING_ID, bridge.getThing().getUID(),
                address.toString());
        ThingType type = InsteonConfigProvider.getThingType(category, subCategory);
        if (type == null) {
            logger.error("Unable to find thing type for {} {}", category, subCategory);
            return;
        }
        Map<String, Object> properties = Maps.newHashMap();
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_ADDRESS, address.toString());
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_PRODUCT_KEY, productKey);
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_CATEGORY, category);
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_SUBCATEGORY, subCategory);
        properties.put(InsteonPLMBindingConstants.PROPERTY_INSTEON_MODEL, product.getModel());
        String label = "Insteon " + type.getLabel() + " " + address.toString();
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withThingType(product.getThingTypeUID()).withLabel(label).withBridge(bridge.getThing().getUID())
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return InsteonConfigProvider.getSupportedThingTypes();
    }
}

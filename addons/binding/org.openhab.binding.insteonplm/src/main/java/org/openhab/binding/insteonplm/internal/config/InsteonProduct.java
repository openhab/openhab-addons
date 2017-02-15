package org.openhab.binding.insteonplm.internal.config;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Tracks all the details about the insteon product as read from the things config
 * files.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class InsteonProduct {
    private ThingTypeUID thingTypeUID;
    private String productKey;
    private String model;

    public InsteonProduct(ThingTypeUID thingTypeUID, String productKey, String model) {
        this.thingTypeUID = thingTypeUID;
        this.productKey = productKey;
        this.model = model;
    }

    /**
     * See if this product matches the known product key off the thing.
     */
    public boolean match(String productKey) {
        return this.productKey.equals(productKey);
    }

    public ThingTypeUID getThingTypeUID() {
        return this.thingTypeUID;
    }

    @Override
    public String toString() {
        return String.format("Product Key %s Model %s ThingTypeUID %s", this.productKey, this.model,
                this.thingTypeUID.toString());
    }

    public String getModel() {
        return model;
    }
}

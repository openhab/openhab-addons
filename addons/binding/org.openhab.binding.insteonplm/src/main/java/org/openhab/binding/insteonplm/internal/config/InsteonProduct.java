package org.openhab.binding.insteonplm.internal.config;

import java.util.List;

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
    private int category;
    private List<Integer> subCategory;
    private String model;

    public enum MatchResult {
        CompleteMatch,
        CategoryOnly,
        NoMatch
    }

    public InsteonProduct(ThingTypeUID thingTypeUID, String productKey, String model, int category,
            List<Integer> subCategory) {
        this.thingTypeUID = thingTypeUID;
        this.productKey = productKey;
        this.model = model;
        this.category = category;
        this.subCategory = subCategory;
    }

    /**
     * See if this product matches the known product key off the thing.
     */
    public boolean match(String productKey) {
        return this.productKey.equals(productKey);
    }

    /**
     * See if this product matches the known product key off the thing.
     */
    public MatchResult match(int category, int subCategory) {
        if (this.category == category) {
            if (this.subCategory.size() == 0) {
                return MatchResult.CategoryOnly;
            }
            if (this.subCategory.contains(subCategory)) {
                return MatchResult.CompleteMatch;
            }
        }
        return MatchResult.NoMatch;
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

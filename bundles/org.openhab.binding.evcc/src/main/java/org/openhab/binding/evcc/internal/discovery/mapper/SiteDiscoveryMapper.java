package org.openhab.binding.evcc.internal.discovery.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonObject;

public class SiteDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject root, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        if (root == null || !root.has("siteTitle"))
            return results;
        String siteTitle = root.get("siteTitle").getAsString();
        ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_SITE, bridgeHandler.getThing().getUID(), "site");
        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("evcc Site - " + siteTitle)
                .withBridge(bridgeHandler.getThing().getUID()).withProperty("type", "site")
                .withProperty("siteTitle", siteTitle).withRepresentationProperty("siteTitle").build();
        results.add(result);
        return results;
    }
}

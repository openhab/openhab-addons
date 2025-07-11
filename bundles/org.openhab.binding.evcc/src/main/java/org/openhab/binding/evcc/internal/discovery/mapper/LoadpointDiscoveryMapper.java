package org.openhab.binding.evcc.internal.discovery.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class LoadpointDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject root, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonArray loadpoints = root.getAsJsonArray("loadpoints");
        if (loadpoints == null)
            return results;

        for (int i = 0; i < loadpoints.size(); i++) {
            JsonObject lp = loadpoints.get(i).getAsJsonObject();
            String title = lp.has("title") ? lp.get("title").getAsString().toLowerCase(Locale.ROOT) : "loadpoint" + i;

            ThingUID uid = new ThingUID("DUMMY:DUMMY:DUMMY");
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("index", i);

            if (lp.has(title) && lp.get("chargerFeatureHeating").getAsBoolean()) {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_HEATING, bridgeHandler.getThing().getUID(),
                        Utils.sanatizeName(title));
                properties.put("type", "heating");
            } else {
                uid = new ThingUID(EvccBindingConstants.THING_TYPE_LOADPOINT, bridgeHandler.getThing().getUID(),
                        Utils.sanatizeName(title));
                properties.put("type", "loadpoint");
            }

            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("evcc Loadpoint - " + title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperties(properties)
                    .withRepresentationProperty("index").build();

            results.add(result);
        }

        return results;
    }
}

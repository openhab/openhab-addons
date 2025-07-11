package org.openhab.binding.evcc.internal.discovery.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.discovery.Utils;
import org.openhab.binding.evcc.internal.handler.EvccBridgeHandler;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PvDiscoveryMapper implements EvccDiscoveryMapper {

    @Override
    public Collection<DiscoveryResult> discover(JsonObject root, EvccBridgeHandler bridgeHandler) {
        List<DiscoveryResult> results = new ArrayList<>();
        JsonArray pvs = root.getAsJsonArray("pv");
        if (pvs == null)
            return results;

        for (int i = 0; i < pvs.size(); i++) {
            JsonObject pv = pvs.get(i).getAsJsonObject();
            String title = pv.has("title") ? pv.get("title").getAsString().toLowerCase(Locale.ROOT) : "pv" + i;

            ThingUID uid = new ThingUID(EvccBindingConstants.THING_TYPE_PV, bridgeHandler.getThing().getUID(),
                    Utils.sanatizeName(title));
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withLabel("evcc PV - " + title)
                    .withBridge(bridgeHandler.getThing().getUID()).withProperty("index", i).withProperty("type", "pv")
                    .withRepresentationProperty("index").build();

            results.add(result);
        }
        return results;
    }
}

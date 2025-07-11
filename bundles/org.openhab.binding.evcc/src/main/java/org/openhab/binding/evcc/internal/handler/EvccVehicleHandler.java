package org.openhab.binding.evcc.internal.handler;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@NonNullByDefault
public class EvccVehicleHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccVehicleHandler.class);

    @Nullable
    private final String vehicleId;
    private final Gson gson = new Gson();

    private String endpoint = "";

    public EvccVehicleHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        vehicleId = thing.getProperties().get("id");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            HttpClient httpClient = bridgeHandler.getHttpClient();
            String datapoint = channelUID.getId().replace("vehicle", "").toLowerCase();
            String value = command.toString().substring(0, command.toString().indexOf(" "));
            String url = endpoint + "/" + vehicleId + "/" + datapoint + "/" + value;

            try {
                ContentResponse response = httpClient.newRequest(url).timeout(5, TimeUnit.SECONDS)
                        .method(HttpMethod.POST).header(HttpHeader.ACCEPT, "application/json").send();

                if (response.getStatus() == 200) {
                    @Nullable
                    JsonObject return_value = gson.fromJson(response.getContentAsString(), JsonObject.class);
                    if (return_value != null) {
                        // Add logic here!
                    }
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    logger.warn("EVCC API-Fehler: HTTP {}", response.getStatus());
                }

            } catch (Exception e) {
                logger.error("EVCC Bridge konnte API nicht abrufen", e);
            }
        }
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        root = root.getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        super.updateFromEvccState(root);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (bridgeHandler == null) {
            return;
        }
        endpoint = bridgeHandler.getBaseURL() + "/vehicles";
        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get().getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        commonInitialize(state);
    }
}

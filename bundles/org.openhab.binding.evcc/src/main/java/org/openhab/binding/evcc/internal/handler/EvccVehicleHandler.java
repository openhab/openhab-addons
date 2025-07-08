package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.BINDING_ID;

import java.util.Map;
import java.util.Optional;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EvccVehicleHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccSiteHandler.class);

    private EvccBridgeHandler bridgeHandler;
    private Boolean isInitialized;
    private String vehicleId;
    private final ChannelTypeRegistry channelTypeRegistry;

    public EvccVehicleHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
        vehicleId = thing.getProperties().get("id");
    }

    @Override
    public void initialize() {
        bridgeHandler = getBridge().getHandler() instanceof EvccBridgeHandler
                ? (EvccBridgeHandler) getBridge().getHandler()
                : null;
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get().getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        ThingBuilder builder = editThing();

        for (Map.Entry<String, JsonElement> entry : state.entrySet()) {
            String key = getEndpointKey(entry.getKey());
            JsonElement value = entry.getValue();

            // Beispiel: Nur numerische Werte nehmen
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                continue;
            }

            String itemType = guessAcceptedItemType(key); // z. B. "number:power" oder "number:dimensionless"
            // String label = capitalizeWords(key); // z. B. "PV-Leistung"

            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), key)).withType(channelType(key))
                    .withAcceptedItemType(itemType).build();

            if (!thing.getChannels().stream().anyMatch(c -> c.getUID().equals(channel.getUID()))) {
                builder.withChannel(channel);
            }

        }

        updateThing(builder.build());
        updateStatus(ThingStatus.ONLINE);
        isInitialized = true;
        bridgeHandler.register(this);
    }

    private ChannelTypeUID channelType(String key) {
        return new ChannelTypeUID(BINDING_ID, key);
    }

    private String getEndpointKey(String key) {
        return "vehicle" + key.substring(0, 1).toUpperCase() + key.substring(1);
    }

    private String guessAcceptedItemType(String key) {
        key = key.toLowerCase();
        if (key.contains("power"))
            return "Number:Power";
        if (key.contains("soc") || key.contains("percentage"))
            return "Number:Dimensionless";
        if (key.contains("capacity") || key.contains("energy"))
            return "Number:Energy";
        return "Number"; // Fallback
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregister(this);
        }
        isInitialized = false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        return;
        // TODO Auto-generated method stub
        // throw new UnsupportedOperationException("Unimplemented method 'handleCommand'");
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        if (!isInitialized) {
            return;
        }
        root = root.getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = getEndpointKey(entry.getKey());
            JsonElement value = entry.getValue();

            // Skip non-primitive values
            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelUID channelUID = channelUID(key);
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel == null) {
                continue;
            }

            switch (channel.getAcceptedItemType()) {
                case "Number:Energy":
                    updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.KILOWATT_HOUR));
                    break;
                case "Number:Power":
                    updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.WATT));
                    break;
                case "Number:Dimensionless":
                    updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.PERCENT)); // Assuming
                                                                                                     // value is
                                                                                                     // in
                                                                                                     // percent
                    break;
                case "String":
                    updateState(channelUID, new StringType(value.getAsString()));
                    break;
                case "Number":
                    updateState(channelUID, new DecimalType(value.getAsDouble()));
                    break;
                default:
                    logger.warn("Unsupported channel type for channel {}: {}", channelUID,
                            channel.getAcceptedItemType());
                    continue;
            }
        }
    }

    private ChannelUID channelUID(String id) {
        return new ChannelUID(getThing().getUID(), id);
    }
}

package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class EvccSiteHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccSiteHandler.class);

    private EvccBridgeHandler bridgeHandler;
    private Boolean isInitialized = false;
    private final ChannelTypeRegistry channelTypeRegistry;

    public EvccSiteHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
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

        JsonObject state = stateOpt.get();
        ThingBuilder builder = editThing();

        for (Map.Entry<String, JsonElement> entry : state.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            // Skip non-primitive values
            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, key);
            String itemType = getItemType(channelTypeUID);
            if (itemType.equals("Unknown")) {
                logger.debug("Unknown channel type for key '{}'. Skipping channel creation.", key);
                continue;
            }

            Channel channel = ChannelBuilder.create(new ChannelUID(getThing().getUID(), key)).withType(channelTypeUID)
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

    private String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (channelType == null) {
            return "Unknown";
        } else {
            return channelType.getItemType();
        }
    }

    public static String capitalizeWords(String input) {
        return Arrays.stream(input.split("(?=[A-Z])")) // split before uppercase
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)) // capitalize each
                .collect(Collectors.joining(" "));
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo statusInfo) {
        switch (statusInfo.getStatus()) {
            case OFFLINE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                dispose();
                break;
            case UNINITIALIZED:
                updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED);
                dispose();
                break;
            case ONLINE:
                if (!isInitialized) {
                    logger.debug("Bridge {} ist wieder ONLINE – initialisiere EVCC-Site-Handler neu…",
                            getBridge().getUID());
                    initialize(); // explizit neue Initialisierung starten
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            default:
                break;
        }
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
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelUID channelUID = channelUID(key);
            Channel channel = getThing().getChannel(channelUID.getId());
            if (channel == null) {
                continue;
            }

            switch (getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId()))) {
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
                case "Switch":
                    updateState(channelUID, value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF);
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

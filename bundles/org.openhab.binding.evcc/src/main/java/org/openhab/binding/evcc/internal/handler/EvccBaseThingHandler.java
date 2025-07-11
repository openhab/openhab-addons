package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.BINDING_ID;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class EvccBaseThingHandler extends BaseThingHandler implements EvccJsonAwareHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccBaseThingHandler.class);
    private final ChannelTypeRegistry channelTypeRegistry;
    protected EvccBridgeHandler bridgeHandler;
    protected boolean isInitialized = false;

    public EvccBaseThingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing);
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void commonInitialize(JsonObject state) {
        ThingBuilder builder = editThing();

        if (state.has("gridConfigured")) {
            double gridPower = state.getAsJsonObject("grid").get("power").getAsDouble();
            state.addProperty("gridPower", gridPower);
        }

        for (Map.Entry<String, JsonElement> entry : state.entrySet()) {
            String key = getThingKey(entry.getKey());
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
                    // .withAcceptedItemType(itemType)
                    .build();

            ChannelUID channelUID = channel.getUID();

            setItemValue(itemType, channelUID, value);

            if (!getThing().getChannels().stream().anyMatch(c -> c.getUID().equals(channel.getUID()))) {
                builder.withChannel(channel);
            }

        }

        updateThing(builder.build());
        updateStatus(ThingStatus.ONLINE);
        isInitialized = true;
        bridgeHandler.register(this);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge == null)
            return;

        bridgeHandler = bridge.getHandler() instanceof EvccBridgeHandler ? (EvccBridgeHandler) bridge.getHandler()
                : null;
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
    }

    @Override
    public void dispose() {
        if (bridgeHandler != null) {
            bridgeHandler.unregister(this);
        }
        isInitialized = false;
    }

    private String getItemType(ChannelTypeUID channelTypeUID) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channelTypeUID);
        if (channelType == null) {
            return "Unknown";
        } else {
            return channelType.getItemType();
        }
    }

    private boolean setItemValue(String itemType, ChannelUID channelUID, JsonElement value) {
        switch (itemType) {
            case "Number:Energy" ->
                updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.KILOWATT_HOUR));
            case "Number:Power" -> updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.WATT));
            case "Number:Dimensionless" ->
                updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.PERCENT));
            case "Number:ElectricCurrent" ->
                updateState(channelUID, new QuantityType<>(value.getAsDouble(), Units.AMPERE));
            case "String" -> updateState(channelUID, new StringType(value.getAsString()));
            case "Number", "Number:Currency", "Number:Time" ->
                updateState(channelUID, new DecimalType(value.getAsDouble()));
            case "Switch" -> updateState(channelUID, value.getAsBoolean() ? OnOffType.ON : OnOffType.OFF);
            case "default" -> {
                logger.debug("Unsupported item type '{}' for channel {}", itemType, channelUID);
                return false;
            }
        }
        return true;
    }

    public static String capitalizeWords(String input) {
        return Arrays.stream(input.split("(?=[A-Z])")) // split before uppercase
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)) // capitalize each
                .collect(Collectors.joining(" "));
    }

    private String getThingKey(String key) {
        Map<@NonNull String, @NonNull String> props = getThing().getProperties();

        switch (props.get("type")) {
            case "site":
                return key;
            case null:
                logger.warn("Property type was not found for {}", getThing().getUID().getAsString());
                return key;
            default:
                return props.get("type") + key.substring(0, 1).toUpperCase() + key.substring(1);
        }
    }

    @Override
    public void updateFromEvccState(@NonNull JsonObject root) {
        if (!isInitialized) {
            return;
        }
        ThingBuilder builder = editThing();
        boolean channelAdded = false;

        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = getThingKey(entry.getKey());
            JsonElement value = entry.getValue();

            if (!value.isJsonPrimitive()) {
                continue;
            }

            ChannelUID channelUID = channelUID(key);
            Channel existingChannel = getThing().getChannel(channelUID.getId());
            if (existingChannel == null) {

                ChannelTypeUID typeUID = new ChannelTypeUID(BINDING_ID, key);
                Channel newChannel = ChannelBuilder.create(channelUID).withType(typeUID).build();
                builder.withChannel(newChannel);
                channelAdded = true;
                logger.debug("Dynamisch neuen Channel hinzugefügt: {}", key);
            }

            if (!setItemValue(getItemType(new ChannelTypeUID(BINDING_ID, channelUID.getId())), channelUID, value)) {
                continue;
            }
        }

        if (channelAdded) {
            updateThing(builder.build());
        }
    }

    private ChannelUID channelUID(String id) {
        return new ChannelUID(getThing().getUID(), id);
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo statusInfo) {
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
                    Bridge bridge = getBridge();
                    if (bridge == null)
                        break;
                    logger.debug("Bridge {} ist wieder ONLINE – initialisiere EVCC-Site-Handler neu…", bridge.getUID());
                    initialize(); // explizit neue Initialisierung starten
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
                break;
            default:
                break;
        }
    }
}

package org.openhab.binding.restify.internal.servlet;

import static java.util.Arrays.copyOfRange;
import static java.util.Map.entry;
import static java.util.stream.Collectors.toMap;
import static org.openhab.binding.restify.internal.servlet.Json.NullValue.NULL_VALUE;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jspecify.annotations.NonNull;
import org.openhab.binding.restify.internal.servlet.Json.BooleanValue;
import org.openhab.binding.restify.internal.servlet.Json.JsonArray;
import org.openhab.binding.restify.internal.servlet.Json.JsonObject;
import org.openhab.binding.restify.internal.servlet.Json.NumberValue;
import org.openhab.binding.restify.internal.servlet.Json.StringValue;
import org.openhab.binding.restify.internal.servlet.Schema.ItemSchema;
import org.openhab.binding.restify.internal.servlet.Schema.JsonSchema;
import org.openhab.binding.restify.internal.servlet.Schema.StringSchema;
import org.openhab.binding.restify.internal.servlet.Schema.ThingSchema;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.CommandDescription;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
@Component
public class Engine implements Serializable {
    private static final Pattern DATE_FORMATTER_PATTERN = Pattern.compile("\\[(.*?)]");
    @Serial
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.getLogger(Engine.class);
    private final ItemRegistry itemRegistry;
    private final ThingRegistry thingRegistry;

    @Activate
    public Engine(@Reference ItemRegistry itemRegistry, @Reference ThingRegistry thingRegistry) {
        this.itemRegistry = itemRegistry;
        this.thingRegistry = thingRegistry;
    }

    private Json evaluate(Schema Schema) throws ParameterException {
        return switch (Schema) {
            case ItemSchema itemSchema -> evaluate(itemSchema);
            case JsonSchema jsonSchema -> evaluate(jsonSchema);
            case StringSchema stringSchema -> evaluate(stringSchema);
            case ThingSchema thingSchema -> evaluate(thingSchema);
            case Schema.ArraySchema arraySchema -> evaluate(arraySchema);
        };
    }

    private Json evaluate(ItemSchema itemSchema) throws ParameterException {
        var itemName = itemSchema.itemName();
        try {
            var item = itemRegistry.getItem(itemName);
            return evaluateItemExpression(item, itemSchema.expression());
        } catch (ItemNotFoundException e) {
            logger.debug("Item {} not found", itemName);
            return NULL_VALUE;
        }
    }

    private Json evaluateItemExpression(Item item, String expression) throws ParameterException {
        var parts = expression.split("\\.");
        var tail = findTail(parts);
        if (parts.length == 0) {
            return new JsonObject(Map.ofEntries(entry("state", new StringValue(item.getState().toFullString())),
                    entry("lastStateUpdate", evaluateDate(item.getLastStateUpdate(), tail)),
                    entry("lastStateChange", evaluateDate(item.getLastStateChange(), tail)),
                    entry("name", new StringValue(item.getName())), entry("type", new StringValue(item.getType())),
                    entry("acceptedDataTypes",
                            new JsonArray(item.getAcceptedDataTypes().stream().map(Class::getSimpleName)
                                    .map(StringValue::new).toList())),
                    entry("acceptedCommandTypes",
                            new JsonArray(item.getAcceptedCommandTypes().stream().map(Class::getSimpleName)
                                    .map(StringValue::new).toList())),
                    entry("groups", new JsonArray(item.getGroupNames().stream().map(StringValue::new).toList())),
                    entry("tags", new JsonArray(item.getTags().stream().map(StringValue::new).toList())),
                    entry("label", stringOrNull(item.getLabel())), entry("category", stringOrNull(item.getCategory())),
                    entry("stateDescription", evaluateItemExpression(item.getStateDescription(), tail)),
                    entry("commandDescription", evaluateItemExpression(item.getCommandDescription(), tail))));
        }
        return switch (parts[0]) {
            case "state" -> new StringValue(item.getState().toFullString());
            case "lastStateUpdate" -> evaluateDate(item.getLastStateUpdate(), tail);
            case "lastStateChange" -> evaluateDate(item.getLastStateChange(), tail);
            case "name" -> new StringValue(item.getName());
            case "type" -> new StringValue(item.getType());
            case "acceptedDataTypes" -> new JsonArray(
                    item.getAcceptedDataTypes().stream().map(Class::getSimpleName).map(StringValue::new).toList());
            case "acceptedCommandTypes" ->
                new JsonArray(item.getAcceptedCommandTypes().stream().map(Class::getSimpleName).map(StringValue::new)

                        .toList());
            case "groups" -> new JsonArray(item.getGroupNames().stream().map(StringValue::new).toList());
            case "tags" -> new JsonArray(item.getTags().stream().map(StringValue::new).toList());
            case "label" -> stringOrNull(item.getLabel());
            case "category" -> stringOrNull(item.getCategory());
            case "stateDescription" -> evaluateItemExpression(item.getStateDescription(), tail);
            case "commandDescription" -> evaluateItemExpression(item.getCommandDescription(), tail);
            default -> throw new ParameterException(parts[0]);
        };
    }

    private static String @NonNull [] findTail(String[] parts) {
        return copyOfRange(parts, 1, parts.length);
    }

    private Json evaluateDate(@Nullable ZonedDateTime lastStateUpdate, String[] params) {
        if (lastStateUpdate == null) {
            return NULL_VALUE;
        }
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
        if (params.length > 0) {
            var matcher = DATE_FORMATTER_PATTERN.matcher(params[0]);

            if (matcher.find()) {
                var format = matcher.group(1);
                try {
                    formatter = DateTimeFormatter.ofPattern(format);
                } catch (IllegalArgumentException ex) {
                    logger.warn("Invalid date format {} in expression, using default format", format);
                }
            }
        }
        return new StringValue(formatter.format(lastStateUpdate));
    }

    private Json evaluateItemExpression(@Nullable StateDescription stateDescription, String[] params)
            throws ParameterException {
        if (stateDescription == null) {
            return NULL_VALUE;
        }
        if (params.length == 0) {
            // return full StateDescription
            return new JsonObject(Map.of("minimum", numberOrNull(stateDescription.getMinimum()), //
                    "maximum", numberOrNull(stateDescription.getMaximum()), //
                    "step", numberOrNull(stateDescription.getStep()), //
                    "pattern", stringOrNull(stateDescription.getPattern()), //
                    "readOnly", booleanOrNull(stateDescription.isReadOnly()), //
                    "options", evaluateItemOptions(stateDescription.getOptions(), findTail(params))));
        }
        return switch (params[0]) {
            case "minimum" -> numberOrNull(stateDescription.getMinimum());
            case "maximum" -> numberOrNull(stateDescription.getMaximum());
            case "step" -> numberOrNull(stateDescription.getStep());
            case "pattern" -> stringOrNull(stateDescription.getPattern());
            case "readOnly" -> booleanOrNull(stateDescription.isReadOnly());
            case "options" -> evaluateItemOptions(stateDescription.getOptions(), findTail(params));
            default -> throw new ParameterException(params[0]);
        };
    }

    public Json numberOrNull(@Nullable Number number) {
        if (number == null) {
            return NULL_VALUE;
        }
        return new NumberValue(number);
    }

    public Json stringOrNull(@Nullable String string) {
        if (string == null) {
            return NULL_VALUE;
        }
        return new StringValue(string);
    }

    public Json booleanOrNull(@Nullable Boolean bool) {
        if (bool == null) {
            return NULL_VALUE;
        }
        return new BooleanValue(bool);
    }

    private Json evaluateItemOptions(List<StateOption> options, String[] params) throws ParameterException {
        var list = new ArrayList<Json>();
        for (StateOption option : options) {
            Json json = evaluateStateOption(option, params);
            list.add(json);
        }
        return new JsonArray(list);
    }

    private Json evaluateStateOption(StateOption option, String[] params) throws ParameterException {
        if (params.length == 0) {
            return new JsonObject(
                    Map.of("value", new StringValue(option.getValue()), "label", mapNullableString(option.getLabel())));
        }
        return switch (params[0]) {
            case "value" -> new StringValue(option.getValue());
            case "label" -> mapNullableString(option.getLabel());
            default -> throw new ParameterException(params[0]);
        };
    }

    private static @NonNull Json mapNullableString(@Nullable String string) {
        if (string == null) {
            return NULL_VALUE;
        }
        return new StringValue(string);
    }

    private static @NonNull Json mapNullableObjectToString(@Nullable Object object) {
        if (object == null) {
            return NULL_VALUE;
        }
        return new StringValue(object.toString());
    }

    private Json evaluateItemExpression(@Nullable CommandDescription commandDescription, String[] params)
            throws ParameterException {
        if (commandDescription == null) {
            return NULL_VALUE;
        }
        var list = new ArrayList<Json>();
        for (CommandOption cd : commandDescription.getCommandOptions()) {
            Json json = evaluateCommandOption(cd, params);
            list.add(json);
        }
        return new JsonArray(list);
    }

    private Json evaluateCommandOption(CommandOption option, String[] params) throws ParameterException {
        if (params.length == 0) {
            return new JsonObject(Map.of("command", new StringValue(option.getCommand()), "label",
                    mapNullableString(option.getLabel())));
        }
        return switch (params[0]) {
            case "command" -> new StringValue(option.getCommand());
            case "label" -> mapNullableString(option.getLabel());
            default -> throw new ParameterException(params[0]);
        };
    }

    public JsonObject evaluate(JsonSchema schema) throws ParameterException {
        var map = new HashMap<String, Json>();
        for (var pair : schema.values().entrySet()) {
            var entry = entry(pair.getKey(), evaluate(pair.getValue()));
            if (map.put(entry.getKey(), entry.getValue()) != null) {
                throw new IllegalStateException("Duplicate key");
            }
        }
        return new JsonObject(map);
    }

    private Json evaluate(StringSchema schema) {
        return new StringValue(schema.value());
    }

    private Json evaluate(ThingSchema schema) throws ParameterException {
        try {
            ThingUID uid = new ThingUID(schema.thingUid());
            var thing = thingRegistry.get(uid);
            if (thing == null) {
                logger.debug("Thing {} not found", uid);
                return NULL_VALUE;
            }
            return evaluateThingExpression(thing, schema.expression());
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid Thing UID {} in expression", schema.thingUid());
            return NULL_VALUE;
        }
    }

    private Json evaluateThingExpression(Thing thing, String expression) throws ParameterException {
        var params = expression.split("\\.");
        var tail = findTail(params);
        if (params.length == 0) {
            return new JsonObject(Map.ofEntries(entry("label", mapNullableString(thing.getLabel())),
                    entry("channels", evaluateChannels(thing.getChannels(), tail)),
                    entry("channel", evaluateChannel(findChannel(thing, tail), findTail(tail))),
                    entry("status", new StringValue(thing.getStatus().toString())),
                    entry("statusInfo", evaluateStatusInfo(thing.getStatusInfo(), tail)),
                    entry("configuration", evaluateConfiguration(thing.getConfiguration())),
                    entry("uid", new StringValue(thing.getUID().toString())),
                    entry("thingTypeUid", new StringValue(thing.getThingTypeUID().toString())),
                    entry("properties", evaluateProperties(thing.getProperties())),
                    entry("location", mapNullableString(thing.getLocation())),
                    entry("enabled", new BooleanValue(thing.isEnabled())),
                    entry("semanticEquipmentTag", mapNullableString(thing.getSemanticEquipmentTag()))));

        }
        return switch (params[0]) {
            case "label" -> mapNullableString(thing.getLabel());
            case "channels" -> evaluateChannels(thing.getChannels(), tail);
            case "channel" -> evaluateChannel(findChannel(thing, tail), findTail(tail));
            case "status" -> new StringValue(thing.getStatus().toString());
            case "statusInfo" -> evaluateStatusInfo(thing.getStatusInfo(), tail);
            case "configuration" -> evaluateConfiguration(thing.getConfiguration());
            case "uid" -> new StringValue(thing.getUID().toString());
            case "thingTypeUid" -> new StringValue(thing.getThingTypeUID().toString());
            case "properties" -> evaluateProperties(thing.getProperties());
            case "location" -> mapNullableString(thing.getLocation());
            case "enabled" -> new BooleanValue(thing.isEnabled());
            case "semanticEquipmentTag" -> mapNullableString(thing.getSemanticEquipmentTag());
            default -> throw new ParameterException(params[0]);
        };
    }

    private static @Nullable Channel findChannel(Thing thing, String[] params) {
        if (params.length == 0) {
            return null;
        }
        return thing.getChannel(params[0]);
    }

    private Json evaluateChannels(List<Channel> channels, String[] params) throws ParameterException {
        var list = new ArrayList<Json>();
        for (Channel channel : channels) {
            Json json = evaluateChannel(channel, params);
            list.add(json);
        }
        return new JsonArray(list);
    }

    private Json evaluateChannel(@Nullable Channel channel, String[] params) throws ParameterException {
        if (channel == null) {
            return NULL_VALUE;
        }
        if (params.length == 0) {
            return new JsonObject(Map.ofEntries(

                    entry("acceptedItemType", mapNullableString(channel.getAcceptedItemType())),
                    entry("kind", new StringValue(channel.getKind().toString())),
                    entry("uid", new StringValue(channel.getUID().toString())),
                    entry("channelTypeUID", mapNullableObjectToString(channel.getChannelTypeUID())),
                    entry("label", mapNullableString(channel.getLabel())),
                    entry("description", mapNullableString(channel.getDescription())),
                    entry("configuration", evaluateConfiguration(channel.getConfiguration())),
                    entry("properties", evaluateProperties(channel.getProperties())),
                    entry("defaultTags",
                            new JsonArray(channel.getDefaultTags().stream().map(StringValue::new).toList())),
                    entry("autoUpdatePolicy", mapNullableObjectToString(channel.getAutoUpdatePolicy()))));
        }

        return switch (params[0]) {
            case "acceptedItemType" -> mapNullableString(channel.getAcceptedItemType());
            case "kind" -> new StringValue(channel.getKind().toString());
            case "uid" -> new StringValue(channel.getUID().toString());
            case "channelTypeUID" -> mapNullableObjectToString(channel.getChannelTypeUID());
            case "label" -> mapNullableString(channel.getLabel());
            case "description" -> mapNullableString(channel.getDescription());
            case "configuration" -> evaluateConfiguration(channel.getConfiguration());
            case "properties" -> evaluateProperties(channel.getProperties());
            case "defaultTags" -> new JsonArray(channel.getDefaultTags().stream().map(StringValue::new).toList());
            case "autoUpdatePolicy" -> mapNullableObjectToString(channel.getAutoUpdatePolicy());
            default -> throw new ParameterException(params[0]);
        };
    }

    private Json evaluateStatusInfo(ThingStatusInfo statusInfo, String[] params) throws ParameterException {
        if (params.length == 0) {
            return new JsonObject(Map.ofEntries(entry("status", new StringValue(statusInfo.getStatus().toString())),
                    entry("statusDetail", new StringValue(statusInfo.getStatusDetail().toString())),
                    entry("description", mapNullableString(statusInfo.getDescription()))));
        }

        return switch (params[0]) {
            case "status" -> new StringValue(statusInfo.getStatus().toString());
            case "statusDetail" -> new StringValue(statusInfo.getStatusDetail().toString());
            case "description" -> mapNullableString(statusInfo.getDescription());
            default -> throw new ParameterException(params[0]);
        };
    }

    private Json evaluateConfiguration(Configuration configuration) {
        var map = configuration.getProperties().entrySet().stream()
                .map(entry -> entry(entry.getKey(), new StringValue(entry.getValue().toString())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new JsonObject(map);
    }

    private Json evaluateProperties(Map<String, String> properties) {
        var map = properties.entrySet().stream().map(entry -> entry(entry.getKey(), new StringValue(entry.getValue())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new JsonObject(map);
    }

    private JsonArray evaluate(Schema.ArraySchema arraySchema) throws ParameterException {
        var values = new ArrayList<Json>();
        for (Schema schema : arraySchema.values()) {
            Json evaluate = evaluate(schema);
            values.add(evaluate);
        }
        return new JsonArray(values);
    }
}

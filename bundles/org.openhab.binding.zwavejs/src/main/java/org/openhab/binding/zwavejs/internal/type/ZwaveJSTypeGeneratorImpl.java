/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zwavejs.internal.type;

import static org.openhab.binding.zwavejs.internal.BindingConstants.CONFIGURATION_COMMAND_CLASSES;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zwavejs.internal.BindingConstants;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.Value;
import org.openhab.binding.zwavejs.internal.conversion.ChannelMetadata;
import org.openhab.binding.zwavejs.internal.conversion.ConfigMetadata;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates openHAB entities (Channel, ChannelType and ConfigDescription) based on the Z-Wave JS data.
 *
 * @see ChannelType
 * @see ChannelTypeBuilder
 * @see ChannelTypeUID
 * @see StateChannelTypeBuilder
 * @see ThingRegistry
 * @see ZwaveJSChannelTypeProvider
 * @see ZwaveJSConfigDescriptionProvider
 *
 * @author Leo Siepel - Initial contribution
 */
@Component
@NonNullByDefault
public class ZwaveJSTypeGeneratorImpl implements ZwaveJSTypeGenerator {

    private static final Object CHANNEL_TYPE_VERSION = "4"; // when static configuration is changed, the version must be
                                                            // changed as well to force a new channel type generation
    private static final Map<String, SemanticTag> ITEM_TYPES_TO_PROPERTY_TAGS = new HashMap<>();
    static {
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:ElectricCurrent", Property.CURRENT);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:ElectricPotential", Property.VOLTAGE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Energy", Property.ENERGY);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Frequency", Property.FREQUENCY);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Illuminance", Property.ILLUMINANCE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Power", Property.POWER);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Pressure", Property.PRESSURE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Speed", Property.SPEED);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Temperature", Property.TEMPERATURE);
        ITEM_TYPES_TO_PROPERTY_TAGS.put("Number:Time", Property.DURATION);
    }

    private final Logger logger = LoggerFactory.getLogger(ZwaveJSTypeGeneratorImpl.class);
    private final ThingRegistry thingRegistry;
    private final ZwaveJSChannelTypeProvider channelTypeProvider;
    private final ZwaveJSConfigDescriptionProvider configDescriptionProvider;

    @Activate
    public ZwaveJSTypeGeneratorImpl(@Reference ZwaveJSChannelTypeProvider channelTypeProvider,
            @Reference ZwaveJSConfigDescriptionProvider configDescriptionProvider,
            @Reference ThingRegistry thingRegistry) {
        this.channelTypeProvider = channelTypeProvider;
        this.configDescriptionProvider = configDescriptionProvider;
        this.thingRegistry = thingRegistry;
    }

    /*
     * Retrieves a Thing by its UID.
     *
     * @param thingUID the UID of the Thing
     * 
     * @return the Thing, or null if not found
     */
    public @Nullable Thing getThing(ThingUID thingUID) {
        return thingRegistry.get(thingUID);
    }

    /*
     * Generates a ZwaveJSTypeGeneratorResult for the given ThingUID and Node.
     *
     * @param thingUID the ThingUID of the device
     * 
     * @param node the Node containing the values to be processed
     * 
     * @param configurationAsChannels flag indicating whether to treat configuration as channels
     * 
     * @return a ZwaveJSTypeGeneratorResult containing the generated channels and configuration descriptions
     */
    @Override
    public ZwaveJSTypeGeneratorResult generate(ThingUID thingUID, Node node, boolean configurationAsChannels) {
        ZwaveJSTypeGeneratorResult result = new ZwaveJSTypeGeneratorResult();
        List<ConfigDescriptionParameter> configDescriptions = new ArrayList<>();
        URI uri = Objects.requireNonNull(getConfigDescriptionURI(thingUID, node));
        for (Value value : node.values) {
            if (!configurationAsChannels && CONFIGURATION_COMMAND_CLASSES.contains(value.commandClassName)) {
                ConfigMetadata metadata = new ConfigMetadata(node.nodeId, value);
                configDescriptions.add(createConfigDescription(metadata));
                if (!result.values.containsKey(metadata.id) && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
            ChannelMetadata metadata = new ChannelMetadata(node.nodeId, value);
            if (configurationAsChannels || !CONFIGURATION_COMMAND_CLASSES.contains(value.commandClassName)) {
                result.channels = createChannel(thingUID, result.channels, metadata, configDescriptionProvider);
                if (!metadata.isIgnoredCommandClass(value.commandClassName) && !result.values.containsKey(metadata.id)
                        && value.value != null) {
                    result.values.put(metadata.id, value.value);
                }
            }
        }
        logger.debug("Node {}. Generated {} channels and {} configDescriptions with URI {}", node.nodeId,
                result.channels.size(), configDescriptions.size(), uri);

        configDescriptionProvider
                .addConfigDescription(ConfigDescriptionBuilder.create(uri).withParameters(configDescriptions).build());

        return result;
    }

    private ConfigDescriptionParameter createConfigDescription(ConfigMetadata details) {
        logger.trace("Node {}. createConfigDescriptions with Id: {}", details.nodeId, details.id);

        ConfigDescriptionParameterBuilder parameterBuilder = ConfigDescriptionParameterBuilder
                .create(details.id, details.configType) //
                .withRequired(details.state != null) //
                .withLabel(details.label) //
                .withVerify(details.state != null) //
                .withUnit(null) //
                .withDescription(details.description);

        if (details.unitSymbol != null) {
            parameterBuilder.withUnit(details.unitSymbol);
        }
        Map<String, String> optionList = details.optionList;
        if (optionList != null) {
            List<ParameterOption> options = new ArrayList<>();
            optionList.forEach((k, v) -> options.add(new ParameterOption(k, v)));
            parameterBuilder.withLimitToOptions(true);
            parameterBuilder.withMultiple(false);
            parameterBuilder.withContext("item");
            parameterBuilder.withOptions(options);
        }

        return parameterBuilder.build();
    }

    private Map<String, Channel> createChannel(ThingUID thingUID, Map<String, Channel> channels,
            ChannelMetadata details, ZwaveJSConfigDescriptionProvider configDescriptionProvider) {
        if (details.isIgnoredCommandClass(details.commandClassName)) {
            return channels;
        }
        logger.trace("Node {} building channel with Id: {}", details.nodeId, details.id);
        logger.trace(" >> {}", details);

        ChannelUID channelUID = new ChannelUID(thingUID, details.id);

        Configuration newChannelConfiguration = new Configuration();
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_INCOMING_UNIT, details.unitSymbol);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_ITEM_TYPE, details.itemType);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_ID, details.commandClassId);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_COMMANDCLASS_NAME, details.commandClassName);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_ENDPOINT, details.endpoint);
        if (details.propertyKey instanceof Number propertyInteger) {
            newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_PROPERTY_KEY_INT, propertyInteger);
        } else if (details.propertyKey instanceof String propertyString) {
            newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_PROPERTY_KEY_STR, propertyString);
        }
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_FACTOR, details.factor);
        newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_INVERTED, false);

        if (details.writable) {
            newChannelConfiguration.put(BindingConstants.CONFIG_CHANNEL_WRITE_PROPERTY,
                    String.valueOf(details.writeProperty));
        }

        @Nullable
        Channel existingChannel = channels.get(channelUID.getId());
        if (existingChannel != null) {
            Configuration existingChannelConfiguration = existingChannel.getConfiguration();
            if (ChannelMetadata.isSameReadWriteChannel(existingChannelConfiguration, newChannelConfiguration)
                    && details.writable) {
                ChannelTypeUID newChannelTypeUID = generateChannelTypeUID(details);
                ChannelBuilder builder = ChannelBuilder.create(existingChannel)
                        .withConfiguration(newChannelConfiguration);

                if (!newChannelTypeUID.equals(existingChannel.getChannelTypeUID())) {
                    ChannelType newChannelType = getOrGenerate(newChannelTypeUID, details);
                    if (newChannelType != null) {
                        builder.withAcceptedItemType(newChannelType.getItemType()).withType(newChannelType.getUID());
                    }
                }

                channels.put(details.id, builder.build());

                logger.debug("Node {}. Channel {} existing channel updated", details.nodeId, details.id);
                return channels;
            } else {
                logger.debug("Node {}. Channel {} exists: ignored", details.nodeId, details.id);
                return channels;
            }
        }

        ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
        ChannelType channelType = getOrGenerate(channelTypeUID, details);
        if (channelType == null) {
            return channels;
        }

        ChannelBuilder builder = ChannelBuilder.create(channelUID, details.itemType).withLabel(details.label)
                .withConfiguration(newChannelConfiguration).withType(channelType.getUID());

        channels.put(details.id, builder.build());
        return channels;
    }

    private @Nullable ChannelType getOrGenerate(ChannelTypeUID channelTypeUID, ChannelMetadata details) {
        ChannelType channelType = channelTypeProvider.getChannelType(channelTypeUID, null);
        if (channelType == null) {
            channelType = generateChannelType(details);
            if (channelType != null) {
                channelTypeProvider.addChannelType(channelType);
            }
        }
        if (channelType == null) {
            logger.warn("Node {} Channel {}, ChannelType could not be found or generated, please report, this is a bug",
                    details.nodeId, details.id);
            return null;
        }
        return channelType;
    }

    private ChannelTypeUID generateChannelTypeUID(ChannelMetadata details) {
        StringBuilder parts = new StringBuilder();
        parts.append(CHANNEL_TYPE_VERSION);
        parts.append(details.itemType);
        parts.append(details.unitSymbol);
        parts.append(details.writable);
        parts.append(details.isAdvanced);
        StateDescriptionFragment statePattern = details.statePattern;
        if (statePattern != null) {
            parts.append(statePattern.hashCode());
        }

        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(parts.toString().getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return new ChannelTypeUID(BindingConstants.BINDING_ID, stringBuilder.toString());
        } catch (NoSuchAlgorithmException e) {
            logger.warn("NoSuchAlgorithmException error when calculating MD5 hash");
        }
        return new ChannelTypeUID(BindingConstants.BINDING_ID, "unknown");
    }

    private @Nullable ChannelType generateChannelType(ChannelMetadata details) {
        final ChannelTypeUID channelTypeUID = generateChannelTypeUID(details);
        return generateChannelType(channelTypeUID, details);
    }

    private ChannelType generateChannelType(ChannelTypeUID channelTypeUID, ChannelMetadata details) {
        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, details.label, details.itemType);

        if (details.description != null) {
            builder.withDescription(Objects.requireNonNull(details.description));
        }

        if (details.statePattern != null) {
            builder.withStateDescriptionFragment(details.statePattern);
        }

        if (details.unitSymbol != null) {
            builder.withUnitHint(details.unitSymbol);
        }

        if (details.isInvertible()) {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:invertible-channel"));
        } else {
            builder.withConfigDescriptionURI(URI.create("channel-type:zwavejs:base-channel"));
        }
        builder = setSemanticTags(builder, details);
        return builder.isAdvanced(details.isAdvanced).build();
    }

    private @Nullable URI getConfigDescriptionURI(ThingUID thingUID, Node node) {
        Thing thing = getThing(thingUID);
        if (thing == null) {
            logger.debug("Thing '{}'' not found in registry for getConfigDescriptionURI", thingUID);
            return null;
        }
        ThingUID bridgeUID = thing.getBridgeUID();
        if (bridgeUID == null) {
            logger.debug("No bridgeUID found for Thing '{}'' in getConfigDescriptionURI", thingUID);
            return null;
        }

        try {
            return new URI(String.format("thing:%s:node:%s:node%s", BindingConstants.BINDING_ID, bridgeUID.getId(),
                    node.nodeId));
        } catch (URISyntaxException ex) {
            logger.warn("Can't create configDescriptionURI for node {}", node.nodeId);
            return null;
        }
    }

    private static boolean match(List<String> keyWords, ChannelMetadata details) {
        List<String> sourceTexts = details.description instanceof String description
                ? List.of(details.label, description)
                : List.of(details.label);
        for (String keyWord : keyWords) {
            String keyWordLowercase = keyWord.toLowerCase();
            for (String sourceText : sourceTexts) {
                String sourceTextLowercase = sourceText.toLowerCase();
                if (sourceTextLowercase.contains(keyWordLowercase)) {
                    return true;
                }
                if (sourceTextLowercase.endsWith(keyWordLowercase.stripTrailing())) {
                    return true;
                }
                if (sourceTextLowercase.startsWith(keyWordLowercase.stripLeading())) {
                    return true;
                }
            }
        }
        return false;
    }

    private StateChannelTypeBuilder setSemanticTags(StateChannelTypeBuilder builder, ChannelMetadata details) {
        SemanticTag point = null;
        SemanticTag property = null;
        switch (details.itemType) {
            case CoreItemFactory.COLOR:
                point = details.writable ? Point.CONTROL : Point.MEASUREMENT;
                property = Property.COLOR;
            case CoreItemFactory.DIMMER:
                point = Point.CONTROL;
                break;
            case CoreItemFactory.STRING:
                point = details.writable ? Point.CONTROL : Point.STATUS;
                break;
            case CoreItemFactory.SWITCH:
                point = details.writable ? Point.SWITCH
                        : match(List.of("alarm", "error", "warning", "fault"), details) ? Point.ALARM : Point.STATUS;
                break;
            case CoreItemFactory.CONTACT:
                point = match(List.of("alarm", "error", "warning", "fault"), details) ? Point.ALARM : Point.STATUS;
                property = Property.OPEN_STATE;
                break;
            case CoreItemFactory.DATETIME:
                point = details.writable ? Point.CONTROL : Point.MEASUREMENT;
                property = Property.TIMESTAMP;
                break;
            case CoreItemFactory.LOCATION:
                point = Point.MEASUREMENT;
                property = Property.GEO_LOCATION;
                break;
            case CoreItemFactory.PLAYER:
                point = Point.CONTROL;
                property = Property.MEDIA_CONTROL;
                break;
            case CoreItemFactory.ROLLERSHUTTER:
                point = Point.CONTROL;
                property = Property.OPEN_LEVEL;
                break;
            case CoreItemFactory.NUMBER:
                point = details.writable ? Point.CONTROL : Point.MEASUREMENT;
                break;
            default:
                // this handles the case of Number:Dimension
                if (details.itemType.startsWith(CoreItemFactory.NUMBER)) {
                    point = details.writable ? Point.SETPOINT : Point.MEASUREMENT;
                    property = ITEM_TYPES_TO_PROPERTY_TAGS.get(details.itemType);
                }
                break;
        }

        // estimate property tag if missing
        if (point != null && property == null) {
            if (Point.SWITCH == point && match(List.of("mode ", "auto", "manual", "enable", "disable", "lock", " day ",
                    "night", "standby", "lock", "away"), details)) {
                property = Property.MODE;
            } else if (Point.STATUS == point && match(List.of("mode ", "auto", "manual", "enable", "disable", "lock",
                    " day ", "night", "standby", "lock", "away"), details)) {
                property = Property.MODE;
            } else if (match(List.of("air"), details) && match(List.of("flow"), details)) {
                property = Property.AIRFLOW;
            } else if (match(List.of("air"), details) && match(List.of("quality"), details)) {
                property = Property.AIR_QUALITY;
            } else if (match(List.of(" nox ", "no2", "no₂"), details)) {
                property = Property.AIR_QUALITY;
            } else if (match(List.of("aqi"), details)) {
                property = Property.AQI;
            } else if (match(List.of("bright", "dimm"), details)) {
                property = Property.BRIGHTNESS;
            } else if (match(List.of(" co ", "(co)", "monoxide"), details)) {
                property = Property.CO;
            } else if (match(List.of("co2", "co₂", "dioxide"), details)) {
                property = Property.CO2;
            } else if (match(List.of("color", "colour"), details) && match(List.of("temp"), details)) {
                property = Property.COLOR_TEMPERATURE;
            } else if (match(List.of("color", "colour"), details)) {
                property = Property.COLOR;
            } else if (match(List.of("kelvin", "mirek", "mired"), details)) {
                property = Property.COLOR_TEMPERATURE;
            } else if (match(List.of("duration"), details)) {
                property = Property.DURATION;
            } else if (match(List.of("energy"), details)) {
                property = Property.ENERGY;
            } else if (match(List.of("fan"), details)) {
                property = Property.SPEED;
            } else if (match(List.of("freq"), details)) {
                property = Property.FREQUENCY;
            } else if (match(List.of("gas"), details)) {
                property = Property.GAS;
            } else if (match(List.of("location"), details)) {
                property = Property.GEO_LOCATION;
            } else if (match(List.of("heat"), details)) {
                property = Property.HEATING;
            } else if (match(List.of("conditioning", "cooling", " ac "), details)) {
                property = Property.AIRCONDITIONING;
            } else if (match(List.of("humidity"), details) && !match(List.of("soil"), details)) {
                property = Property.HUMIDITY;
            } else if (match(List.of("light"), details) && match(List.of("level"), details)) {
                property = Property.ILLUMINANCE;
            } else if (match(List.of("illuminance", " lux "), details)) {
                property = Property.ILLUMINANCE;
            } else if (match(List.of("battery"), details) && match(List.of("low", "alarm"), details)) {
                property = Property.LOW_BATTERY;
            } else if (Point.ALARM == point && match(List.of("battery"), details)) {
                property = Property.LOW_BATTERY;
            } else if (match(List.of("battery"), details)) {
                property = Property.ENERGY;
            } else if (match(List.of("media", "player", "television", " tv ", "receiver"), details)) {
                property = Property.INFO;
            } else if (match(List.of("soil"), details) && match(List.of("humidity"), details)) {
                property = Property.MOISTURE;
            } else if (match(List.of("moisture"), details)) {
                property = Property.MOISTURE;
            } else if (match(List.of("motion", " pir "), details)) {
                property = Property.MOTION;
            } else if (match(List.of("noise"), details)) {
                property = Property.NOISE;
            } else if (match(List.of(" oil "), details)) {
                property = Property.OIL;
            } else if (match(List.of("open", "close", " shut "), details)
                    && match(List.of("state", "status"), details)) {
                property = Property.OPEN_LEVEL;
            } else if (match(List.of("open", "close", " shut "), details)
                    && match(List.of("level", "position"), details)) {
                property = Property.OPEN_LEVEL;
            } else if (match(List.of("open", "close", " shut "), details)) {
                property = Property.OPENING;
            } else if (match(List.of("ozone", "o3", "o₃"), details)) {
                property = Property.OZONE;
            } else if (match(List.of("partic", " pm ", "dust"), details)) {
                property = Property.PARTICULATE_MATTER;
            } else if (match(List.of("pollen"), details)) {
                property = Property.POLLEN;
            } else if (match(List.of("position"), details)) {
                property = Property.POSITION;
            } else if (match(List.of("wall"), details) && !match(List.of("socket", "outlet", "plug "), details)) {
                property = Property.LIGHT;
            } else if (match(List.of("light", "luminaire", "lamp", "bulb", " led "), details)) {
                property = Property.LIGHT;
            } else if (match(List.of("power", "on-off", " off ", "on/off", "relay", "outlet", "plug "), details)) {
                property = Property.POWER;
            } else if (match(List.of("presence", " occup"), details)) {
                property = Property.PRESENCE;
            } else if (match(List.of("pressure"), details)) {
                property = Property.PRESSURE;
            } else if (match(List.of("quality "), details)) {
                property = Property.QUALITY_OF_SERVICE;
            } else if (match(List.of("radon"), details)) {
                property = Property.RADON;
            } else if (match(List.of(" rain", "precip"), details)) {
                property = Property.RAIN;
            } else if (match(List.of("rssi"), details)) {
                property = Property.RSSI;
            } else if (match(List.of("signal"), details) && match(List.of("strength"), details)) {
                property = Property.SIGNAL_STRENGTH;
            } else if (match(List.of(" rf ", " dbw "), details)) {
                property = Property.SIGNAL_STRENGTH;
            } else if (match(List.of("smoke", "fire"), details)) {
                property = Property.SMOKE;
            } else if (match(List.of("sound", "audio", "noise", "decibel", " db ", "mute", " dba "), details)) {
                property = Property.SOUND_VOLUME;
            } else if (match(List.of("speed", "velocity"), details)) {
                property = Property.SPEED;
            } else if (match(List.of("tamper"), details)) {
                property = Property.TAMPERED;
            } else if (match(List.of("tilt", "vane", "slat"), details)) {
                property = Property.TILT;
            } else if (match(List.of("stamp "), details)) {
                property = Property.TIMESTAMP;
            } else if (match(List.of("violet", " uv "), details)) {
                property = Property.ULTRAVIOLET;
            } else if (match(List.of("ventilat", " fan "), details)) {
                property = Property.VENTILATION;
            } else if (match(List.of("vibration"), details)) {
                property = Property.VIBRATION;
            } else if (match(List.of("volatile", " voc "), details)) {
                property = Property.VOC;
            } else if (match(List.of("water", "leak", "flood"), details)) {
                property = Property.WATER;
            } else if (match(List.of("wind"), details)) {
                property = Property.WIND;
            } else if (match(List.of("level"), details)) {
                property = Property.LEVEL;
            } else if (Point.CONTROL == point && match(List.of("application", " app "), details)) {
                property = Property.APP;
            } else if (Point.CONTROL == point && match(List.of("channel"), details)) {
                property = Property.CHANNEL;
            }
        }

        if (point != null) {
            if (property == null) {
                builder.withTags(point);
            } else {
                builder.withTags(point, property);
            }
        }
        return builder;
    }
}

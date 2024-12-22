/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.myuplink.internal.provider;

import static org.openhab.binding.myuplink.internal.MyUplinkBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myuplink.internal.Utils;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.util.UnitUtils;
import org.openhab.core.util.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Factory that contains logic to create dynamic channels.
 *
 * @author Alexander Friese - initial contribution
 */
@Component(service = { ChannelFactory.class })
@NonNullByDefault
public class ChannelFactory {
    private final Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

    private final MyUplinkChannelTypeProvider channelTypeProvider;
    private final ChannelTypeRegistry channelTypeRegistry;

    @Activate
    public ChannelFactory(@Reference MyUplinkChannelTypeProvider channelTypeProvider,
            @Reference ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeProvider = channelTypeProvider;
        this.channelTypeRegistry = channelTypeRegistry;
    }

    public Channel createChannel(ThingUID thingUID, JsonObject channelData) {
        final var channelId = Utils.getAsString(channelData, JSON_KEY_CHANNEL_ID, GENERIC_NO_VAL);
        final var label = Utils.getAsString(channelData, JSON_KEY_CHANNEL_LABEL, GENERIC_NO_VAL);
        final var unit = Utils.fixUnit(Utils.getAsString(channelData, JSON_KEY_CHANNEL_UNIT, ""));
        final var strVal = Utils.getAsString(channelData, JSON_KEY_CHANNEL_STR_VAL, GENERIC_NO_VAL);
        final var writable = Utils.getAsBool(channelData, JSON_KEY_CHANNEL_WRITABLE, Boolean.FALSE);
        final var enumValues = Utils.getAsJsonArray(channelData, JSON_KEY_CHANNEL_ENUM_VALUES);
        final var minValue = Utils.getAsBigDecimal(channelData, JSON_KEY_CHANNEL_MIN);
        final var maxValue = Utils.getAsBigDecimal(channelData, JSON_KEY_CHANNEL_MAX);
        final var stepValue = Utils.getAsBigDecimal(channelData, JSON_KEY_CHANNEL_STEP);

        ChannelTypeUID channelTypeUID = null;
        if (enumValues.isEmpty()) {
            if (!writable) {
                channelTypeUID = determineStaticChannelTypeUID(unit, strVal.contains(JSON_VAL_DECIMAL_SEPARATOR));
            } else {
                channelTypeUID = getOrBuildNumberChannelType(channelId, unit, minValue, maxValue, stepValue);
            }
        } else {
            channelTypeUID = determineEnumChannelTypeUID(channelId, enumValues, writable);
        }

        final var channelUID = new ChannelUID(thingUID, channelId);
        final var acceptedType = determineAcceptedType(channelTypeUID, unit);
        final var builder = ChannelBuilder.create(channelUID).withLabel(label).withDescription(label)
                .withType(channelTypeUID).withAcceptedItemType(acceptedType);

        if (writable) {
            var props = new HashMap<String, String>();
            props.put(PARAMETER_NAME_VALIDATION_REGEXP, DEFAULT_VALIDATION_EXPRESSION);
            builder.withProperties(props);
        }

        return builder.build();
    }

    String determineAcceptedType(ChannelTypeUID channelTypeUID, String unit) {
        if (channelTypeUID.getId().equals(CHANNEL_TYPE_RW_SWITCH)) {
            return CoreItemFactory.SWITCH;
        } else if (unit.isEmpty()) {
            return CoreItemFactory.NUMBER;
        } else {
            Unit<?> parsedUnit = UnitUtils.parseUnit(unit);
            String dimension = parsedUnit == null ? null : UnitUtils.getDimensionName(parsedUnit);

            if (dimension == null || dimension.isEmpty()) {
                logger.warn("Could not parse unit: '{}'", unit);
                return CoreItemFactory.NUMBER;
            } else {
                return CoreItemFactory.NUMBER + ":" + dimension;
            }
        }
    }

    private ChannelTypeUID determineStaticChannelTypeUID(String unit, boolean isDouble) {
        String typeName = switch (unit) {
            case CHANNEL_TYPE_ENERGY_UNIT -> CHANNEL_TYPE_ENERGY;
            case CHANNEL_TYPE_PRESSURE_UNIT -> CHANNEL_TYPE_PRESSURE;
            case CHANNEL_TYPE_PERCENT_UNIT -> CHANNEL_TYPE_PERCENT;
            case CHANNEL_TYPE_TEMPERATURE_UNIT -> CHANNEL_TYPE_TEMPERATURE;
            case CHANNEL_TYPE_FREQUENCY_UNIT -> CHANNEL_TYPE_FREQUENCY;
            case CHANNEL_TYPE_FLOW_UNIT -> CHANNEL_TYPE_FLOW;
            case CHANNEL_TYPE_ELECTRIC_CURRENT_UNIT -> CHANNEL_TYPE_ELECTRIC_CURRENT;
            case CHANNEL_TYPE_TIME_UNIT -> CHANNEL_TYPE_TIME;
            default -> isDouble ? CHANNEL_TYPE_DOUBLE : CHANNEL_TYPE_INTEGER;
        };
        return new ChannelTypeUID(BINDING_ID, typeName);
    }

    private ChannelTypeUID determineEnumChannelTypeUID(String channelId, JsonArray enumValues, boolean writable) {
        var channelTypeUID = determineStaticEnumType(enumValues, writable);
        if (channelTypeUID == null) {
            channelTypeUID = getOrBuildDynamicEnumType(channelId, enumValues, writable);
        }
        return channelTypeUID;
    }

    private ChannelTypeUID getOrBuildDynamicEnumType(String channelId, JsonArray enumValues, boolean writable) {
        final var prefix = writable ? CHANNEL_TYPE_PREFIX_RW + CHANNEL_TYPE_ENUM_PRFIX : CHANNEL_TYPE_ENUM_PRFIX;
        final var channelTypeUID = new ChannelTypeUID(BINDING_ID, prefix + channelId);
        var type = channelTypeRegistry.getChannelType(channelTypeUID);

        if (type == null) {
            var stateBuilder = StateDescriptionFragmentBuilder.create();
            stateBuilder.withReadOnly(!writable).withOptions(extractEnumValues(enumValues));

            var typeBuilder = ChannelTypeBuilder.state(channelTypeUID, channelId, CoreItemFactory.NUMBER)
                    .withStateDescriptionFragment(stateBuilder.build());

            type = typeBuilder.build();
            channelTypeProvider.putChannelType(type);
        }

        return channelTypeUID;
    }

    private ChannelTypeUID getOrBuildNumberChannelType(String channelId, String unit, @Nullable BigDecimal min,
            @Nullable BigDecimal max, @Nullable BigDecimal step) {
        final var channelTypeUID = new ChannelTypeUID(BINDING_ID,
                CHANNEL_TYPE_PREFIX_RW + CHANNEL_TYPE_NUMERIC_PRFIX + channelId);
        var type = channelTypeRegistry.getChannelType(channelTypeUID);

        if (type == null) {
            var stateBuilder = StateDescriptionFragmentBuilder.create().withReadOnly(false);

            if (min != null) {
                stateBuilder.withMinimum(min);
            }
            if (max != null) {
                stateBuilder.withMaximum(max);
            }
            if (step != null) {
                stateBuilder.withStep(step);
            }

            var itemType = determineAcceptedType(channelTypeUID, unit);
            var typeBuilder = ChannelTypeBuilder.state(channelTypeUID, channelId, itemType)
                    .withStateDescriptionFragment(stateBuilder.build());
            if (!itemType.equals(CoreItemFactory.NUMBER)) {
                typeBuilder.withUnitHint(unit);
            }

            channelTypeProvider.putChannelType(typeBuilder.build());
        }

        return channelTypeUID;
    }

    List<StateOption> extractEnumValues(JsonArray enumValues) {
        List<StateOption> list = new ArrayList<>();
        for (var element : enumValues) {
            var enumText = Utils.getAsString(element.getAsJsonObject(), JSON_ENUM_KEY_TEXT, EMPTY);
            var enumOrdinal = Utils.getAsString(element.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE, GENERIC_NO_VAL);
            list.add(new StateOption(enumOrdinal, StringUtils.capitalizeByWhitespace(enumText.toLowerCase())));
        }
        return list;
    }

    /**
     * internal method to dertermine the enum type.
     *
     * @param enumValues enum data from myuplink API
     * @param writable flag to determine writable capability
     * @return
     */
    @Nullable
    private ChannelTypeUID determineStaticEnumType(JsonArray enumValues, boolean writable) {
        boolean containsOffAt0 = false;
        boolean containsOnAt1 = false;

        for (var element : enumValues) {
            var enumText = Utils.getAsString(element.getAsJsonObject(), JSON_ENUM_KEY_TEXT, "").toLowerCase();
            var enumOrdinal = Utils.getAsString(element.getAsJsonObject(), JSON_KEY_CHANNEL_VALUE, GENERIC_NO_VAL);

            switch (enumText) {
                case JSON_ENUM_VAL_OFF -> containsOffAt0 = enumOrdinal.equals(JSON_ENUM_ORD_0);
                case JSON_ENUM_VAL_ON -> containsOnAt1 = enumOrdinal.equals(JSON_ENUM_ORD_1);
            }
        }

        if (enumValues.size() == 2 && containsOffAt0 && containsOnAt1) {
            if (writable) {
                return new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_RW_SWITCH);
            } else {
                return new ChannelTypeUID(BINDING_ID, CHANNEL_TYPE_ON_OFF);
            }
        }
        return null;
    }
}

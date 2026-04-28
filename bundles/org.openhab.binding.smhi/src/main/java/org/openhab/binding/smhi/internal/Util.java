/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smhi.internal;

import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.math.BigDecimal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smhi.provider.ParameterMetadata;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;

/**
 * A class with static utility methods to get correct data depending on the parameter
 *
 * @author Anders Alfredsson - Initial contribution
 */

@NonNullByDefault
public class Util {

    public static BigDecimal getMissingValue(ParameterMetadata metadata) {
        return switch (metadata.name()) {
            case TEMPERATURE, TEMPERATURE_MIN, TEMPERATURE_MAX -> DEFAULT_MISSING_VALUE;
            default -> BigDecimal.valueOf(-1);
        };
    }

    public static State getParameterAsState(ParameterMetadata metadata, BigDecimal value) {
        if (metadata.missingValue().equals(value)) {
            value = getMissingValue(metadata);
        }
        Unit<?> unit = UNIT_MAP.get(metadata.unit());

        value = switch (metadata.unit()) {
            case "fraction" -> value.multiply(FRACTION_TO_PERCENT);
            case "octas" -> value.multiply(OCTAS_TO_PERCENT);
            case "percent" -> value.intValue() == -9 ? BigDecimal.valueOf(-1) : value;
            default -> value;
        };

        if (unit != null) {
            return new QuantityType<>(value, unit);
        } else {
            return new DecimalType(value.stripTrailingZeros());
        }
    }

    public static ChannelType createChannelTypeFromMetadata(ParameterMetadata metadata) {
        String itemType = CoreItemFactory.NUMBER;
        Unit<?> unit = UNIT_MAP.get(metadata.unit());
        if (unit != null) {
            itemType += ":" + UnitUtils.getDimensionName(unit);
        }
        ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, metadata.name());
        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, metadata.description(), itemType)
                .isAdvanced(!STANDARD_CHANNELS.contains(metadata.name())).withTags(DefaultSemanticTags.Point.FORECAST);
        SemanticTag property = unit != null ? SEMANTIC_PROPERTIES.get(unit) : null;
        if (property != null) {
            builder.withTags(property);
        }
        if (unit != null) {
            builder.withUnitHint(unit.toString());
        }
        builder.withStateDescriptionFragment(createStateDescription(metadata));
        return builder.build();
    }

    private static StateDescriptionFragment createStateDescription(ParameterMetadata metadata) {
        StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create();
        switch (metadata.unit()) {
            case "Cel":
            case "m/s":
            case "m s**-1":
            case "km":
            case "octas":
            case "kg/m2":
                builder.withPattern("%.1f %unit%");
                break;
            case "degree":
            case "percent":
            case "hPa":
            case "fraction":
            case "m":
            case "%":
                builder.withPattern("%d %unit%");
        }
        return builder.build();
    }
}

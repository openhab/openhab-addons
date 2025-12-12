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
package org.openhab.binding.smartmeter.dlms.internal.helper;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartmeter.SmartMeterBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.util.UnitUtils;
import org.openmuc.jdlms.ObisCode.Medium;

/**
 * A helper class to create channel type uids and channel types for DLMS/COSEM meters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsChannelUtils {

    private static final String CATEGORY_ENERGY = "energy";
    private static final String CATEGORY_CLIMATE = "climate";
    private static final String CATEGORY_HEATING = "heating";
    private static final String CATEGORY_WATER = "water";
    private static final String CATEGORY_GAS = "gas";

    private static final Pattern CAMEL_CASE_PATTERN = Pattern
            .compile("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[^A-Z])(?=[A-Z])|(?<=[A-Za-z])(?=[^A-Za-z])");

    /**
     * Create a {@link ChannelType} for the given {@link ChannelTypeUID}, {@link QuantityType}
     * and {@link Medium}, for example
     *
     * <li>uid "smartmeter:electricity-electricpotential-volt"
     * <li>channel type 'state'
     * <li>item type "Number:ElectricPotential"
     * <li>state pattern "%.1f %unit%"
     * <li>read only "true"
     * <li>label "Electric Potential [V]"
     * <li>tags "Point:Measurement, Property:Energy"
     */
    public static ChannelType getChannelType(ChannelTypeUID uid, Medium medium, QuantityType<?> quantity) {
        String dimension = UnitUtils.getDimensionName(quantity.getUnit());
        if (dimension == null) {
            throw new IllegalArgumentException("Quantity has no dimension: " + quantity);
        }

        String label = "%s [%s]".formatted(splitCamelCase(dimension), quantity.getUnit().toString());
        String itemType = CoreItemFactory.NUMBER + ":" + dimension;

        StateDescriptionFragment stateFragment = StateDescriptionFragmentBuilder.create().withReadOnly(true)
                .withPattern("%.1f " + UnitUtils.UNIT_PLACEHOLDER).build();

        SemanticTag[] semanticTags;
        String category;
        switch (medium) {
            case ELECTRICITY:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.ENERGY };
                category = CATEGORY_ENERGY;
                break;
            case GAS:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.GAS };
                category = CATEGORY_GAS;
                break;
            case HOT_WATER:
            case COLD_WATER:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.WATER };
                category = CATEGORY_WATER;
                break;
            case HEAT:
            case HEAT_COST_ALLOCATOR:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.HEATING };
                category = CATEGORY_HEATING;
                break;
            case COOLING:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.AIRCONDITIONING };
                category = CATEGORY_CLIMATE;
                break;
            default:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT };
                category = null;
        }

        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(uid, label, itemType)
                .withStateDescriptionFragment(stateFragment).withTags(semanticTags);

        if (category != null) {
            builder.withCategory(category);
        }

        return builder.build();
    }

    /**
     * Create a @link ChannelTypeUID} for the given {@link QuantityType}, for example
     * "smartmeter:electricpotential-volt".
     */
    public static ChannelTypeUID getChannelTypeUID(Medium medium, QuantityType<?> quantity) {
        String dimension = UnitUtils.getDimensionName(quantity.getUnit());
        if (dimension == null) {
            throw new IllegalArgumentException("Quantity has no dimension: " + quantity);
        }
        return getChannelTypeUID(medium, quantity, dimension);
    }

    /**
     * Internal method to create a {@link ChannelTypeUID} for the given {@link QuantityType} and
     * dimension, for example "smartmeter:electricity-electricpotential-volt".
     */
    private static ChannelTypeUID getChannelTypeUID(Medium medium, QuantityType<?> quantity, String dimension) {
        String unitId = quantity.getUnit().getName() instanceof String name ? name : quantity.getUnit().toString();
        return new ChannelTypeUID(SmartMeterBindingConstants.BINDING_ID, medium.toString().toLowerCase() + "-"
                + dimension.toLowerCase() + "-" + unitId.toLowerCase().replaceAll("[^a-z0-9]", "_"));
    }

    /**
     * Internal method to split a camel case string into words, for example "ElectricPotential" converts
     * to "Electric Potential". This is used to create human-readable labels for channel types.
     */
    private static String splitCamelCase(String string) {
        return CAMEL_CASE_PATTERN.matcher(string).replaceAll(" ");
    }
}

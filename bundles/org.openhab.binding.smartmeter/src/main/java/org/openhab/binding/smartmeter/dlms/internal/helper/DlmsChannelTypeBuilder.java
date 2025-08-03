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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.StateChannelTypeBuilder;
import org.openhab.core.types.StateDescriptionFragment;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openmuc.jdlms.ObisCode.Medium;

/**
 * A helper class to create channel types for DLMS/COSEM meters.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class DlmsChannelTypeBuilder {

    /**
     * Create a new channel type for the given channel type UID and medium. Specifically it creates the following
     *
     * <li>channel type 'state'
     * <li>uid "smartmeter:ELECTRIC_POTENTIAL"
     * <li>item type "Number:ElectricPotential"
     * <li>state pattern "%.2f %unit%"
     * <li>read only is true
     * <li>label "Meter reading for electric potential" (subject to localisation)
     * <li>tagged with 'Point:Meaurement' plus whatever Property is appropriate
     *
     * @param channelTypeUID
     * @param medium
     * @param quantity
     * @return
     */
    public static ChannelType build(ChannelTypeUID channelTypeUID, Medium medium) {
        StringBuilder labelBuilder = new StringBuilder();
        StringBuilder itemTypeBuilder = new StringBuilder().append(CoreItemFactory.NUMBER).append(':');

        String typeId = channelTypeUID.getId();
        String[] typeParts = typeId.split("_");
        boolean severalWords = false;
        for (String typePart : typeParts) {
            char firstCharacter = Character.toUpperCase(typePart.charAt(0));
            String suffix = typePart.substring(1).toLowerCase();
            if (severalWords) {
                labelBuilder.append(' ');
            } else {
                severalWords = true;
            }
            labelBuilder.append(firstCharacter).append(suffix);
            itemTypeBuilder.append(firstCharacter).append(suffix);
        }

        String label = labelBuilder.toString();
        String itemType = itemTypeBuilder.toString();
        String description = "@text/dlms.meter-reading-for [\"%s\"]".formatted(label);

        StateDescriptionFragment stateFragment = StateDescriptionFragmentBuilder.create().withReadOnly(true)
                .withPattern("%.1f %unit%").build();

        SemanticTag[] semanticTags;
        String category;
        switch (medium) {
            case GAS:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.GAS };
                category = "gas";
                break;
            case HOT_WATER:
            case COLD_WATER:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.WATER };
                category = "water";
                break;
            case HEAT:
            case HEAT_COST_ALLOCATOR:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.HEATING };
                category = "heating";
                break;
            case COOLING:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT,
                        DefaultSemanticTags.Property.AIRCONDITIONING };
                category = "climate";
                break;
            default:
                semanticTags = new SemanticTag[] { DefaultSemanticTags.Point.MEASUREMENT };
                category = null;
        }

        StateChannelTypeBuilder builder = ChannelTypeBuilder.state(channelTypeUID, label, itemType) //
                .withDescription(description) //
                .withStateDescriptionFragment(stateFragment) //
                .withTags(semanticTags);

        if (category != null) {
            builder.withCategory(category);
        }

        return builder.build();
    }
}

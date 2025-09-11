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
package org.openhab.binding.homekit.internal.dto;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.BINDING_ID;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.CharacteristicType;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import com.google.gson.annotations.SerializedName;

/**
 * HomeKit characteristic DTO.
 * Used to deserialize individual characteristics from the /accessories endpoint of a HomeKit bridge.
 * Each characteristic has a type, instance ID (iid), value, permissions (perms), and format.
 * This class also includes a method to convert the characteristic to an openHAB ChannelType, if possible.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Characteristic {
    public @Nullable @SerializedName("iid") Integer instanceId; // e.g. 10
    public @Nullable @SerializedName("type") String characteristicId; // e.g. '25' = 'public.hap.characteristic.on'
    public @Nullable @SerializedName("value") String dataValue; // e.g. true
    public @Nullable @SerializedName("format") String dataFormat; // e.g. "bool"
    public @Nullable @SerializedName("perms") List<String> permissions; // e.g. ["read", "write", "events"]

    /**
     * Converts this characteristic to an openHAB ChannelType, if possible.
     * Returns null if the characteristic ID or data format is missing or unrecognized.
     *
     * @return the corresponding ChannelType, or null if not mappable
     */
    public @Nullable ChannelType getChannelType() {
        String characId = this.characteristicId;
        String dataFormat = this.dataFormat;
        if (characId == null || dataFormat == null) {
            return null;
        }

        CharacteristicType characType = CharacteristicType.from(characId);

        String label = "label"; // TODO determine label based on characType
        String itemType = CoreItemFactory.SWITCH; // TODO determine item type based on characType
        String category = "sensor"; // TODO determine category based on characType
        SemanticTag point = Point.STATUS; // TODO determine point based on characType
        SemanticTag property = Property.AIR_QUALITY; // TODO determine property based on characteristicType

        return ChannelTypeBuilder.state(new ChannelTypeUID(BINDING_ID, characId), label, itemType)
                .withTags(point, property).withCategory(category).build();
    }
}

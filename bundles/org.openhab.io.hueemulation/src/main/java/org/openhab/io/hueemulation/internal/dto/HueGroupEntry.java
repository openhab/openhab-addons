/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.io.hueemulation.internal.dto;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.io.hueemulation.internal.ConfigStore;
import org.openhab.io.hueemulation.internal.DeviceType;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

/**
 * Hue API group object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueGroupEntry {
    public static enum TypeEnum {
        LightGroup, // 1.4
        Luminaire, // 1.4
        LightSource, // 1.4
        Room, // 1.11
        Entertainment, // 1.22
        Zone // 1.30
    }

    public AbstractHueState action = new HueStatePlug();

    // The group type
    public String type = TypeEnum.LightGroup.name();

    // A unique, editable name given to the group.
    public String name;

    @SerializedName("class")
    public String roomclass = "Other";

    // The IDs of the lights that are in the group.
    public List<String> lights = Collections.emptyList();
    public List<String> sensors = Collections.emptyList();

    public transient @NonNullByDefault({}) GroupItem groupItem;
    public transient @Nullable DeviceType deviceType;

    // For deserialisation
    HueGroupEntry() {
        name = "";
    }

    public HueGroupEntry(String name, @Nullable GroupItem groupItem, @Nullable DeviceType deviceType) {
        this.name = name;
        this.groupItem = groupItem;
        this.deviceType = deviceType;
    }

    public void updateItem(GroupItem element) {
        groupItem = element;
    }

    /**
     * This custom serializer computes the {@link HueGroupEntry#lights} list, before serializing.
     * It does so, by looking up all item members of the references groupItem.
     */
    @NonNullByDefault({})
    public static class Serializer implements JsonSerializer<HueGroupEntry> {

        private ConfigStore cs;

        public Serializer(ConfigStore cs) {
            this.cs = cs;
        }

        static class HueGroupHelper extends HueGroupEntry {

        }

        @Override
        public JsonElement serialize(HueGroupEntry product, Type type, JsonSerializationContext context) {
            GroupItem item = product.groupItem;
            if (item != null) {
                product.lights = item.getMembers().stream().map(gitem -> cs.mapItemUIDtoHueID(gitem))
                        .collect(Collectors.toList());
            }

            JsonElement jsonSubscription = context.serialize(product, HueGroupHelper.class);
            return jsonSubscription;
        }
    }
}

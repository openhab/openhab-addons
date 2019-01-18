/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.GroupItem;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Hue API group object
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class HueGroup {
    public HueStateColorBulb action = new HueStateColorBulb();
    public String type = "LightGroup";
    public String name;
    public List<String> lights = Collections.emptyList();

    public transient @Nullable GroupItem groupItem;
    public transient Map<String, Integer> itemUIDtoHueID;

    public HueGroup(String name, @Nullable GroupItem groupItem, Map<String, Integer> itemUIDtoHueID) {
        this.name = name;
        this.groupItem = groupItem;
        this.itemUIDtoHueID = itemUIDtoHueID;
    }

    public void updateItem(GroupItem element) {
        groupItem = element;
    }

    /**
     * This custom serializer computes the {@link HueGroup#lights} list, before serializing.
     * It does so, by looking up all item members of the references groupItem and map them to
     * either a known hue ID or filtering them out.
     *
     */
    @NonNullByDefault({})
    public static class Serializer implements JsonSerializer<HueGroup> {

        @SuppressWarnings("null")
        @Override
        public JsonElement serialize(HueGroup product, Type type, JsonSerializationContext jsc) {
            GroupItem item = product.groupItem;
            if (item != null) {
                product.lights = item.getMembers().stream().map(gitem -> product.itemUIDtoHueID.get(gitem.getUID()))
                        .filter(id -> id != null).map(e -> String.valueOf(e)).collect(Collectors.toList());
            }

            JsonObject o = new JsonObject();
            o.addProperty("name", product.name);
            o.addProperty("type", product.type);
            o.add("action", jsc.serialize(product.action));
            o.add("lights", jsc.serialize(product.lights));
            return o;
        }
    }

}

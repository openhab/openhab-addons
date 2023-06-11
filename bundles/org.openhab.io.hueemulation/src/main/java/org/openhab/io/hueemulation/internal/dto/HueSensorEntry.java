/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Hue API scene object
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class HueSensorEntry {
    // A unique, editable name given to the group.
    public String name;
    public String type;
    public String modelid;
    public String manufacturername = "openHab";
    public String swversion = "1.0";
    public Object config = new Object();
    public String uniqueid;

    public final @NonNullByDefault({}) transient GenericItem item;

    private HueSensorEntry() {
        item = null;
        name = "";
        type = "";
        modelid = "";
        uniqueid = "";
    }

    public HueSensorEntry(GenericItem item) throws IllegalArgumentException {
        this.item = item;
        String label = item.getLabel();
        this.name = label != null ? label : item.getName();
        this.modelid = "openHAB_" + item.getType();
        this.uniqueid = item.getUID();
        switch (item.getType()) {
            case CoreItemFactory.CONTACT:
                this.type = "CLIPOpenClose"; // "open"
                break;
            case CoreItemFactory.ROLLERSHUTTER:
            case CoreItemFactory.DIMMER:
                this.type = "CLIPGenericStatus"; // "status" (int)
                break;
            case CoreItemFactory.NUMBER:
                if (item.hasTag("temperature")) {
                    this.type = "CLIPTemperature"; // "temperature"
                } else {
                    this.type = "CLIPLightLevel"; // "lightlevel" (int), "dark" (bool), "daylight" (bool)
                }
                break;
            case CoreItemFactory.COLOR:
                this.type = "CLIPLightLevel"; // "lightlevel" (int), "dark" (bool), "daylight" (bool)
                break;
            case CoreItemFactory.SWITCH:
                this.type = "CLIPGenericFlag"; // "flag" (bool)
                break;
            default:
                throw new IllegalArgumentException("Item type not supported as sensor");
        }
    }

    /**
     * This custom serializer computes the {@link HueGroupEntry#lights} list, before serializing.
     * It does so, by looking up all item members of the references groupItem.
     */
    @NonNullByDefault({})
    public static class Serializer implements JsonSerializer<HueSensorEntry> {

        static class HueHelper extends HueSensorEntry {

        }

        @Override
        public JsonElement serialize(HueSensorEntry product, Type type, JsonSerializationContext context) {
            JsonElement json = context.serialize(product, HueHelper.class);
            JsonObject state = new JsonObject();
            State itemState = product.item.getState();
            switch (product.type) {
                case "CLIPOpenClose":
                    if (itemState instanceof OpenClosedType) {
                        state.addProperty("open", ((OpenClosedType) product.item.getState()) == OpenClosedType.OPEN);
                    } else {
                        state.addProperty("open", false);
                    }
                    break;
                case "CLIPGenericStatus":
                    if (itemState instanceof DecimalType) {
                        state.addProperty("status", ((DecimalType) product.item.getState()).intValue());
                    } else {
                        state.addProperty("status", 0);
                    }
                    break;
                case "CLIPTemperature":
                    if (itemState instanceof DecimalType) {
                        state.addProperty("temperature", ((DecimalType) product.item.getState()).intValue());
                    } else {
                        state.addProperty("status", 0);
                    }
                    break;
                case "CLIPLightLevel":
                    if (itemState instanceof DecimalType) {
                        state.addProperty("lightlevel", ((DecimalType) product.item.getState()).intValue());
                    } else {
                        state.addProperty("status", 0);
                    }
                    state.addProperty("dark", false);
                    state.addProperty("daylight", false);
                    break;
                case "CLIPGenericFlag":
                    if (itemState instanceof OnOffType) {
                        state.addProperty("flag", ((OnOffType) product.item.getState()) == OnOffType.ON);
                    } else {
                        state.addProperty("open", false);
                    }
                    break;
            }
            json.getAsJsonObject().add("state", state);
            return json;
        }
    }
}

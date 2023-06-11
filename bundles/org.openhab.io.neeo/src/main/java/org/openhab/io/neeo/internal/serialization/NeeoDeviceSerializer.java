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
package org.openhab.io.neeo.internal.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ThingType;
import org.openhab.io.neeo.NeeoService;
import org.openhab.io.neeo.internal.NeeoBrainServlet;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.io.neeo.internal.NeeoDeviceKeys;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceTiming;
import org.openhab.io.neeo.internal.models.NeeoDeviceType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial
 * {@link NeeoDevice}. This implementation should NOT be used in communications with the NEEO brain (use
 * {@link NeeoBrainDeviceSerializer} instead)
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceSerializer implements JsonSerializer<NeeoDevice>, JsonDeserializer<NeeoDevice> {

    /** The service */
    @Nullable
    private final NeeoService service;

    /** The service context */
    @Nullable
    private final ServiceContext context;

    /**
     * Constructs the object with no service or context
     */
    public NeeoDeviceSerializer() {
        this(null, null);
    }

    /**
     * Constructs the object from the service and context. A null service or context will suppress certain values on the
     * returned json object
     *
     * @param service the possibly null service
     * @param context the possibly null context
     */
    public NeeoDeviceSerializer(@Nullable NeeoService service, @Nullable ServiceContext context) {
        this.service = service;
        this.context = context;
    }

    @Override
    public @Nullable NeeoDevice deserialize(JsonElement elm, Type type, JsonDeserializationContext jsonContext)
            throws JsonParseException {
        if (!(elm instanceof JsonObject)) {
            throw new JsonParseException("Element not an instance of JsonObject: " + elm);
        }

        final JsonObject jo = (JsonObject) elm;
        final NeeoThingUID uid = jsonContext.deserialize(jo.get("uid"), NeeoThingUID.class);
        final NeeoDeviceType devType = jsonContext.deserialize(jo.get("type"), NeeoDeviceType.class);
        final String manufacturer = NeeoUtil.getString(jo, "manufacturer");
        final String name = NeeoUtil.getString(jo, "name");
        final NeeoDeviceChannel[] channels = jsonContext.deserialize(jo.get("channels"), NeeoDeviceChannel[].class);
        final NeeoDeviceTiming timing = jo.has("timing")
                ? jsonContext.deserialize(jo.get("timing"), NeeoDeviceTiming.class)
                : null;

        final String[] deviceCapabilities = jo.has("deviceCapabilities")
                ? jsonContext.deserialize(jo.get("deviceCapabilities"), String[].class)
                : null;

        final String specificName = jo.has("specificName") ? jo.get("specificName").getAsString() : null;

        final String iconName = jo.has("iconName") ? jo.get("iconName").getAsString() : null;
        final int driverVersion = jo.has("driverVersion") ? jo.get("driverVersion").getAsInt() : 0;

        try {
            return new NeeoDevice(uid, driverVersion, devType,
                    manufacturer == null || manufacturer.isEmpty() ? NeeoUtil.NOTAVAILABLE : manufacturer, name,
                    Arrays.asList(channels), timing,
                    deviceCapabilities == null ? null : Arrays.asList(deviceCapabilities), specificName, iconName);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new JsonParseException(e);
        }
    }

    @Override
    public JsonElement serialize(NeeoDevice device, @Nullable Type deviceType,
            @Nullable JsonSerializationContext jsonContext) {
        Objects.requireNonNull(device, "device cannot be null");
        Objects.requireNonNull(deviceType, "deviceType cannot be null");
        Objects.requireNonNull(jsonContext, "jsonContext cannot be null");

        final JsonObject jsonObject = new JsonObject();

        final NeeoThingUID uid = device.getUid();
        jsonObject.add("uid", jsonContext.serialize(uid));
        jsonObject.add("type", jsonContext.serialize(device.getType()));
        jsonObject.addProperty("manufacturer", device.getManufacturer());
        jsonObject.addProperty("name", device.getName());
        jsonObject.addProperty("specificName", device.getSpecificName());
        jsonObject.addProperty("iconName", device.getIconName());
        jsonObject.addProperty("driverVersion", device.getDriverVersion());

        final JsonArray channels = (JsonArray) jsonContext.serialize(device.getChannels());

        final NeeoDeviceTiming timing = device.getDeviceTiming();
        jsonObject.add("timing", jsonContext.serialize(timing == null ? new NeeoDeviceTiming() : timing));

        jsonObject.add("deviceCapabilities", jsonContext.serialize(device.getDeviceCapabilities()));

        jsonObject.addProperty("thingType", uid.getThingType());

        if (NeeoConstants.NEEOIO_BINDING_ID.equalsIgnoreCase(uid.getBindingId())) {
            jsonObject.addProperty("thingStatus", uid.getThingType().toUpperCase());
        }

        final ServiceContext localContext = context;
        if (localContext != null) {
            if (!NeeoConstants.NEEOIO_BINDING_ID.equalsIgnoreCase(uid.getBindingId())) {
                final Thing thing = localContext.getThingRegistry().get(device.getUid().asThingUID());
                jsonObject.addProperty("thingStatus",
                        thing == null ? ThingStatus.UNKNOWN.name() : thing.getStatus().name());

                if (thing != null) {
                    final ThingType thingType = localContext.getThingTypeRegistry()
                            .getThingType(thing.getThingTypeUID());

                    if (thingType != null) {
                        for (JsonElement chnl : channels) {
                            JsonObject jo = (JsonObject) chnl;
                            if (jo.has("groupId") && jo.has("itemLabel")) {
                                final String groupId = jo.get("groupId").getAsString();
                                final String groupLabel = NeeoUtil.getGroupLabel(thingType, groupId);
                                if (groupLabel != null && !groupLabel.isEmpty()) {
                                    final JsonElement itemLabel = jo.remove("itemLabel");
                                    jo.addProperty("itemLabel", groupLabel + "#" + itemLabel.getAsString());
                                } else if (groupId != null && !groupId.isEmpty()) {
                                    // have a groupid but no group definition found (usually error on binding)
                                    // just default to "Others".
                                    final JsonElement itemLabel = jo.remove("itemLabel");
                                    jo.addProperty("itemLabel", "Others#" + itemLabel.getAsString());
                                }
                            }
                        }
                    }
                }
            }
        }

        jsonObject.add("channels", channels);

        final NeeoService localService = service;
        if (localService != null) {
            List<String> foundKeys = new ArrayList<>();
            for (final NeeoBrainServlet servlet : localService.getServlets()) {
                final NeeoDeviceKeys servletKeys = servlet.getDeviceKeys();
                final Set<String> keys = servletKeys.get(device.getUid());
                foundKeys.addAll(keys);
            }
            jsonObject.add("keys", jsonContext.serialize(foundKeys));
        }

        return jsonObject;
    }
}

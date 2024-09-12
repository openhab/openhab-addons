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
package org.openhab.io.neeo.internal.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Command;
import org.openhab.io.neeo.internal.NeeoUtil;
import org.openhab.io.neeo.internal.ServiceContext;
import org.openhab.io.neeo.internal.models.ItemSubType;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelDirectory;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelDirectoryListItem;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelKind;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelRange;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannelText;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Implementation of {@link JsonSerializer} and {@link JsonDeserializer} to serialize/deserial
 * {@link NeeoDeviceChannel}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoDeviceChannelSerializer
        implements JsonSerializer<NeeoDeviceChannel>, JsonDeserializer<NeeoDeviceChannel> {

    /** The service context */
    @Nullable
    private final ServiceContext context;

    /**
     * Creates the serializer with no context
     */
    public NeeoDeviceChannelSerializer() {
        this(null);
    }

    /**
     * Creates the serializer using the given context. A null context will suppress certain values on the returned
     * object
     *
     * @param context the possibly null context
     */
    public NeeoDeviceChannelSerializer(@Nullable ServiceContext context) {
        this.context = context;
    }

    @Override
    public JsonElement serialize(NeeoDeviceChannel chnl, Type type, JsonSerializationContext jsonContext) {
        final JsonObject jo = new JsonObject();

        jo.add("kind", jsonContext.serialize(chnl.getKind()));
        jo.addProperty("itemName", chnl.getItemName());
        jo.addProperty("label", chnl.getLabel());
        jo.addProperty("value", chnl.getValue());
        jo.addProperty("channelNbr", chnl.getChannelNbr());
        jo.add("type", jsonContext.serialize(chnl.getType()));
        jo.add("subType", jsonContext.serialize(chnl.getSubType()));
        jo.add("range", jsonContext.serialize(chnl.getRange()));

        final ServiceContext localContext = context;
        if (localContext != null) {
            final List<String> commandTypes = new ArrayList<>();
            boolean isReadOnly = false;
            String itemLabel = chnl.getLabel();
            String itemType = null;

            try {
                final Item item = localContext.getItemRegistry().getItem(chnl.getItemName());
                itemType = item.getType();

                String label = item.getLabel();
                if (label != null && !label.isEmpty()) {
                    itemLabel = label;
                }

                for (Class<? extends Command> cmd : item.getAcceptedCommandTypes()) {
                    if (!"refreshtype".equalsIgnoreCase(cmd.getSimpleName())) {
                        commandTypes.add(cmd.getSimpleName().toLowerCase());
                    }
                }

                for (ChannelUID channelUid : localContext.getItemChannelLinkRegistry()
                        .getBoundChannels(chnl.getItemName())) {
                    if (channelUid != null) {
                        jo.addProperty("groupId", channelUid.getGroupId());
                        final Channel channel = localContext.getThingRegistry().getChannel(channelUid);
                        if (channel != null) {
                            final ChannelType channelType = localContext.getChannelTypeRegistry()
                                    .getChannelType(channel.getChannelTypeUID());
                            if (channelType != null && channelType.getState() != null) {
                                isReadOnly = channelType.getState().isReadOnly();
                            }
                        }
                    }
                }
            } catch (ItemNotFoundException e) {
                itemType = "N/A";
            }

            if (!itemLabel.isEmpty()) {
                switch (chnl.getSubType()) {
                    case HUE:
                        itemType += " (Hue)";
                        break;

                    case SATURATION:
                        itemType += " (Sat)";
                        break;

                    case BRIGHTNESS:
                        itemType += " (Bri)";
                        break;

                    default:
                        break;
                }
            }

            jo.addProperty("itemType", itemType);
            jo.addProperty("itemLabel", itemLabel);
            jo.add("acceptedCommandTypes", jsonContext.serialize(commandTypes));
            jo.addProperty("isReadOnly", isReadOnly);

        }

        if (chnl instanceof NeeoDeviceChannelText text) {
            jo.addProperty("labelVisible", text.isLabelVisible());
        } else if (chnl instanceof NeeoDeviceChannelDirectory directory) {
            jo.add("listItems", jsonContext.serialize(directory.getListItems()));
        }

        return jo;
    }

    @Override
    public @Nullable NeeoDeviceChannel deserialize(JsonElement elm, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        if (!(elm instanceof JsonObject)) {
            throw new JsonParseException("Element not an instance of JsonObject: " + elm);
        }

        final JsonObject jo = (JsonObject) elm;
        final String itemName = NeeoUtil.getString(jo, "itemName");

        if (itemName == null || itemName.isEmpty()) {
            throw new JsonParseException("Element requires an itemName attribute: " + elm);
        }

        final ItemSubType itemSubType = jo.has("subType") ? context.deserialize(jo.get("subType"), ItemSubType.class)
                : ItemSubType.NONE;

        final String label = NeeoUtil.getString(jo, "label");
        final String value = NeeoUtil.getString(jo, "value");
        final Integer channelNbr = NeeoUtil.getInt(jo, "channelNbr");

        if (channelNbr == null) {
            throw new JsonParseException("Channel Number is not a valid integer");
        }
        final NeeoCapabilityType capType = context.deserialize(jo.get("type"), NeeoCapabilityType.class);

        final NeeoDeviceChannelRange range = jo.has("range")
                ? context.deserialize(jo.get("range"), NeeoDeviceChannelRange.class)
                : null;

        final NeeoDeviceChannelKind kind = jo.has("kind")
                ? context.deserialize(jo.get("kind"), NeeoDeviceChannelKind.class)
                : NeeoDeviceChannelKind.ITEM;

        try {
            if (capType == NeeoCapabilityType.TEXTLABEL) {
                final boolean labelVisible = jo.has("labelVisible") ? jo.get("labelVisible").getAsBoolean() : true;

                return new NeeoDeviceChannelText(kind, itemName, channelNbr, capType, itemSubType,
                        label == null || label.isEmpty() ? NeeoUtil.NOTAVAILABLE : label, value, range, labelVisible);
            } else if (capType == NeeoCapabilityType.DIRECTORY) {
                final NeeoDeviceChannelDirectoryListItem[] listItems = jo.has("listItems")
                        ? context.deserialize(jo.get("listItems"), NeeoDeviceChannelDirectoryListItem[].class)
                        : new NeeoDeviceChannelDirectoryListItem[0];

                return new NeeoDeviceChannelDirectory(kind, itemName, channelNbr, capType, itemSubType,
                        label == null || label.isEmpty() ? NeeoUtil.NOTAVAILABLE : label, value, range, listItems);
            } else {
                return new NeeoDeviceChannel(kind, itemName, channelNbr, capType, itemSubType,
                        label == null || label.isEmpty() ? NeeoUtil.NOTAVAILABLE : label, value, range);
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new JsonParseException(e);
        }
    }
}

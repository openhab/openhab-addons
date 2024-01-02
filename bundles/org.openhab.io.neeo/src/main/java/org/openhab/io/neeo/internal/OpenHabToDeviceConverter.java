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
package org.openhab.io.neeo.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.io.neeo.internal.models.ItemSubType;
import org.openhab.io.neeo.internal.models.NeeoCapabilityType;
import org.openhab.io.neeo.internal.models.NeeoDevice;
import org.openhab.io.neeo.internal.models.NeeoDeviceChannel;
import org.openhab.io.neeo.internal.models.NeeoDeviceTiming;
import org.openhab.io.neeo.internal.models.NeeoDeviceType;
import org.openhab.io.neeo.internal.models.NeeoThingUID;
import org.openhab.io.neeo.internal.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class will handle the conversion between a {@link Thing} and a {@link NeeoDevice}
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
class OpenHabToDeviceConverter {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(OpenHabToDeviceConverter.class);

    /** The service context */
    private final ServiceContext context;

    /** Whether to expose all items by default or not */
    private final boolean exposeAll;

    /** Whether to expose things by the NEEO binding by default or not */
    private final boolean exposeNeeoBinding;

    /**
     * Constructs the object from the give {@link ServiceContext}
     *
     * @param context the non-null service context
     */
    OpenHabToDeviceConverter(ServiceContext context) {
        Objects.requireNonNull(context, "context cannot be null");

        this.context = context;

        exposeAll = context.isExposeAllThings();
        exposeNeeoBinding = context.isExposeNeeoBinding();
    }

    /**
     * Convert the {@link Thing} to a {@link NeeoDevice}
     *
     * @param thing the non-null thing
     * @return a potentially null neeo device
     */
    @Nullable
    NeeoDevice convert(Thing thing) {
        Objects.requireNonNull(thing, "thing cannot be null");

        final List<NeeoDeviceChannel> channels = new ArrayList<>();

        final ThingUID thingUID = thing.getUID();
        final Set<String> existingLabels = new HashSet<>();

        for (Channel channel : thing.getChannels()) {
            final ChannelUID uid = channel.getUID();

            if (channel.getKind() == ChannelKind.TRIGGER) {
                channels.addAll(
                        NeeoDeviceChannel.from(channel, NeeoCapabilityType.BUTTON, ItemSubType.NONE, existingLabels));
            } else {
                final ChannelType channelType = context.getChannelTypeRegistry()
                        .getChannelType(channel.getChannelTypeUID());

                NeeoCapabilityType type = NeeoCapabilityType.EXCLUDE;
                if (NeeoConstants.NEEOBINDING_BINDING_ID.equalsIgnoreCase(thingUID.getBindingId())) {
                    if (thingUID.getAsString().toLowerCase()
                            .startsWith(NeeoConstants.NEEOBINDING_DEVICE_ID.toLowerCase())) {
                        // all device channels are currently macros - so buttons are appropriate
                        type = NeeoCapabilityType.BUTTON;
                    } else {
                        type = NeeoCapabilityType.guessType(channelType);
                    }
                } else if (exposeAll) {
                    type = NeeoCapabilityType.guessType(channelType);
                }

                final Set<Item> linkedItems = context.getItemChannelLinkRegistry().getLinkedItems(uid);
                if (linkedItems != null) {
                    for (Item item : linkedItems) {
                        channels.addAll(NeeoDeviceChannel.from(item, channel, channelType, type, existingLabels));
                    }
                }
            }

        }

        if (channels.isEmpty()) {
            logger.debug("No linked channels found for thing {} - ignoring", thing.getLabel());
            return null;
        }

        if (NeeoConstants.NEEOBINDING_BINDING_ID.equalsIgnoreCase(thing.getUID().getBindingId())) {
            final Map<String, String> properties = thing.getProperties();
            /** The following properties have matches in org.openhab.binding.neeo.NeeoDeviceHandler.java */
            String neeoType = properties.get("Type");
            if (neeoType == null || neeoType.isEmpty()) {
                neeoType = NeeoDeviceType.ACCESSOIRE.toString();
            }
            String manufacturer = properties.get("Manufacturer");
            if (manufacturer == null || manufacturer.isEmpty()) {
                manufacturer = "openHAB";
            }
            final Integer standbyDelay = parseInteger(properties.getOrDefault("Standby Command Delay", "0"));
            final Integer switchDelay = parseInteger(properties.getOrDefault("Source Switch Delay", "0"));
            final Integer shutDownDelay = parseInteger(properties.getOrDefault("Shutdown Delay", "0"));

            final NeeoDeviceTiming timing = new NeeoDeviceTiming(standbyDelay, switchDelay, shutDownDelay);

            final String dc = properties.get("Device Capabilities");
            final String[] deviceCapabilities = dc == null || dc.isEmpty() ? new String[0] : StringUtils.split(dc, ",");

            try {
                return new NeeoDevice(new NeeoThingUID(thing.getUID()), 0,
                        exposeNeeoBinding ? NeeoDeviceType.parse(neeoType) : NeeoDeviceType.EXCLUDE, manufacturer,
                        thing.getLabel(), channels, timing, Arrays.asList(deviceCapabilities), null, null);
            } catch (IllegalArgumentException e) {
                logger.debug("NeeoDevice constructor threw an IAE - ignoring device: {} - {}", thing.getUID(),
                        e.getMessage(), e);
                return null;
            }
        } else {
            try {
                return new NeeoDevice(thing, channels, exposeAll ? NeeoUtil.guessType(thing) : NeeoDeviceType.EXCLUDE,
                        null);
            } catch (IllegalArgumentException e) {
                logger.debug("NeeoDevice constructor threw an IAE - ignoring device: {} - {}", thing.getUID(),
                        e.getMessage(), e);
                return null;
            }
        }
    }

    /**
     * Helper method to parse a value to an Integer (or null if not a number)
     *
     * @param value a possibly null, possibly empty value to parse
     * @return an Integer or null if not a number
     */

    private static @Nullable Integer parseInteger(String value) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns a {@link NeeoDeviceChannel} that represents the given itemname (or null if itemname is not found)
     *
     * @param itemName a possibly empty, possibly null item name
     * @return a {@link NeeoDeviceChannel} representing the item name or null if not found
     */
    @Nullable
    List<NeeoDeviceChannel> getNeeoDeviceChannel(String itemName) {
        if (itemName.isEmpty()) {
            return null;
        }

        try {
            final Item item = context.getItemRegistry().getItem(itemName);
            return NeeoDeviceChannel.from(item, null, null, NeeoCapabilityType.EXCLUDE, new HashSet<>());
        } catch (ItemNotFoundException e) {
            return null;
        }
    }
}

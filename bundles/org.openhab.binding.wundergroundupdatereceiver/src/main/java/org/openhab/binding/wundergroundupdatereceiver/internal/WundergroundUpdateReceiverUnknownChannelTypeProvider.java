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
package org.openhab.binding.wundergroundupdatereceiver.internal;

import static org.openhab.binding.wundergroundupdatereceiver.internal.WundergroundUpdateReceiverBindingConstants.THING_TYPE_UPDATE_RECEIVER;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeBuilder;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Demus - Initial contribution
 */
@Component(service = { ChannelTypeProvider.class, WundergroundUpdateReceiverUnknownChannelTypeProvider.class })
@NonNullByDefault
public class WundergroundUpdateReceiverUnknownChannelTypeProvider implements ChannelTypeProvider {

    private static final List<String> BOOLEAN_STRINGS = List.of("1", "0", "true", "false");
    private final Map<ChannelTypeUID, ChannelType> channelTypes = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(WundergroundUpdateReceiverUnknownChannelTypeProvider.class);

    @Override
    public Collection<ChannelType> getChannelTypes(@Nullable Locale locale) {
        return channelTypes.values();
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        return channelTypes.get(channelTypeUID);
    }

    public ChannelType getOrCreateChannelType(String parameterName, String value) {
        ChannelTypeUID typeUid = new ChannelTypeUID(THING_TYPE_UPDATE_RECEIVER.getBindingId(), parameterName);
        @Nullable
        ChannelType type = getChannelType(typeUid, null);
        if (type == null) {
            String itemType = guessItemType(value);
            type = ChannelTypeBuilder.state(typeUid, parameterName + " channel type", itemType).build();
            return addChannelType(typeUid, type);
        }
        return type;
    }

    private static String guessItemType(String value) {
        if (BOOLEAN_STRINGS.contains(value.toLowerCase())) {
            return "Switch";
        }
        try {
            Float.valueOf(value);
            return "Number";
        } catch (NumberFormatException ignored) {
        }
        return "String";
    }

    private ChannelType addChannelType(ChannelTypeUID channelTypeUID, ChannelType channelType) {
        logger.warn("Adding channelType {} for unknown parameter", channelTypeUID.getAsString());
        this.channelTypes.put(channelTypeUID, channelType);
        return channelType;
    }
}

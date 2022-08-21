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
package org.openhab.binding.arcam.internal.devices;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.arcam.internal.ArcamBindingConstants;
import org.openhab.binding.arcam.internal.connection.ArcamCommandDataFinder;
import org.openhab.binding.arcam.internal.exceptions.NotFoundException;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeProvider;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link ArcamAVR20ChannelTypeProvider} class provides the device specific channel types.
 *
 * @author Joep Admiraal - Initial contribution
 */
@Component(service = ChannelTypeProvider.class)
@NonNullByDefault
public class ArcamAVR20ChannelTypeProvider implements ChannelTypeProvider {

    public static final String AVR20_DISPLAY_BRIGHTNESS = "avr20DisplayBrightness";
    public static final String AVR20_MASTER_INPUT = "avr20MasterInput";
    public static final String AVR20_ZONE2_INPUT = "avr20Zone2Input";

    @Override
    public Collection<@NonNull ChannelType> getChannelTypes(@Nullable Locale locale) {
        List<ChannelType> channelTypeList = new LinkedList<>();
        channelTypeList.add(getChannelTypeOrThrow(AVR20_DISPLAY_BRIGHTNESS, locale));
        channelTypeList.add(getChannelTypeOrThrow(AVR20_MASTER_INPUT, locale));
        channelTypeList.add(getChannelTypeOrThrow(AVR20_ZONE2_INPUT, locale));

        return channelTypeList;
    }

    @Override
    public @Nullable ChannelType getChannelType(ChannelTypeUID channelTypeUID, @Nullable Locale locale) {
        if (!channelTypeUID.getBindingId().equals(ArcamBindingConstants.BINDING_ID)) {
            return null;
        }

        String channelID = channelTypeUID.getId();

        if (channelID.equals(AVR20_DISPLAY_BRIGHTNESS)) {
            return ArcamCommandDataFinder.generateStringOptionChannelType( //
                    channelTypeUID, //
                    "Display brightness", //
                    "Select display brightness", //
                    ArcamAVR20.DISPLAY_BRIGHTNESS_COMMANDS); //
        }

        if (channelID.equals(AVR20_MASTER_INPUT)) {
            return ArcamCommandDataFinder.generateStringOptionChannelType( //
                    channelTypeUID, //
                    "xMaster Input", //
                    "Select the input source", //
                    ArcamAVR20.INPUT_COMMANDS); //
        }

        if (channelID.equals(AVR20_ZONE2_INPUT)) {
            return ArcamCommandDataFinder.generateStringOptionChannelType( //
                    channelTypeUID, //
                    "Zone2 Input", //
                    "Select the input source", //
                    ArcamAVR20.INPUT_COMMANDS); //
        }

        return null;
    }

    private ChannelType getChannelTypeOrThrow(String id, @Nullable Locale locale) {
        ChannelType channelType = getChannelType(new ChannelTypeUID(ArcamBindingConstants.BINDING_ID, id), locale);
        if (channelType == null) {
            throw new NotFoundException("Could not find Arcam channelType " + id);
        }

        return channelType;
    }
}

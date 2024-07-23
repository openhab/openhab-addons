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
package org.openhab.binding.emotiva.internal.protocol;

import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_CHANNEL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_FREQUENCY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_HEIGHT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MAIN_VOLUME_DB;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_CONTROL;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_DOWN;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_ENTER;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_LEFT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_RIGHT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MENU_UP;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_ALL_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_AUTO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DIRECT;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DOLBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_DTS;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MOVIE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_MUSIC;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_REF_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_STEREO;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MODE_SURROUND;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SEEK;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_STANDBY;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_SURROUND_MODE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_WIDTH;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_MUTE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_SOURCE;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME;
import static org.openhab.binding.emotiva.internal.EmotivaBindingConstants.CHANNEL_ZONE2_VOLUME_DB;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Maps OH channels with only an indirect connection to an Emotiva command. Only handles 1:1 mappings.
 *
 * @author Espen Fossen - Initial contribution
 */
@NonNullByDefault
public enum OHChannelToEmotivaCommand {

    standby(CHANNEL_STANDBY, EmotivaControlCommands.standby),
    source(CHANNEL_SOURCE, EmotivaControlCommands.input),
    menu(CHANNEL_MENU, EmotivaControlCommands.menu),
    menu_control(CHANNEL_MENU_CONTROL, EmotivaControlCommands.menu_control),
    up(CHANNEL_MENU_UP, EmotivaControlCommands.up),
    down(CHANNEL_MENU_DOWN, EmotivaControlCommands.down),
    left(CHANNEL_MENU_LEFT, EmotivaControlCommands.left),
    right(CHANNEL_MENU_RIGHT, EmotivaControlCommands.right),
    enter(CHANNEL_MENU_ENTER, EmotivaControlCommands.enter),
    mute(CHANNEL_MUTE, EmotivaControlCommands.mute),
    volume(CHANNEL_MAIN_VOLUME, EmotivaControlCommands.volume),
    volume_db(CHANNEL_MAIN_VOLUME_DB, EmotivaControlCommands.volume),
    zone2_volume(CHANNEL_ZONE2_VOLUME, EmotivaControlCommands.zone2_volume),
    zone2_volume_db(CHANNEL_ZONE2_VOLUME_DB, EmotivaControlCommands.zone2_volume),
    zone2_mute(CHANNEL_ZONE2_MUTE, EmotivaControlCommands.zone2_mute),
    zone2_source(CHANNEL_ZONE2_SOURCE, EmotivaControlCommands.zone2_input),
    width(CHANNEL_WIDTH, EmotivaControlCommands.width_trim_set),
    height(CHANNEL_HEIGHT, EmotivaControlCommands.height_trim_set),
    frequency(CHANNEL_FREQUENCY, EmotivaControlCommands.frequency),
    seek(CHANNEL_SEEK, EmotivaControlCommands.seek),
    channel(CHANNEL_CHANNEL, EmotivaControlCommands.channel),
    mode_ref_stereo(CHANNEL_MODE_REF_STEREO, EmotivaControlCommands.reference_stereo),
    surround_mode(CHANNEL_SURROUND_MODE, EmotivaControlCommands.surround_mode),
    mode_surround(CHANNEL_MODE_SURROUND, EmotivaControlCommands.surround_mode),
    mode_stereo(CHANNEL_MODE_STEREO, EmotivaControlCommands.stereo),
    mode_music(CHANNEL_MODE_MUSIC, EmotivaControlCommands.music),
    mode_movie(CHANNEL_MODE_MOVIE, EmotivaControlCommands.movie),
    mode_direct(CHANNEL_MODE_DIRECT, EmotivaControlCommands.direct),
    mode_dolby(CHANNEL_MODE_DOLBY, EmotivaControlCommands.dolby),
    mode_dts(CHANNEL_MODE_DTS, EmotivaControlCommands.dts),
    mode_all_stereo(CHANNEL_MODE_ALL_STEREO, EmotivaControlCommands.all_stereo),
    mode_auto(CHANNEL_MODE_AUTO, EmotivaControlCommands.auto);

    private final String ohChannel;
    private final EmotivaControlCommands command;

    OHChannelToEmotivaCommand(String ohChannel, EmotivaControlCommands command) {
        this.ohChannel = ohChannel;
        this.command = command;
    }

    public String getChannel() {
        return ohChannel;
    }

    public EmotivaControlCommands getCommand() {
        return command;
    }

    public static EmotivaControlCommands fromChannelUID(String id) {
        for (OHChannelToEmotivaCommand value : values()) {
            if (id.equals(value.ohChannel)) {
                return value.command;
            }
        }
        return EmotivaControlCommands.none;
    }
}

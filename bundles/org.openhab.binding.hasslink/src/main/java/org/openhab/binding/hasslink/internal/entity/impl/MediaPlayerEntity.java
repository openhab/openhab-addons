/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hasslink.internal.entity.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hasslink.internal.ItemType;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.api.dto.ServiceCall;
import org.openhab.binding.hasslink.internal.entity.ChannelSpec;
import org.openhab.binding.hasslink.internal.entity.EntityContext;
import org.openhab.binding.hasslink.internal.entity.EntityType;
import org.openhab.binding.hasslink.internal.entity.ParsedData;
import org.openhab.binding.hasslink.internal.entity.util.ChannelSpecsBuilder;
import org.openhab.binding.hasslink.internal.entity.util.CommandMapper;
import org.openhab.binding.hasslink.internal.entity.util.OptionUtils;
import org.openhab.binding.hasslink.internal.entity.util.StateMapBuilder;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescriptionFragment;

/**
 * The {@link MediaPlayerEntity} class represents a media player entity in Home Assistant.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class MediaPlayerEntity implements EntityType {

    // Home Assistant media player feature flags
    private static final long VOLUME_SET = 4L;
    private static final long VOLUME_MUTE = 8L;
    private static final long TURN_ON = 128L;
    private static final long TURN_OFF = 256L;
    private static final long SELECT_SOURCE = 2048L;
    private static final long STOP = 4096L;
    private static final long SHUFFLE_SET = 32768L;
    private static final long SELECT_SOUND_MODE = 65536L;
    private static final long REPEAT_SET = 262144L;

    @Override
    public String getType() {
        return "media_player";
    }

    @Override
    public List<ChannelSpec> getChannelSpecs(EntityState entityState, EntityContext context) {
        return ChannelSpecsBuilder.create(entityState, context) //
                .addPrimaryChannel(ItemType.STRING) //
                .add("player", ItemType.PLAYER) // a virtual "attribute"
                .addIfSupported(STOP, "stop", ItemType.SWITCH) // a virtual "attribute" for stop command
                .addAttrIfSupported(TURN_ON | TURN_OFF, "power", ItemType.SWITCH) //
                .addAttrIfSupported(VOLUME_SET, "volume_level", ItemType.DIMMER) //
                .addAttrIfSupported(VOLUME_MUTE, "is_volume_muted", ItemType.SWITCH) //
                .addAttrIfSupported(SELECT_SOURCE, "source", ItemType.STRING) //
                .addAttrIfSupported(SELECT_SOUND_MODE, "sound_mode", ItemType.STRING) //
                .addAttrIfSupported(SHUFFLE_SET, "shuffle", ItemType.SWITCH) //
                .addAttrIfSupported(REPEAT_SET, "repeat", ItemType.STRING) //
                .addAttr("media_title", ItemType.STRING) //
                .addAttr("media_artist", ItemType.STRING) //
                .build();
    }

    @Override
    public @Nullable StateDescriptionFragment getStateDescriptionFragment(EntityState entityState, String property,
            EntityContext context) {
        if ("source".equals(property)) {
            return OptionUtils.extractStateOptions(entityState, "source_list", val -> val);
        } else if ("sound_mode".equals(property)) {
            return OptionUtils.extractStateOptions(entityState, "sound_mode_list", val -> val);
        }
        return null;
    }

    @Override
    public Map<String, ParsedData> parseState(EntityState entityState, EntityContext context) {
        StateMapBuilder builder = StateMapBuilder.create(entityState, context);

        String rawState = entityState.state();
        builder.putPrimaryStringState();
        builder.put("power", "off".equalsIgnoreCase(rawState) ? OnOffType.OFF : OnOffType.ON);

        // Stop is a momentary trigger switch; keep state OFF
        builder.put("stop", OnOffType.OFF);

        builder.putPlayer("player") //
                .putScaledPercent("volume_level", 0.0, 1.0) //
                .putOnOff("is_volume_muted") //
                .putString("source") //
                .putString("sound_mode") //
                .putOnOff("shuffle") //
                .putString("repeat") //
                .putString("media_title") //
                .putString("media_artist");

        return builder.build();
    }

    @Override
    public Optional<ServiceCall> toServiceCall(String entityId, String attribute, Command command,
            @Nullable EntityState entityState, EntityContext context) {

        switch (attribute) {
            case "player":
                String service = switch (command) {
                    case PlayPauseType.PLAY -> "media_play";
                    case PlayPauseType.PAUSE -> "media_pause";
                    case NextPreviousType.NEXT, RewindFastforwardType.FASTFORWARD -> "media_next_track";
                    case NextPreviousType.PREVIOUS, RewindFastforwardType.REWIND -> "media_previous_track";
                    default -> null;
                };

                if (service != null) {
                    return Optional.of(new ServiceCall("media_player", service, entityId));
                }
                break;
            case "stop":
                if (command instanceof OnOffType onOff && onOff == OnOffType.ON) {
                    return Optional.of(new ServiceCall("media_player", "media_stop", entityId));
                }
                break;
            case "power":
                return CommandMapper.onOff(command, "media_player", entityId);
            case "volume_level":
                return CommandMapper.onPercentScaled(command, "media_player", "volume_set", "volume_level", entityId,
                        0.0, 1.0);
            case "is_volume_muted":
                return CommandMapper.onOffParam(command, "media_player", "volume_mute", "is_volume_muted", entityId);
            case "source":
                return CommandMapper.onString(command, "media_player", "select_source", "source", entityId);
            case "sound_mode":
                return CommandMapper.onString(command, "media_player", "select_sound_mode", "sound_mode", entityId);
            case "shuffle":
                return CommandMapper.onOffParam(command, "media_player", "shuffle_set", "shuffle", entityId);
            case "repeat":
                return CommandMapper.onString(command, "media_player", "repeat_set", "repeat", entityId);
        }

        return Optional.empty();
    }
}

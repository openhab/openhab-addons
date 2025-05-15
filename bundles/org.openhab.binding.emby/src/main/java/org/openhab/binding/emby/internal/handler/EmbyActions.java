/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.emby.internal.handler;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * The {@link EmbyActions} is responsible for handling the actions which can be sent to {@link EmbyDeviceHandler}.
 * 
 * @author Zachary Christiansen - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = EmbyActions.class)
@ThingActionsScope(name = "emby")
@NonNullByDefault
public class EmbyActions implements ThingActions {

    public enum EmbyPlayCommand {
        PlayNow,
        PlayNext,
        PlayLast
    }

    public enum EmbyGeneralCommand {
        MoveUp,
        MoveDown,
        MoveLeft,
        MoveRight,
        PageUp,
        PageDown,
        PreviousLetter,
        NextLetter,
        ToggleOsdMenu,
        ToggleContextMenu,
        ToggleMute,
        Select,
        Back,
        TakeScreenshot,
        GoHome,
        GoToSettings,
        VolumeUp,
        VolumeDown,
        ToggleFullscreen,
        GoToSearch
    }

    public enum EmbyCommandWithArgs {
        SetVolume,
        SetAudioStreamIndex,
        SetSubtitleStreamIndex,
        DisplayContent,
        PlayTrailers,
        SendString,
        DisplayMessage,
        SetPlaybackRate,
        SetSubtitleOffset,
        IncrementSubtitleOffset
    }

    private @Nullable EmbyDeviceHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (EmbyDeviceHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/action.emby.SendPlay.label", description = "@text/action.emby.SendPlay.desc")
    public void sendPlay(
            @ActionInput(name = "itemIds", label = "@text/action.emby.SendPlay.input.itemIds.label", description = "@text/action.emby.SendPlay.input.itemIds.desc") String itemIds,
            @ActionInput(name = "playCommand", label = "@text/action.emby.SendPlay.input.playCommand.label", description = "@text/action.emby.SendPlay.input.playCommand.desc") EmbyPlayCommand playCommand,
            @ActionInput(name = "startPositionTicks", label = "@text/action.emby.SendPlay.input.startPositionTicks.label", description = "@text/action.emby.SendPlay.input.startPositionTicks.desc") @Nullable Integer startPositionTicks,
            @ActionInput(name = "mediaSourceId", label = "@text/action.emby.SendPlay.input.mediaSourceId.label", description = "@text/action.emby.SendPlay.input.mediaSourceId.desc") @Nullable String mediaSourceId,
            @ActionInput(name = "audioStreamIndex", label = "@text/action.emby.SendPlay.input.audioStreamIndex.label", description = "@text/action.emby.SendPlay.input.audioStreamIndex.desc") @Nullable Integer audioStreamIndex,
            @ActionInput(name = "subtitleStreamIndex", label = "@text/action.emby.SendPlay.input.subtitleStreamIndex.label", description = "@text/action.emby.SendPlay.input.subtitleStreamIndex.desc") @Nullable Integer subtitleStreamIndex,
            @ActionInput(name = "startIndex", label = "@text/action.emby.SendPlay.input.startIndex.label", description = "@text/action.emby.SendPlay.input.startIndex.desc") @Nullable Integer startIndex) {
        requireNonNull(handler, "EmbyDeviceHandler not set").sendPlayWithParams(itemIds, playCommand.name(),
                startPositionTicks, mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex);
    }

    public static void sendPlay(ThingActions actions, String itemIds, EmbyPlayCommand playCommand,
            @Nullable Integer startPositionTicks, @Nullable String mediaSourceId, @Nullable Integer audioStreamIndex,
            @Nullable Integer subtitleStreamIndex, @Nullable Integer startIndex) {
        if (actions instanceof EmbyActions) {
            ((EmbyActions) actions).sendPlay(requireNonNull(itemIds), requireNonNull(playCommand), startPositionTicks,
                    mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex);
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }

    @RuleAction(label = "@text/action.emby.SendGeneralCommand.label", description = "@text/action.emby.SendGeneralCommand.desc")
    public void sendGeneralCommand(
            @ActionInput(name = "commandName", label = "@text/action.emby.SendGeneralCommand.input.commandName.label", description = "@text/action.emby.SendGeneralCommand.input.commandName.desc") EmbyGeneralCommand commandName) {
        requireNonNull(handler, "EmbyDeviceHandler not set").sendGeneralCommand(commandName.name());
    }

    public static void sendGeneralCommand(ThingActions actions, EmbyGeneralCommand commandName) {
        if (actions instanceof EmbyActions embyActions) {
            embyActions.sendGeneralCommand(commandName);
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }

    @RuleAction(label = "@text/action.emby.SendGeneralCommandWithArgs.label", description = "@text/action.emby.SendGeneralCommandWithArgs.desc")
    public void sendGeneralCommandWithArgs(
            @ActionInput(name = "commandName", label = "@text/action.emby.SendGeneralCommandWithArgs.input.commandName.label", description = "@text/action.emby.SendGeneralCommandWithArgs.input.commandName.desc") EmbyCommandWithArgs commandName,
            @ActionInput(name = "jsonArguments", label = "@text/action.emby.SendGeneralCommandWithArgs.input.jsonArguments.label", description = "@text/action.emby.SendGeneralCommandWithArgs.input.jsonArguments.desc") String jsonArguments) {
        requireNonNull(handler, "EmbyDeviceHandler not set").sendGeneralCommandWithArgs(commandName.name(),
                jsonArguments);
    }

    public static void sendGeneralCommandWithArgs(ThingActions actions, EmbyCommandWithArgs commandName,
            String jsonArguments) {
        if (actions instanceof EmbyActions embyActions) {
            embyActions.sendGeneralCommandWithArgs(requireNonNull(commandName), requireNonNull(jsonArguments));
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }
}

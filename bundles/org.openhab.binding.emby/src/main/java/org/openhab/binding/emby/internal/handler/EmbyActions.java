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
import static org.openhab.binding.emby.internal.EmbyBindingConstants.*;

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

    /**
     * Allowed play commands.
     */
    public enum EmbyPlayCommand {
        PlayNow,
        PlayNext,
        PlayLast
    }

    /**
     * Allowed general commands (no arguments).
     */
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

    /**
     * Allowed commands with arguments.
     */
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

    @RuleAction(label = "@text/emby.action.sendPlay.label", description = "@text/emby.action.sendPlay.desc")
    public void sendPlay(
            @ActionInput(name = "@text/emby.action.sendPlay.input.itemIds.name", label = "@text/emby.action.sendPlay.input.itemIds.label", description = "@text/emby.action.sendPlay.input.itemIds.desc") String itemIds,
            @ActionInput(name = "@text/emby.action.sendPlay.input.playCommand.name", label = "@text/emby.action.sendPlay.input.playCommand.label", description = "@text/emby.action.sendPlay.input.playCommand.desc") EmbyPlayCommand playCommand,
            @ActionInput(name = "@text/emby.action.sendPlay.input.startPositionTicks.name", label = "@text/emby.action.sendPlay.input.startPositionTicks.label", description = "@text/emby.action.sendPlay.input.startPositionTicks.desc") @Nullable Integer startPositionTicks,
            @ActionInput(name = "@text/emby.action.sendPlay.input.mediaSourceId.name", label = "@text/emby.action.sendPlay.input.mediaSourceId.label", description = "@text/emby.action.sendPlay.input.mediaSourceId.desc") @Nullable String mediaSourceId,
            @ActionInput(name = "@text/emby.action.sendPlay.input.audioStreamIndex.name", label = "@text/emby.action.sendPlay.input.audioStreamIndex.label", description = "@text/emby.action.sendPlay.input.audioStreamIndex.desc") @Nullable Integer audioStreamIndex,
            @ActionInput(name = "@text/emby.action.sendPlay.input.subtitleStreamIndex.name", label = "@text/emby.action.sendPlay.input.subtitleStreamIndex.label", description = "@text/emby.action.sendPlay.input.subtitleStreamIndex.desc") @Nullable Integer subtitleStreamIndex,
            @ActionInput(name = "@text/emby.action.sendPlay.input.startIndex.name", label = "@text/emby.action.sendPlay.input.startIndex.label", description = "@text/emby.action.sendPlay.input.startIndex.desc") @Nullable Integer startIndex) {
        if (handler == null) {
            throw new IllegalStateException("EmbyDeviceHandler not set");
        }
        handler.sendPlayWithParams(itemIds, playCommand.name(), startPositionTicks, mediaSourceId, audioStreamIndex,
                subtitleStreamIndex, startIndex);
    }

    public static void sendPlay(ThingActions actions, String itemIds, EmbyPlayCommand playCommand,
            @Nullable Integer startPositionTicks, @Nullable String mediaSourceId, @Nullable Integer audioStreamIndex,
            @Nullable Integer subtitleStreamIndex, @Nullable Integer startIndex) {
        if (actions instanceof EmbyActions) {
            // itemIds and playCommand are required, so cast away @Nullable:
            ((EmbyActions) actions).sendPlay(requireNonNull(itemIds), requireNonNull(playCommand), startPositionTicks,
                    mediaSourceId, audioStreamIndex, subtitleStreamIndex, startIndex);
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }

    // ––– 2) General Command –––
    @RuleAction(label = "@text/emby.action.generalCommand.label", description = "@text/emby.action.generalCommand.desc")
    public void sendGeneralCommand(
            @ActionInput(name = "@text/emby.action.generalCommand.input.commandName.name", label = "@text/emby.action.generalCommand.input.commandName.label", description = "@text/emby.action.generalCommand.input.commandName.desc") EmbyGeneralCommand commandName) {
        if (handler == null) {
            throw new IllegalStateException("EmbyDeviceHandler is not set");
        }
        handler.sendGeneralCommand(commandName.name());
    }

    public static void sendGeneralCommand(ThingActions actions, EmbyGeneralCommand commandName) {
        if (actions instanceof EmbyActions) {
            ((EmbyActions) actions).sendGeneralCommand(commandName);
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }

    // ––– 3) General Command with Arguments –––
    @RuleAction(label = "@text/emby.action.generalCommandWithArgs.label", description = "@text/emby.action.generalCommandWithArgs.desc")
    public void sendGeneralCommandWithArgs(
            @ActionInput(name = "@text/emby.action.generalCommandWithArgs.input.name.name", label = "@text/emby.action.generalCommandWithArgs.input.name.label", description = "@text/emby.action.generalCommandWithArgs.input.name.desc") EmbyCommandWithArgs commandName,
            @ActionInput(name = "@text/emby.action.generalCommandWithArgs.input.args.name", label = "@text/emby.action.generalCommandWithArgs.input.args.label", description = "@text/emby.action.generalCommandWithArgs.input.args.desc") String jsonArguments) {
        if (handler == null) {
            throw new IllegalStateException("EmbyDeviceHandler is not set");
        }
        handler.sendGeneralCommandWithArgs(commandName.name(), jsonArguments);
    }

    public static void sendGeneralCommandWithArgs(ThingActions actions, EmbyCommandWithArgs commandName,
            String jsonArguments) {
        if (actions instanceof EmbyActions) {
            ((EmbyActions) actions).sendGeneralCommandWithArgs(requireNonNull(commandName),
                    requireNonNull(jsonArguments));
        } else {
            throw new IllegalArgumentException("Not an EmbyActions instance");
        }
    }
}

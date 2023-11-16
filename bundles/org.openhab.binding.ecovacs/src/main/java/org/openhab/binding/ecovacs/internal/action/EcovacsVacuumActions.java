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
package org.openhab.binding.ecovacs.internal.action;

import static org.openhab.binding.ecovacs.internal.EcovacsBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.commands.PlaySoundCommand;
import org.openhab.binding.ecovacs.internal.handler.EcovacsVacuumHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Danny Baumann - Initial contribution
 */
@ThingActionsScope(name = "ecovacs")
@NonNullByDefault
public class EcovacsVacuumActions implements ThingActions {
    private final Logger logger = LoggerFactory.getLogger(EcovacsVacuumActions.class);
    private @Nullable EcovacsVacuumHandler handler;

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (EcovacsVacuumHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }

    @RuleAction(label = "@text/playSoundActionLabel", description = "@text/playSoundActionDesc")
    public void playSound(
            @ActionInput(name = "type", label = "@text/actionInputSoundTypeLabel", description = "@text/actionInputSoundTypeDesc") String type) {
        EcovacsVacuumHandler handler = this.handler;
        if (handler != null) {
            Optional<PlaySoundCommand.SoundType> soundType = SOUND_TYPE_MAPPING.findMappedEnumValue(type);
            if (soundType.isPresent()) {
                handler.playSound(new PlaySoundCommand(soundType.get()));
            } else {
                logger.debug("Sound type '{}' is unknown, ignoring", type);
            }
        }
    }

    @RuleAction(label = "@text/playSoundActionLabel", description = "@text/playSoundActionDesc")
    public void playSoundWithId(
            @ActionInput(name = "soundId", label = "@text/actionInputSoundIdLabel", description = "@text/actionInputSoundIdDesc") int soundId) {
        EcovacsVacuumHandler handler = this.handler;
        if (handler != null) {
            handler.playSound(new PlaySoundCommand(soundId));
        }
    }

    public static void playSound(@Nullable ThingActions actions, String type) {
        if (actions instanceof EcovacsVacuumActions action) {
            action.playSound(type);
        }
    }

    public static void playSoundWithId(@Nullable ThingActions actions, int soundId) {
        if (actions instanceof EcovacsVacuumActions action) {
            action.playSoundWithId(soundId);
        }
    }
}

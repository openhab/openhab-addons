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
package org.openhab.binding.mielecloud.internal.handler.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * Wrapper for {@link ActionsState} handling the type conversion to {@link State} for directly filling channels.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ActionsChannelState {
    private final ActionsState actions;

    public ActionsChannelState(ActionsState actions) {
        this.actions = actions;
    }

    public State getRemoteControlCanBeSwitchedOn() {
        return OnOffType.from(actions.canBeSwitchedOn());
    }

    public State getRemoteControlCanBeSwitchedOff() {
        return OnOffType.from(actions.canBeSwitchedOff());
    }

    public State getLightCanBeControlled() {
        return OnOffType.from(actions.canControlLight());
    }

    public State getSuperCoolCanBeControlled() {
        return OnOffType.from(actions.canContolSupercooling());
    }

    public State getSuperFreezeCanBeControlled() {
        return OnOffType.from(actions.canControlSuperfreezing());
    }

    public State getRemoteControlCanBeStarted() {
        return OnOffType.from(actions.canBeStarted());
    }

    public State getRemoteControlCanBeStopped() {
        return OnOffType.from(actions.canBeStopped());
    }

    public State getRemoteControlCanBePaused() {
        return OnOffType.from(actions.canBePaused());
    }

    public State getRemoteControlCanSetProgramActive() {
        return OnOffType.from(actions.canSetActiveProgramId());
    }
}

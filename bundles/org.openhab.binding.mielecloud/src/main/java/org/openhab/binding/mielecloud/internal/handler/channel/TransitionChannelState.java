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
package org.openhab.binding.mielecloud.internal.handler.channel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.TransitionState;
import org.openhab.core.types.State;

/**
 * Wrapper for {@link TransitionState} handling the type conversion to {@link State} for directly filling channels.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class TransitionChannelState {
    private final TransitionState transition;

    public TransitionChannelState(TransitionState transition) {
        this.transition = transition;
    }

    public boolean hasFinishedChanged() {
        return transition.hasFinishedChanged();
    }

    public State getFinishState() {
        return ChannelTypeUtil.booleanToState(transition.isFinished());
    }

    public State getProgramRemainingTime() {
        return ChannelTypeUtil.intToState(transition.getRemainingTime());
    }

    public State getProgramProgress() {
        return ChannelTypeUtil.intToState(transition.getProgress());
    }
}

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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.INVERT_UID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inverts a {@link Command} or {@link State}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class InvertStateProfile implements StateProfile {

    private final Logger logger = LoggerFactory.getLogger(InvertStateProfile.class);

    private final ProfileCallback callback;

    public InvertStateProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return INVERT_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        // do nothing
    }

    @Override
    public void onCommandFromItem(Command command) {
        callback.handleCommand((Command) invert(command));
    }

    @Override
    public void onCommandFromHandler(Command command) {
        callback.sendCommand((Command) invert(command));
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        callback.sendUpdate((State) invert(state));
    }

    private Type invert(Type type) {
        if (type instanceof UnDefType) {
            // we cannot invert UNDEF or NULL values, thus we simply return them without reporting an error or warning
            return type;
        }

        if (type instanceof QuantityType<?> qtState) {
            return qtState.negate();
        } else if (type instanceof PercentType ptState) {
            return new PercentType(100 - ptState.intValue());
        } else if (type instanceof DecimalType dtState) {
            return new DecimalType(-1 * dtState.doubleValue());
        } else if (type instanceof IncreaseDecreaseType) {
            return IncreaseDecreaseType.INCREASE.equals(type) ? IncreaseDecreaseType.DECREASE
                    : IncreaseDecreaseType.INCREASE;
        } else if (type instanceof NextPreviousType) {
            return NextPreviousType.NEXT.equals(type) ? NextPreviousType.PREVIOUS : NextPreviousType.NEXT;
        } else if (type instanceof OnOffType) {
            return OnOffType.ON.equals(type) ? OnOffType.OFF : OnOffType.ON;
        } else if (type instanceof OpenClosedType) {
            return OpenClosedType.OPEN.equals(type) ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
        } else if (type instanceof PlayPauseType) {
            return PlayPauseType.PLAY.equals(type) ? PlayPauseType.PAUSE : PlayPauseType.PLAY;
        } else if (type instanceof RewindFastforwardType) {
            return RewindFastforwardType.REWIND.equals(type) ? RewindFastforwardType.FASTFORWARD
                    : RewindFastforwardType.REWIND;
        } else if (type instanceof StopMoveType) {
            return StopMoveType.MOVE.equals(type) ? StopMoveType.STOP : StopMoveType.MOVE;
        } else if (type instanceof UpDownType) {
            return UpDownType.UP.equals(type) ? UpDownType.DOWN : UpDownType.UP;
        } else {
            logger.warn("Invert cannot be applied to the type of class '{}'. Returning original type.",
                    type.getClass());
            return type;
        }
    }
}

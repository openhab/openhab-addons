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
package org.openhab.binding.loxone.internal.controls;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loxone Garage/Gate control with position, movement and prevention feedback and a partial-opening action.
 * Position is inverted from Loxone's 0 (closed) to 1 (open) to openHAB's 0% (open) to 100% (closed).
 * Directional commands require Miniserver 12.1; partial opening requires 14.2.
 *
 * @author Robert Pollai - Initial contribution
 */
@NonNullByDefault
class LxControlGate extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(@Nullable LxUuid uuid) {
            return new LxControlGate(requireNonNull(uuid));
        }

        @Override
        String getType() {
            return "gate";
        }
    }

    private static final String STATE_POSITION = "position";
    private static final String STATE_ACTIVE = "active";
    private static final String STATE_PREVENT_OPEN = "preventopen";
    private static final String STATE_PREVENT_CLOSE = "preventclose";
    private static final String CMD_OPEN = "forceOpen";
    private static final String CMD_CLOSE = "forceClose";
    private static final String CMD_STOP = "stop";
    private static final String CMD_PARTIALLY_OPEN = "PartiallyOpen";

    private final Logger logger = LoggerFactory.getLogger(LxControlGate.class);

    private LxControlGate(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(@Nullable LxControlConfig config) {
        super.initialize(requireNonNull(config));
        addChannel("Rollershutter", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_GATE_POSITION),
                defaultChannelLabel, "Garage/Gate position", tags, this::handleOperateCommands, this::getPositionState);
        addChannel("String", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_GATE_MOVEMENT),
                defaultChannelLabel + " / Movement", "Garage/Gate movement", tags, null, this::getMovementState);
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + " / Opening Prevented", "Garage/Gate opening prevented", tags, null,
                () -> getPreventionState(STATE_PREVENT_OPEN));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RO_SWITCH),
                defaultChannelLabel + " / Closing Prevented", "Garage/Gate closing prevented", tags, null,
                () -> getPreventionState(STATE_PREVENT_CLOSE));
        addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_GATE_PARTIALLY_OPEN),
                defaultChannelLabel + " / Partially Open", "Move Garage/Gate to its Loxone partial-open position", tags,
                this::handlePartiallyOpenCommands, () -> OnOffType.OFF);
    }

    private void handleOperateCommands(Command command) throws IOException {
        if (command instanceof PercentType) {
            if (PercentType.ZERO.equals(command)) {
                sendAction(CMD_OPEN);
            } else if (PercentType.HUNDRED.equals(command)) {
                sendAction(CMD_CLOSE);
            } else {
                logger.debug("Gate {} does not support target position {}: only 0% and 100% are supported", uuid,
                        command);
            }
        } else if (command instanceof UpDownType) {
            // Unlike open/close, forceOpen/forceClose stop existing movement before moving in the requested direction.
            sendAction(command == UpDownType.UP ? CMD_OPEN : CMD_CLOSE);
        } else if (command == StopMoveType.STOP) {
            sendAction(CMD_STOP);
        }
    }

    private void handlePartiallyOpenCommands(Command command) throws IOException {
        if (command == OnOffType.ON) {
            sendAction(CMD_PARTIALLY_OPEN);
        }
    }

    private State getPositionState() {
        Double value = getStateDoubleValue(STATE_POSITION);
        if (value != null && value >= 0 && value <= 1) {
            return new PercentType((int) Math.round((1.0 - value) * 100.0));
        }
        return UnDefType.UNDEF;
    }

    private State getMovementState() {
        Double value = getStateDoubleValue(STATE_ACTIVE);
        if (value != null) {
            if (value == 1.0) {
                return new StringType("OPENING");
            } else if (value == 0.0) {
                return new StringType("STOPPED");
            } else if (value == -1.0) {
                return new StringType("CLOSING");
            }
        }
        return UnDefType.UNDEF;
    }

    private State getPreventionState(String name) {
        Double value = getStateDoubleValue(name);
        if (value != null && (value == 0.0 || value == 1.0)) {
            return OnOffType.from(value == 1.0);
        }
        return UnDefType.UNDEF;
    }
}

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

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

/**
 * A window type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a window control covers the Window block function with the following states:
 * <ul>
 * <li>{@code position} - 0 fully closed to 1 fully open</li>
 * <li>{@code direction} - -1 closing, 0 stopped, 1 opening</li>
 * <li>{@code lockedReason} - name of the input blocking the window</li>
 * <li>{@code targetPosition} - target position, available since Miniserver 11.3</li>
 * </ul>
 * <p>
 * The control exposes one rollershutter channel with the window position. Percentages follow the openHAB convention
 * by default, i.e. 0% is fully open and 100% is fully closed, while Loxone uses the opposite one. The channel's
 * {@code inverted} configuration reverses that mapping for installations that prefer Loxone's own convention.
 * <p>
 * The {@code stop} command requires Miniserver 11.3 or newer. Older firmware ignores it.
 *
 * @author Robert Pollai - Initial contribution
 *
 */
class LxControlWindow extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlWindow(uuid);
        }

        @Override
        String getType() {
            return "window";
        }
    }

    /**
     * The position of the window, a number from 0 to 1
     * Window fully closed = 0
     * Window fully open = 1
     */
    private static final String STATE_POSITION = "position";
    /**
     * Command string used to open the window completely
     */
    private static final String CMD_FULL_OPEN = "fullopen";
    /**
     * Command string used to close the window completely
     */
    private static final String CMD_FULL_CLOSE = "fullclose";
    /**
     * Command string used to stop the window movement, available since Miniserver 11.3
     */
    private static final String CMD_STOP = "stop";
    /**
     * Command string used to move the window to a position from 0 (closed) to 100 (open)
     */
    private static final String CMD_MOVE_TO_POSITION = "moveToPosition/";
    /**
     * Channel configuration parameter switching the percentage convention
     */
    private static final String CONFIG_INVERTED = "inverted";

    private ChannelUID positionChannelId;

    private LxControlWindow(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config);
        Set<String> windowTags = new HashSet<>(tags);
        Configuration channelConfig = new Configuration();
        channelConfig.put(CONFIG_INVERTED, false);
        positionChannelId = addChannel("Rollershutter",
                new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_WINDOW_POSITION), defaultChannelLabel,
                "Window position", windowTags, channelConfig, this::handleOperateCommands, this::getOperateState);
    }

    private void handleOperateCommands(Command command) throws IOException {
        boolean inverted = isInverted();
        if (command instanceof PercentType percentCommand) {
            int percent = Math.max(0, Math.min(100, (int) Math.round(percentCommand.doubleValue())));
            if (percent == 0) {
                sendAction(inverted ? CMD_FULL_CLOSE : CMD_FULL_OPEN);
            } else if (percent == 100) {
                sendAction(inverted ? CMD_FULL_OPEN : CMD_FULL_CLOSE);
            } else {
                sendAction(CMD_MOVE_TO_POSITION + (inverted ? percent : 100 - percent));
            }
        } else if (command instanceof UpDownType upDownCommand) {
            // UP always means moving towards 0%, which is either fully open or fully closed depending on the convention
            boolean open = (upDownCommand == UpDownType.UP) != inverted;
            sendAction(open ? CMD_FULL_OPEN : CMD_FULL_CLOSE);
        } else if (command instanceof StopMoveType stopMoveCommand) {
            if (stopMoveCommand == StopMoveType.STOP) {
                sendAction(CMD_STOP);
            }
        }
    }

    private State getOperateState() {
        Double value = getStateDoubleValue(STATE_POSITION);
        if (value != null && value >= 0 && value <= 1) {
            // round the result of the multiplication to avoid skipping percentages, see LxControlJalousie
            double percent = (isInverted() ? value : 1.0 - value) * 100.0;
            return new PercentType((int) Math.round(percent));
        }
        return null;
    }

    /**
     * Checks whether percentages of the window position channel are expressed in Loxone's own convention.
     *
     * @return true if 0% means fully closed and 100% means fully open
     */
    private boolean isInverted() {
        Configuration channelConfig = positionChannelId != null ? getChannelConfiguration(positionChannelId) : null;
        if (channelConfig == null) {
            return false;
        }
        Object value = channelConfig.get(CONFIG_INVERTED);
        if (value instanceof Boolean inverted) {
            return inverted;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }
}

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
package org.openhab.binding.loxone.internal.controls;

import static org.openhab.binding.loxone.internal.LxBindingConstants.*;

import java.io.IOException;

import org.openhab.binding.loxone.internal.types.LxUuid;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;

/**
 * An UpDownDigital type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, UpDownDigital control is a virtual input that is digital and has an input type
 * up-down buttons. Buttons act like on an integrated up/down arrows switch - only one direction can be active at a
 * time. Pushing button in one direction will automatically set the other direction to off.
 * This control has no states and can only accept commands. Only up/down on/off commands are generated. Pulse
 * commands are not supported, because of lack of corresponding feature in openHAB. Pulse can be emulated by quickly
 * alternating between ON and OFF commands. Because this control has no states, there will be no openHAB state changes
 * triggered by the Miniserver and we need to take care of updating the states inside this class.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlUpDownDigital extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlUpDownDigital(uuid);
        }

        @Override
        String getType() {
            return "updowndigital";
        }
    }

    private static final String CMD_UP_ON = "UpOn";
    private static final String CMD_UP_OFF = "UpOff";
    private static final String CMD_DOWN_ON = "DownOn";
    private static final String CMD_DOWN_OFF = "DownOff";

    private OnOffType upState = OnOffType.OFF;
    private OnOffType downState = OnOffType.OFF;
    private ChannelUID upChannelId;
    private ChannelUID downChannelId;

    LxControlUpDownDigital(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        initialize(config, " / Up", "Up/Down Digital: Up", " / Down", "Up/Down Digital: Down");
    }

    void initialize(LxControlConfig config, String upChannelLabel, String upChannelDescription, String downChannelLabel,
            String downChannelDescription) {
        super.initialize(config);
        upChannelId = addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + upChannelLabel, upChannelDescription, tags, this::handleUpCommands,
                () -> upState);
        downChannelId = addChannel("Switch", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_SWITCH),
                defaultChannelLabel + downChannelLabel, downChannelDescription, tags, this::handleDownCommands,
                () -> downState);
    }

    private void handleUpCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON && upState == OnOffType.OFF) {
                setStates(OnOffType.ON, OnOffType.OFF);
                sendAction(CMD_UP_ON);
            } else if (upState == OnOffType.ON) {
                setStates(OnOffType.OFF, OnOffType.OFF);
                sendAction(CMD_UP_OFF);
            }
        }
    }

    private void handleDownCommands(Command command) throws IOException {
        if (command instanceof OnOffType onOffCommand) {
            if (onOffCommand == OnOffType.ON && downState == OnOffType.OFF) {
                setStates(OnOffType.OFF, OnOffType.ON);
                sendAction(CMD_DOWN_ON);
            } else if (downState == OnOffType.ON) {
                setStates(OnOffType.OFF, OnOffType.OFF);
                sendAction(CMD_DOWN_OFF);
            }
        }
    }

    private void setStates(OnOffType upState, OnOffType downState) {
        this.upState = upState;
        this.downState = downState;
        setChannelState(upChannelId, upState);
        setChannelState(downChannelId, downState);
    }
}

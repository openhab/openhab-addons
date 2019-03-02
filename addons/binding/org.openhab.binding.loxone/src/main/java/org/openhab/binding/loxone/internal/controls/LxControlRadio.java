/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateOption;
import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * A radio-button type of control on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlRadio extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
                LxCategory category) {
            return new LxControlRadio(handlerApi, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * Number of outputs a radio controller may have
     */
    public static final int MAX_RADIO_OUTPUTS = 16;

    /**
     * A name by which Miniserver refers to radio-button controls
     */
    private static final String TYPE_NAME = "radio";

    /**
     * Radio-button has one state that is a number representing current active output
     */
    private static final String STATE_ACTIVE_OUTPUT = "activeoutput";

    /**
     * Command string used to set radio button to all outputs off
     */
    private static final String CMD_RESET = "reset";

    private List<StateOption> outputs = new ArrayList<>();

    /**
     * Create radio-button control object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       radio-button's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which radio-button belongs
     * @param category   category to which control belongs
     */
    LxControlRadio(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
        // add both channel and state description (all needed configuration is available)
        addChannel("Number", new ChannelTypeUID(BINDING_ID, MINISERVER_CHANNEL_TYPE_RADIO_BUTTON), defaultChannelId,
                defaultChannelLabel, "Radio button", tags);
        addChannelStateDescription(defaultChannelId, new StateDescription(BigDecimal.ZERO,
                new BigDecimal(MAX_RADIO_OUTPUTS), BigDecimal.ONE, null, false, outputs));
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json     JSON describing the control as received from the Miniserver
     * @param room     New room that this control belongs to
     * @param category New category that this control belongs to
     */
    @Override
    public void update(LxJsonControl json, LxContainer room, LxCategory category) {
        super.update(json, room, category);
        if (json.details.outputs != null) {
            json.details.outputs.entrySet().stream().map(e -> new StateOption(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
        }
        if (json.details != null && json.details.allOff != null) {
            outputs.add(new StateOption("0", json.details.allOff));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelId, Command command) throws IOException {
        if (command instanceof OnOffType) {
            if ((OnOffType) command == OnOffType.OFF) {
                setOutput(0);
            }
        } else if (command instanceof DecimalType) {
            setOutput(((DecimalType) command).intValue());
        }
    }

    @Override
    public State getChannelState(ChannelUID channelId) {
        if (defaultChannelId.equals(channelId)) {
            Double output = getStateDoubleValue(STATE_ACTIVE_OUTPUT);
            if (output != null && output >= 0 && output <= MAX_RADIO_OUTPUTS) {
                return new DecimalType(output);
            }
        }
        return null;
    }

    /**
     * Set radio-button control's active output
     * <p>
     * Sends a command to operate the radio-button control.
     *
     * @param output output number to activate
     * @throws IOException when something went wrong with communication
     */
    private void setOutput(int output) throws IOException {
        if (output == 0) {
            sendAction(CMD_RESET);
        } else if (output >= 1 && output <= MAX_RADIO_OUTPUTS) {
            sendAction(Long.toString(output));
        }
    }
}

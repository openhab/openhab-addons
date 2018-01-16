/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * A radio-button type of control on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxControlRadio extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlRadio(client, uuid, json, room, category);
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
    private Map<String, String> outputs;

    /**
     * Create radio-button control object.
     *
     * @param client
     *            communication client used to send commands to the Miniserver
     * @param uuid
     *            radio-button's UUID
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            room to which radio-button belongs
     * @param category
     *            category to which control belongs
     */
    LxControlRadio(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    /**
     * Update Miniserver's control in runtime.
     *
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            New room that this control belongs to
     * @param category
     *            New category that this control belongs to
     */
    @Override
    void update(LxJsonControl json, LxContainer room, LxCategory category) {
        super.update(json, room, category);
        if (json.details.outputs != null) {
            outputs = new TreeMap<>(json.details.outputs);
        } else {
            outputs = new TreeMap<>();
        }
        if (json.details != null && json.details.allOff != null) {
            outputs.put("0", json.details.allOff);
        }
    }

    /**
     * Set radio-button control's active output
     * <p>
     * Sends a command to operate the radio-button control.
     *
     * @param output
     *            output number to activate
     * @throws IOException
     *             when something went wrong with communication
     */
    public void setOutput(int output) throws IOException {
        if (output == 0) {
            socketClient.sendAction(uuid, CMD_RESET);
        } else if (output >= 1 && output <= MAX_RADIO_OUTPUTS) {
            socketClient.sendAction(uuid, Long.toString(output));
        }
    }

    /**
     * Get current active output of a radio-button control
     *
     * @return
     *         active output number 1-8 (or 1-16), or 0 if all outputs are off, or null if error occured
     */
    public Integer getActiveOutput() {
        Double value = getStateValue(STATE_ACTIVE_OUTPUT);
        if (value != null) {
            return value.intValue();
        }
        return null;
    }

    /**
     * Get mapping between output numbers and output names
     *
     * @return
     *         map where key is output number 1-8 (or 1-16) or 0 for no active outputs and value is corresponding name
     *         (never returns null)
     */
    public Map<String, String> getOutputs() {
        return outputs;
    }
}

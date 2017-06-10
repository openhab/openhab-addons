/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.loxone.core;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * A radio-button type of control on Loxone Miniserver.
 *
 * @author Pawel Pieczul - initial commit
 *
 */
public class LxControlRadio extends LxControl {

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
     * @param name
     *            radio-button's name
     * @param room
     *            room to which radio-button belongs
     * @param category
     *            category to which radio-button belongs
     * @param states
     *            radio-button's states and their names (expecting one object with "activeOutput" name)
     * @param outputs
     *            a map of output names with output number as a key (1-8 or 1-16) or 0 for no active outputs and
     *            corresponding name (not all outputs need to be defined and names can be null)
     * @param noOutputs
     *            a text description of a situation where no output is active (can be null)
     */
    LxControlRadio(LxWsClient client, LxUuid uuid, String name, LxContainer room, LxCategory category,
            Map<String, LxControlState> states, Map<String, String> outputs, String noOutputs) {
        super(client, uuid, name, room, category, states, TYPE_NAME);
        if (outputs != null) {
            this.outputs = new TreeMap<String, String>(outputs);
        } else {
            this.outputs = new TreeMap<String, String>();
        }
        this.outputs.put("0", noOutputs);
    }

    /**
     * Check if control accepts provided type name from the Miniserver
     *
     * @param type
     *            name of the type received from Miniserver
     * @return
     *         true if this control is suitable for this type
     */
    public static boolean accepts(String type) {
        return type.toLowerCase().equals(TYPE_NAME);
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
     *         active output number 1-8 (or 1-16), or 0 if all outputs are off, or -1 if error occured
     */
    public int getActiveOutput() {
        LxControlState state = getState(STATE_ACTIVE_OUTPUT);
        if (state != null) {
            return (int) state.getValue();
        }
        return -1;
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

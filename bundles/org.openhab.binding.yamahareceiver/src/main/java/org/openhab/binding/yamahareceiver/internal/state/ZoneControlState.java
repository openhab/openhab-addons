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
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.VALUE_EMPTY;

/**
 * The state of a specific zone of a Yamaha receiver.
 *
 * @author David Graeff - Initial contribution
 */
public class ZoneControlState {
    public boolean power = false;
    // User visible name of the input channel for the current zone
    public String inputName = VALUE_EMPTY;
    // The ID of the input channel that is used as xml tags (for example NET_RADIO, HDMI_1).
    // This may differ from what the AVR returns in Input/Input_Sel ("NET RADIO", "HDMI1")
    public String inputID = VALUE_EMPTY;
    public String surroundProgram = VALUE_EMPTY;
    public float volumeDB = 0.0f; // volume in dB
    public boolean mute = false;
    public int dialogueLevel = 0;
    public boolean hdmi1Out = false;
    public boolean hdmi2Out = false;
}

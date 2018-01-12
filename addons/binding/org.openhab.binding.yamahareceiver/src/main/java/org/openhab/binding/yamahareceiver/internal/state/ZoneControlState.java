/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.VALUE_EMPTY;

/**
 * The state of a specific zone of a Yamaha receiver.
 *
 * @author David Graeff <david.graeff@web.de>
 *
 */
public class ZoneControlState {
    public boolean power = false;
    // User visible name of the input channel for the current zone
    public String inputName = VALUE_EMPTY;
    // The ID of the input channel that is used as xml tags (for example NET_RADIO, HDMI_1).
    // This may differ from what the AVR returns in Input/Input_Sel ("NET RADIO", "HDMI1")
    public String inputID = VALUE_EMPTY;
    public String surroundProgram = VALUE_EMPTY;
    public float volume = 0.0f; // volume in percent
    public boolean mute = false;
}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.state;

import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithPlayControlXML;

/**
 * The preset state containing the channel names and currently selected channel
 *
 * @author David Graeff - Initial contribution
 */
public class PresetInfoState implements Invalidateable {
    public int presetChannel = 0; // Used by NET_RADIO, RADIO, HD_RADIO, iPOD, USB, PC
    public String presetChannelNames[] = new String[InputWithPlayControlXML.PRESET_CHANNELS];
    public boolean presetChannelNamesChanged = false;

    public void invalidate() {
        presetChannel = 0;
    }
}

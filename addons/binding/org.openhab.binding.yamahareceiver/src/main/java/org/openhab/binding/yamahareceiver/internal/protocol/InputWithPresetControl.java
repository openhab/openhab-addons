/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * The preset control protocol interface
 *
 * @author David Graeff - Initial contribution
 */

public interface InputWithPresetControl extends IStateUpdateable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    public static Set<String> supportedInputs = Sets.newHashSet("TUNER", "NET_RADIO", "USB", "DOCK", "iPOD_USB", "PC",
            "Napster", "Pandora", "SIRIUS", "Rhapsody", "Bluetooth", "iPod", "HD_RADIO");

    /**
     * Select a preset channel.
     *
     * @param number The preset position [1,40]
     * @throws Exception
     */
    public void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException;
}

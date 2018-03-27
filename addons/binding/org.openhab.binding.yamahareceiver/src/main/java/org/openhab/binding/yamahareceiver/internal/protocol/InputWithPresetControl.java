/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

/**
 * The preset control protocol interface
 *
 * @author David Graeff - Initial contribution
 */

public interface InputWithPresetControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream.of(YamahaReceiverBindingConstants.INPUT_TUNER, "NET_RADIO", "NET RADIO",
            "USB", "DOCK", "iPOD_USB", "PC", "Napster", "Pandora", "SIRIUS", "Rhapsody",
            YamahaReceiverBindingConstants.INPUT_BLUETOOTH, "iPod", "HD_RADIO").collect(Collectors.toSet());

    /**
     * Select a preset channel.
     *
     * @param presetChannel The preset position [1,40]
     * @throws Exception
     */
    void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException;
}

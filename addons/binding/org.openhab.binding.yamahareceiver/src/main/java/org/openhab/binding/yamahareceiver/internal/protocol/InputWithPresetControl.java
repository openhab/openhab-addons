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
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.*;

/**
 * The preset control protocol interface
 *
 * @author David Graeff - Initial contribution
 * @author Tomasz Maruszak - Adding Spotify, Server to supported preset inputs
 */

public interface InputWithPresetControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream
            .of(
                    INPUT_TUNER,
                    INPUT_NET_RADIO,
                    INPUT_NET_RADIO_LEGACY,
                    INPUT_USB,
                    INPUT_IPOD,
                    INPUT_IPOD_USB,
                    INPUT_DOCK,
                    INPUT_PC,
                    INPUT_NAPSTER,
                    INPUT_PANDORA,
                    INPUT_SIRIUS,
                    INPUT_RHAPSODY,
                    INPUT_BLUETOOTH,
                    INPUT_SPOTIFY,
                    INPUT_SERVER,
                    INPUT_HD_RADIO
            ).collect(toSet());

    /**
     * Select a preset channel.
     *
     * @param presetChannel The preset position [1,40]
     * @throws Exception
     */
    void selectItemByPresetNumber(int presetChannel) throws IOException, ReceivedMessageParseException;
}

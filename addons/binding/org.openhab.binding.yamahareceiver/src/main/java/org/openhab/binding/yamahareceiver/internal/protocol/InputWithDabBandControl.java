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
import static org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Inputs.INPUT_TUNER;

/**
 * The DAB Band control protocol interface.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public interface InputWithDabBandControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream.of(INPUT_TUNER).collect(toSet());

    /**
     * Select a DAB band by name.
     *
     * @param band The band name (e.g. FM or DAB)
     * @throws Exception
     */
    void selectBandByName(String band) throws IOException, ReceivedMessageParseException;
}

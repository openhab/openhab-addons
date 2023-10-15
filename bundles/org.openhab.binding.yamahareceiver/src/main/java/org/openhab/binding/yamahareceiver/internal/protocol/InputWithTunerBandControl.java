/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.protocol;

import static java.util.stream.Collectors.toSet;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.INPUT_TUNER;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The DAB Band control protocol interface.
 *
 * @author Tomasz Maruszak - Initial contribution.
 */
public interface InputWithTunerBandControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Stream.of(INPUT_TUNER).collect(toSet());

    /**
     * Select a DAB band by name.
     *
     * @param band The band name (e.g. FM or DAB)
     * @throws IOException
     * @throws ReceivedMessageParseException
     */
    void selectBandByName(String band) throws IOException, ReceivedMessageParseException;
}

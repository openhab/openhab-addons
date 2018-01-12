/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.internal.protocol;

import com.google.common.collect.Sets;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;

import java.io.IOException;
import java.util.Set;

/**
 * The DAB Band control protocol interface.
 *
 * @author Tomasz Maruszak - [yamaha] Tuner band selection and preset feature for dual band models (RX-S601D)
 */
public interface InputWithDabBandControl extends IStateUpdatable {
    /**
     * List all inputs that are compatible with this kind of control
     */
    Set<String> SUPPORTED_INPUTS = Sets.newHashSet(YamahaReceiverBindingConstants.INPUT_TUNER);

    /**
     * Select a DAB band by name.
     *
     * @param band The band name (e.g. FM or DAB)
     * @throws Exception
     */
    void selectBandByName(String band) throws IOException, ReceivedMessageParseException;
}

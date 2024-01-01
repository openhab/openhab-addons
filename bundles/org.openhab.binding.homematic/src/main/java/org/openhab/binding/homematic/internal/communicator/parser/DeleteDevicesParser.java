/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.homematic.internal.misc.MiscUtils;

/**
 * Parses a delete device event received from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class DeleteDevicesParser extends CommonRpcParser<Object[], List<String>> {
    @Override
    public List<String> parse(Object[] message) throws IOException {
        List<String> adresses = new ArrayList<>();
        if (message != null && message.length > 1) {
            Object[] data = (Object[]) message[1];
            for (int i = 0; i < message.length; i++) {
                String address = getSanitizedAddress(data[i]);
                if (MiscUtils.isDevice(address)) {
                    adresses.add(address);
                }
            }
        }
        return adresses;
    }
}

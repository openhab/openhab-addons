/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homematic.internal.misc.MiscUtils;

/**
 * Parses a new device event received from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public class NewDevicesParser extends CommonRpcParser<Object[], List<String>> {
    @Override
    @SuppressWarnings("unchecked")
    public List<String> parse(Object @Nullable [] message) throws IOException {
        List<String> adresses = new ArrayList<>();
        if (message != null && message.length > 1) {
            message = (Object[]) message[1];
            for (int i = 0; i < message.length; i++) {
                Map<String, ?> data = (Map<String, ?>) message[i];

                String address = toString(data.get("ADDRESS"));
                if (MiscUtils.isDevice(address)) {
                    address = getSanitizedAddress(address);
                    if (address != null) {
                        adresses.add(address);
                    }
                }
            }
        }
        return adresses;
    }
}

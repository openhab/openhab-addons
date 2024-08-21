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
import java.util.Map;

import org.openhab.binding.homematic.internal.model.HmDatapoint;

/**
 * Parses a getValue message from a Homematic gateway.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class GetValueParser extends CommonRpcParser<Object[], Void> {
    private HmDatapoint dp;

    public GetValueParser(HmDatapoint dp) {
        this.dp = dp;
    }

    @Override
    public Void parse(Object[] message) throws IOException {
        if (message != null && message.length > 0 && !(message[0] instanceof Map)) {
            dp.setValue(convertToType(dp, message[0]));
            adjustRssiValue(dp);
        }
        return null;
    }
}

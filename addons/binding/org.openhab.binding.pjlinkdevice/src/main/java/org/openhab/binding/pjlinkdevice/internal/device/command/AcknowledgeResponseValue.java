/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command;

import java.util.HashMap;

/**
 * @author Nils Schnabel - Initial contribution
 */
public enum AcknowledgeResponseValue {
    OK;

    public String getText() {
        final HashMap<AcknowledgeResponseValue, String> texts = new HashMap<AcknowledgeResponseValue, String>();
        texts.put(OK, "Success");

        return texts.get(this);
    }

    public static AcknowledgeResponseValue getValueForCode(String code) throws ResponseException {
        final HashMap<String, AcknowledgeResponseValue> codes = new HashMap<String, AcknowledgeResponseValue>();
        codes.put("OK", OK);
        AcknowledgeResponseValue result = codes.get(code);
        if (result == null) {
            throw new ResponseException("Cannot understand status: " + code);
        }

        return result;
    }

}
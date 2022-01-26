/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.infokeydinrail.internal;

import org.openhab.binding.infokeydinrail.internal.handler.InfokeyRelayOptoDinV1Handler;

/**
 * The {@link InfokeyRelayOptoDinV1Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */

public class Mcp230xxResponse {

    public Mcp230xxResponse() {
    }

    public Mcp230xxResponse(int pinNo, boolean value) {
        this.pinNo = pinNo;
        this.value = value;
    }

    private int pinNo;
    private boolean value;

    public int getPinNo() {
        return pinNo;
    }

    public void setPinNo(int pinNo) {
        this.pinNo = pinNo;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }
}

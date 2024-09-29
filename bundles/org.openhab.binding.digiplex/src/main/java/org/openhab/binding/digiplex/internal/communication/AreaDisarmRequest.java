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
package org.openhab.binding.digiplex.internal.communication;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Command for disarming area
 *
 * @author Robert Michalak - Initial contribution
 */
@NonNullByDefault
public class AreaDisarmRequest implements DigiplexRequest {

    private int areaNo;
    private String pin;

    public AreaDisarmRequest(int areaNo, String pin) {
        this.areaNo = areaNo;
        this.pin = pin;
    }

    @Override
    public String getSerialMessage() {
        return String.format("AD%03d%s\r", areaNo, pin);
    }
}

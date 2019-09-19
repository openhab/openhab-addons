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
package org.openhab.binding.digiplex.internal.communication;

/**
 * Command for arming area
 *
 * @author Robert Michalak - Initial contribution
 */

public class AreaArmRequest implements DigiplexRequest {

    private int areaNo;
    private ArmType armType;
    private String pin;

    public AreaArmRequest(int areaNo, ArmType armType, String pin) {
        this.areaNo = areaNo;
        this.armType = armType;
        this.pin = pin;
    }

    @Override
    public String getSerialMessage() {
        return String.format("AA%03d%c%s\r", areaNo, armType.getIndicator(), pin);
    }
}

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
 * Command for quick arming area
 *
 * @author Robert Michalak - Initial contribution
 */

public class AreaQuickArmRequest implements DigiplexRequest {

    private int areaNo;
    private ArmType armType;

    public AreaQuickArmRequest(int areaNo, ArmType armType) {
        this.areaNo = areaNo;
        this.armType = armType;
    }

    @Override
    public String getSerialMessage() {
        return String.format("AQ%03d%c\r", areaNo, armType.getIndicator());
    }
}

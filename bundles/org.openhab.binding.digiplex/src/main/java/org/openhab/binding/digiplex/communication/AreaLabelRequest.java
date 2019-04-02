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
package org.openhab.binding.digiplex.communication;

/**
 * Command for requesting area label information from PRT3 device
 *
 * @author Robert Michalak - Initial contribution
 */

public class AreaLabelRequest implements DigiplexRequest {

    private int areaNo;

    public AreaLabelRequest(int areaNo) {
        this.areaNo = areaNo;
    }

    @Override
    public String getSerialMessage() {
        return String.format("AL%03d\r", areaNo);
    }

}

/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.digiplex.communication;

/**
 * Command for arming area
 *
 * @author Robert Michalak
 */

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

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
 * Command for requesting area label information from PRT3 device
 *
 * @author Robert Michalak
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

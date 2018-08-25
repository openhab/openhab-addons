/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.datatypes;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSControllerState complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */

public class WSControllerState extends WSBaseDataType {
    private String state;

    public WSControllerState() {
    }

    public WSControllerState(String state) {
        this.state = state;
    }

    /**
     * Gets the state value for this WSControllerState.
     *
     * @return state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state value for this WSControllerState.
     *
     * @param state
     */
    public void setState(String state) {
        this.state = state;
    }

    public WSControllerState parseXMLData(String data) throws IhcExecption {
        if (data.contains("getState1")) {
            state = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getState1/ns1:state");
        } else if (data.contains("waitForControllerStateChange3")) {
            state = parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:waitForControllerStateChange3/ns1:state");
        } else {
            throw new IhcExecption("Encoding error, unsupported data");
        }
        return this;
    }
}

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
package org.openhab.binding.ihc.internal.ws.datatypes;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;

/**
 * Class for WSControllerState complex type.
 *
 * @author Pauli Anttila - Initial contribution
 */

public class WSControllerState {
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
        try {
            if (data.contains("getState1")) {
                state = XPathUtils.parseXMLValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getState1/ns1:state");
            } else if (data.contains("waitForControllerStateChange3")) {
                state = XPathUtils.parseXMLValue(data,
                        "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:waitForControllerStateChange3/ns1:state");
            } else {
                throw new IhcExecption("Encoding error, unsupported data");
            }
            return this;
        } catch (IOException | XPathExpressionException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}

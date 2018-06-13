/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2.internal.ws.datatypes;

import org.openhab.binding.ihc2.internal.ws.Ihc2Execption;

/**
 * <p>
 * Java class for WSControllerState complex type.
 *
 */
/**
 * IHC WSBaseDataType data value.
 *
 * @author Pauli Anttila
 * @since 1.5.0
 */
public class WSControllerState extends WSBaseDataType {

    public static final String CONTROLLER_STATE_READY = "text.ctrl.state.ready";
    public static final String CONTROLLER_STATE_INITIALIZE = "text.ctrl.state.initialize";

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

    @Override
    public void encodeData(String data) throws Ihc2Execption {
        if (data.contains("getState1")) {
            state = parseValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getState1/ns1:state");
        } else if (data.contains("waitForControllerStateChange3")) {
            state = parseValue(data, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:waitForControllerStateChange3/ns1:state");
        } else {
            throw new Ihc2Execption("Encoding error, unsupported data");
        }
    }

}

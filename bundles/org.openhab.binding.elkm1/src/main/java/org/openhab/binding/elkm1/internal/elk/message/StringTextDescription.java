/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.elkm1.internal.elk.message;

import org.openhab.binding.elkm1.internal.elk.ElkCommand;
import org.openhab.binding.elkm1.internal.elk.ElkMessage;
import org.openhab.binding.elkm1.internal.elk.ElkTypeToRequest;

/**
 * Requests the string data for the specific type of data from the elk.
 *
 * @author David Bennett - Initial Contribution
 */
public class StringTextDescription extends ElkMessage {
    private ElkTypeToRequest requestType;
    private int numToRequest;

    public StringTextDescription(ElkTypeToRequest typeToRequest, int numToRequest) {
        super(ElkCommand.StringTextDescription);
        this.requestType = typeToRequest;
        this.numToRequest = numToRequest;
    }

    public ElkTypeToRequest getRequestType() {
        return requestType;
    }

    public void setRequestType(ElkTypeToRequest requestType) {
        this.requestType = requestType;
    }

    public int getNumToRequest() {
        return numToRequest;
    }

    public void setNumToRequest(int numToRequest) {
        this.numToRequest = numToRequest;
    }

    @Override
    protected String getData() {
        return String.format("%02X%03d", requestType.ordinal(), numToRequest);
    }

}

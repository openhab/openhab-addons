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
package org.openhab.binding.pjlinkdevice.internal.device.command.power;

import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class PowerQueryCommand extends AbstractCommand<PowerQueryRequest, PowerQueryResponse> {

    public PowerQueryCommand(PJLinkDevice pjLinkDevice) {
        super(pjLinkDevice);
    }

    @Override
    public PowerQueryRequest createRequest() {
        return new PowerQueryRequest();
    }

    @Override
    public PowerQueryResponse parseResponse(String response) throws ResponseException {
        PowerQueryResponse result = new PowerQueryResponse();
        result.parse(response);
        return result;
    }
}

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
package org.openhab.binding.pjlinkdevice.internal.device.command.identification;

import org.openhab.binding.pjlinkdevice.internal.device.command.PrefixedResponse;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * @author Nils Schnabel - Initial contribution
 */
public class IdentificationResponse extends PrefixedResponse {

    private String result = null;

    public IdentificationResponse(IdentificationCommand command) {
        super(command.identificationProperty.getPJLinkCommandPrefix() + "=");
    }

    public String getResult() {
        return result;
    }

    @Override
    protected void parse0(String responseWithoutPrefix) throws ResponseException {
        this.result = responseWithoutPrefix;
    }

}

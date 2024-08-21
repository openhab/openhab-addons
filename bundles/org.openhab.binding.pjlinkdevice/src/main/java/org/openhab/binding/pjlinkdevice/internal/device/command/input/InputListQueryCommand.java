/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pjlinkdevice.internal.device.command.input;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pjlinkdevice.internal.device.PJLinkDevice;
import org.openhab.binding.pjlinkdevice.internal.device.command.AbstractCommand;
import org.openhab.binding.pjlinkdevice.internal.device.command.ResponseException;

/**
 * This command is used for retrieving the list of available inputs of the device as described in
 * <a href="https://pjlink.jbmia.or.jp/english/data_cl2/PJLink_5-1.pdf">[PJLinkSpec]</a> chapter 4.9. Input toggling
 * list query
 *
 * @author Nils Schnabel - Initial contribution
 */
@NonNullByDefault
public class InputListQueryCommand extends AbstractCommand<InputListQueryRequest, InputListQueryResponse> {

    public InputListQueryCommand(PJLinkDevice pjLinkDevice) {
        super(pjLinkDevice);
    }

    @Override
    public InputListQueryRequest createRequest() {
        return new InputListQueryRequest();
    }

    @Override
    public InputListQueryResponse parseResponse(String response) throws ResponseException {
        return new InputListQueryResponse(response);
    }
}

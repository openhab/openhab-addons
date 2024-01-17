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
package org.openhab.binding.ecovacs.internal.api.commands;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.EnabledStateReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetTrueDetectCommand extends IotDeviceCommand<Boolean> {
    public GetTrueDetectCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Command is not supported for XML");
        }
        return "getTrueDetect";
    }

    @Override
    public Boolean convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        EnabledStateReport resp = ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson,
                EnabledStateReport.class);
        return resp.enabled != 0;
    }
}

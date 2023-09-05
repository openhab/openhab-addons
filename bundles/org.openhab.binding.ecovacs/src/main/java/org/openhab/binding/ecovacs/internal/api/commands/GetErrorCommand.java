/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.ErrorReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.DeviceInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetErrorCommand extends IotDeviceCommand<Optional<Integer>> {
    public GetErrorCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetError" : "getError";
    }

    @Override
    public Optional<Integer> convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version,
            Gson gson) throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse jsonResponse) {
            ErrorReport resp = jsonResponse.getResponsePayloadAs(gson, ErrorReport.class);
            if (resp.errorCodes.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(resp.errorCodes.get(0));
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            return DeviceInfo.parseErrorInfo(payload);
        }
    }
}

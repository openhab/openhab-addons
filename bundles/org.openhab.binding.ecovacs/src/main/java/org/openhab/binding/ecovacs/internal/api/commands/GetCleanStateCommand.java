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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.CleanReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.CleanReportV2;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.CleaningInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetCleanStateCommand extends IotDeviceCommand<CleanMode> {
    public GetCleanStateCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        switch (version) {
            case XML:
                return "GetCleanState";
            case JSON:
                return "getCleanInfo";
            case JSON_V2:
                return "getCleanInfo_V2";
        }
        throw new AssertionError();
    }

    @Override
    public CleanMode convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse jsonResponse) {
            final CleanMode mode;
            if (version == ProtocolVersion.JSON) {
                CleanReport resp = jsonResponse.getResponsePayloadAs(gson, CleanReport.class);
                mode = resp.determineCleanMode(gson);
            } else {
                CleanReportV2 resp = jsonResponse.getResponsePayloadAs(gson, CleanReportV2.class);
                mode = resp.determineCleanMode(gson);
            }
            if (mode == null) {
                throw new DataParsingException("Could not get clean mode from response " + jsonResponse.response);
            }
            return mode;
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            return CleaningInfo.parseCleanStateInfo(payload, gson).mode;
        }
    }
}

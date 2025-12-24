/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.DeviceInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetContinuousCleaningCommand extends IotDeviceCommand<Boolean> {
    public GetContinuousCleaningCommand() {
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetOnOff" : "getBreakPoint";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("t", "g");
    }

    @Override
    public Boolean convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse jsonResponse) {
            EnabledStateReport resp = jsonResponse.getResponsePayloadAs(gson, EnabledStateReport.class);
            return resp.enabled != 0;
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            return DeviceInfo.parseEnabledStateInfo(payload);
        }
    }
}

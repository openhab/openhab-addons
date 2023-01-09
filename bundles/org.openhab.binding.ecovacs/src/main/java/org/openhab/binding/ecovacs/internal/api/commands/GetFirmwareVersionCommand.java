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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetFirmwareVersionCommand extends IotDeviceCommand<String> {
    public GetFirmwareVersionCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version != ProtocolVersion.XML) {
            throw new IllegalStateException("Get FW version is only supported for XML");
        }
        return "GetVersion";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("name", "FW");
    }

    @Override
    public String convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
        return XPathUtils.getFirstXPathMatch(payload, "//ver[@name='FW']").getTextContent();
    }
}

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.MapSetReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetMapSpotAreasWithMapIdCommand extends IotDeviceCommand<List<String>> {
    private final String mapId;

    public GetMapSpotAreasWithMapIdCommand(String mapId) {
        this.mapId = mapId;
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetMapSet" : "getMapSet";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("tp", "sa");
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonObject args = new JsonObject();
        args.addProperty("mid", mapId);
        args.addProperty("type", "ar");
        return args;
    }

    @Override
    public List<String> convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse) {
            MapSetReport resp = ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson,
                    MapSetReport.class);
            return resp.subsets.stream().map(i -> i.id).collect(Collectors.toList());
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            NodeList mapIds = XPathUtils.getXPathMatches(payload, "//m/@mid");
            List<String> result = new ArrayList<>();
            for (int i = 0; i < mapIds.getLength(); i++) {
                result.add(mapIds.item(i).getNodeValue());
            }
            return result;
        }
    }
}

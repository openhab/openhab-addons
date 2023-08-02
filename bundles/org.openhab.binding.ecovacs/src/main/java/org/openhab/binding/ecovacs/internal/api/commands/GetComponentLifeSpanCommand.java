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

import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.ComponentLifeSpanReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.DeviceInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.Component;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetComponentLifeSpanCommand extends IotDeviceCommand<Integer> {
    private final Component type;

    public GetComponentLifeSpanCommand(Component type) {
        this.type = type;
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetLifeSpan" : "getLifeSpan";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("type", type.xmlValue);
    }

    @Override
    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        JsonArray args = new JsonArray(1);
        args.add(type.jsonValue);
        return args;
    }

    @Override
    public Integer convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse) {
            JsonElement respPayloadRaw = ((PortalIotCommandJsonResponse) response).getResponsePayload(gson);
            Type type = new TypeToken<List<ComponentLifeSpanReport>>() {
            }.getType();
            try {
                List<ComponentLifeSpanReport> resp = gson.fromJson(respPayloadRaw, type);
                if (resp == null || resp.isEmpty()) {
                    throw new DataParsingException("Invalid lifespan response " + respPayloadRaw);
                }
                return (int) Math.round(100.0 * resp.get(0).left / resp.get(0).total);
            } catch (JsonSyntaxException e) {
                throw new DataParsingException(e);
            }
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            return DeviceInfo.parseComponentLifespanInfo(payload);
        }
    }
}

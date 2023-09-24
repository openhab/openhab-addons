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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.request.portal.PortalIotCommandRequest.JsonPayloadHeader;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public abstract class IotDeviceCommand<RESPONSETYPE> {
    protected IotDeviceCommand() {
    }

    public abstract String getName(ProtocolVersion version);

    public final String getXmlPayload(@Nullable String id) throws ParserConfigurationException, TransformerException {
        Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element ctl = xmlDoc.createElement("ctl");
        ctl.setAttribute("td", getName(ProtocolVersion.XML));
        if (id != null) {
            ctl.setAttribute("id", id);
        }
        applyXmlPayload(xmlDoc, ctl);
        xmlDoc.appendChild(ctl);
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        tf.transform(new DOMSource(xmlDoc), new StreamResult(writer));
        return writer.getBuffer().toString().replaceAll("\n|\r", "");
    }

    public final JsonElement getJsonPayload(ProtocolVersion version, Gson gson) {
        JsonObject result = new JsonObject();
        result.add("header", gson.toJsonTree(new JsonPayloadHeader()));
        @Nullable
        JsonElement args = getJsonPayloadArgs(version);
        if (args != null) {
            JsonObject body = new JsonObject();
            body.add("data", args);
            result.add("body", body);
        }
        return result;
    }

    protected @Nullable JsonElement getJsonPayloadArgs(ProtocolVersion version) {
        return null;
    }

    protected void applyXmlPayload(Document doc, Element ctl) {
    }

    public abstract RESPONSETYPE convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version,
            Gson gson) throws DataParsingException;
}

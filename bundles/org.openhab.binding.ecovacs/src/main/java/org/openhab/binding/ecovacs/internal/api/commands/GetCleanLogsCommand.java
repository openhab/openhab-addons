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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.CleanLogRecord;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetCleanLogsCommand extends IotDeviceCommand<List<CleanLogRecord>> {
    private static final int LOG_SIZE = 20;

    @Override
    public String getName(ProtocolVersion version) {
        if (version != ProtocolVersion.XML) {
            throw new IllegalStateException("Command is only supported for XML");
        }
        return "GetCleanLogs";
    }

    @Override
    protected void applyXmlPayload(Document doc, Element ctl) {
        ctl.setAttribute("count", String.valueOf(LOG_SIZE));
    }

    @Override
    public List<CleanLogRecord> convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version,
            Gson gson) throws DataParsingException {
        String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            NodeList entryNodes = db.parse(new ByteArrayInputStream(payload.getBytes("UTF-8"))).getFirstChild()
                    .getChildNodes();
            List<CleanLogRecord> result = new ArrayList<>();

            for (int i = 0; i < entryNodes.getLength(); i++) {
                NamedNodeMap attrs = entryNodes.item(i).getAttributes();
                String area = attrs.getNamedItem("a").getNodeValue();
                String startTime = attrs.getNamedItem("s").getNodeValue();
                String duration = attrs.getNamedItem("l").getNodeValue();

                result.add(new CleanLogRecord(Long.parseLong(startTime), Integer.parseInt(duration),
                        Integer.parseInt(area), Optional.empty(), CleanMode.IDLE));
            }
            return result;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DataParsingException(e);
        }
    }
}

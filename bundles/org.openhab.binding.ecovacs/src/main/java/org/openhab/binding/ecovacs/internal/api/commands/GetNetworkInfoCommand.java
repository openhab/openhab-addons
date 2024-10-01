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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.NetworkInfoReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.NetworkInfo;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.openhab.binding.ecovacs.internal.api.util.XPathUtils;
import org.w3c.dom.Node;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetNetworkInfoCommand extends IotDeviceCommand<NetworkInfo> {
    public GetNetworkInfoCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetNetInfo" : "getNetInfo";
    }

    @Override
    public NetworkInfo convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse jsonResponse) {
            NetworkInfoReport resp = jsonResponse.getResponsePayloadAs(gson, NetworkInfoReport.class);
            try {
                return new NetworkInfo(resp.ip, resp.mac, resp.ssid, Integer.valueOf(resp.rssi));
            } catch (NumberFormatException e) {
                throw new DataParsingException(e);
            }
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            Node ipAttr = XPathUtils.getFirstXPathMatch(payload, "//@wi");
            Node ssidAttr = XPathUtils.getFirstXPathMatch(payload, "//@s");
            return new NetworkInfo(ipAttr.getNodeValue(), "", ssidAttr.getNodeValue(), 0);
        }
    }
}

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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.WaterInfoReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.xml.WaterSystemInfo;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.MoppingWaterAmount;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetMoppingWaterAmountCommand extends IotDeviceCommand<MoppingWaterAmount> {
    public GetMoppingWaterAmountCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        return version == ProtocolVersion.XML ? "GetWaterPermeability" : "getWaterInfo";
    }

    @Override
    public MoppingWaterAmount convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version,
            Gson gson) throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse jsonResponse) {
            WaterInfoReport resp = jsonResponse.getResponsePayloadAs(gson, WaterInfoReport.class);
            return MoppingWaterAmount.fromApiValue(resp.waterAmount);
        } else {
            String payload = ((PortalIotCommandXmlResponse) response).getResponsePayloadXml();
            return WaterSystemInfo.parseWaterPermeabilityInfo(payload);
        }
    }
}

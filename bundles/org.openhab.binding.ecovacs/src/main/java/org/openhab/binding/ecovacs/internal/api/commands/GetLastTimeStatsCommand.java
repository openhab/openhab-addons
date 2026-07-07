/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.LastTimeStatsReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * Polls the GOAT mower for the last completed mowing session statistics.
 * The response contains start time, duration (seconds), and area (cm²).
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GetLastTimeStatsCommand extends IotDeviceCommand<LastTimeStatsReport> {
    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Mower commands are not supported for XML");
        }
        return "getLastTimeStats";
    }

    @Override
    public LastTimeStatsReport convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version,
            Gson gson) throws DataParsingException {
        return ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson, LastTimeStatsReport.class);
    }
}

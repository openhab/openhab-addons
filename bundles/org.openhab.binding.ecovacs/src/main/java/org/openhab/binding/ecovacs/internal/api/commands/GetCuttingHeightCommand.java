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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GetCuttingHeightCommand extends IotDeviceCommand<Integer> {

    private static class CuttingHeightReport {
        @SerializedName("height")
        public int height;

        @SerializedName("level")
        public int level;
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Mower commands are not supported for XML");
        }
        return "getCutHeight";
    }

    @Override
    public Integer convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        CuttingHeightReport resp = ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson,
                CuttingHeightReport.class);
        // Some models return 'height' in mm directly, others return 'level' (1-10)
        // Level maps to mm: level 1 = 30mm, level 2 = 35mm, ..., level 10 = 75mm
        if (resp.height > 0) {
            return resp.height;
        }
        return resp.level * 5 + 25;
    }
}

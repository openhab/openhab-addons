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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
public class GetVolumeCommand extends IotDeviceCommand<Integer> {
    public GetVolumeCommand() {
        super();
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Get volume command is not supported for XML");
        }
        return "getVolume";
    }

    @Override
    public Integer convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        if (response instanceof PortalIotCommandJsonResponse) {
            JsonResponse resp = ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson,
                    JsonResponse.class);
            return resp.volume;
        } else {
            // unsupported in XML case?
            return 0;
        }
    }

    private static class JsonResponse {
        @SerializedName("volume")
        public int volume;

        @SerializedName("total")
        public int maxVolume;
    }
}

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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.AbstractPortalIotCommandResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;

/**
 * Gets the current mower state via 'getCleanInfo'.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GetMowerStateCommand extends IotDeviceCommand<CleanMode> {

    private static class CleanStateDto {
        public @Nullable String motionState;
    }

    private static class MowerStateDto {
        public @Nullable String state;
        public @Nullable CleanStateDto cleanState;
    }

    @Override
    public String getName(ProtocolVersion version) {
        if (version == ProtocolVersion.XML) {
            throw new IllegalStateException("Mower commands are not supported for XML");
        }
        return "getCleanInfo";
    }

    @Override
    public CleanMode convertResponse(AbstractPortalIotCommandResponse response, ProtocolVersion version, Gson gson)
            throws DataParsingException {
        MowerStateDto dto = ((PortalIotCommandJsonResponse) response).getResponsePayloadAs(gson, MowerStateDto.class);
        if (dto == null) {
            throw new DataParsingException("Null payload in getCleanInfo response");
        }

        if (dto.cleanState != null) {
            String motionState = dto.cleanState.motionState;
            if ("working".equals(motionState)) {
                return CleanMode.AUTO;
            } else if ("pause".equals(motionState)) {
                return CleanMode.PAUSE;
            } else if ("goCharging".equals(motionState)) {
                return CleanMode.RETURNING;
            }
        }

        if ("goCharging".equals(dto.state)) {
            return CleanMode.RETURNING;
        }

        return CleanMode.IDLE;
    }
}

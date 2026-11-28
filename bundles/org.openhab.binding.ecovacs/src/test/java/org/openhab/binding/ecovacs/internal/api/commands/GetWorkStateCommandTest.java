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

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandXmlResponse;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Andreas Rothstein - Initial contribution
 */
@NonNullByDefault
class GetWorkStateCommandTest {
    private final Gson gson = new Gson();
    private final GetWorkStateCommand command = new GetWorkStateCommand();

    @Test
    void requestsAndDecodesWorkState() throws Exception {
        assertEquals("getWorkState", command.getName(ProtocolVersion.JSON_V2));
        var payload = new JsonObject();
        payload.add("body", JsonParser.parseString("""
                {"data":{"robotState":{"state":"cleaning","cleanState":{"type":"freeClean"}},
                "stationState":{"state":"washing"}}}
                """));
        var report = command.convertResponse(new PortalIotCommandJsonResponse("ok", payload, 0, ""),
                ProtocolVersion.JSON_V2, gson);
        assertEquals(CleanMode.WASHING, report.determineCleanMode(gson, true));
        assertEquals(CleanMode.SPOT_AREA, report.determineCleanMode(gson, false));
    }

    @Test
    void rejectsXmlResponse() {
        assertThrows(DataParsingException.class,
                () -> command.convertResponse(new PortalIotCommandXmlResponse("ok", "<ctl/>", 0, ""),
                        ProtocolVersion.JSON_V2, gson));
    }
}

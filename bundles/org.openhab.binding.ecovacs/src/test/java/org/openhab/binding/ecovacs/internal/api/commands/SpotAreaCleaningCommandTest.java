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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ecovacs.internal.api.impl.ProtocolVersion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * @author Andreas Rothstein - Initial contribution
 */
@NonNullByDefault
class SpotAreaCleaningCommandTest {
    @Test
    void x12sUsesSemicolonSeparatedRoomPairs() {
        JsonObject content = content(true);
        assertEquals("freeClean", content.get("type").getAsString());
        assertEquals("1,4;1,1;1,7", content.get("value").getAsString());
    }

    @Test
    void legacyRoomListIsUnchanged() {
        JsonObject content = content(false);
        assertEquals("spotArea", content.get("type").getAsString());
        assertEquals("4,1,7", content.get("value").getAsString());
    }

    private JsonObject content(boolean freeClean) {
        return new SpotAreaCleaningCommand(List.of("4", "1", "7"), 1, freeClean)
                .getJsonPayload(ProtocolVersion.JSON_V2, new Gson()).getAsJsonObject().getAsJsonObject("body")
                .getAsJsonObject("data").getAsJsonObject("content");
    }
}

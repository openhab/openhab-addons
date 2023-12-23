/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.intellicenter2.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.intellicenter2.internal.protocol.ICProtocol.GSON;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class IntelliBriteTest {

    @Test
    public void testResponseRed() {
        String jsonResponse = "{\"command\":\"SendParamList\",\"messageID\":\"ffc6dca0-1d94-454d-84ba-decc5e1ec067\",\"response\":\"200\",\"objectList\":[{\"objnam\":\"C0004\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"INTELLI\",\"STATUS\":\"ON\",\"SNAME\":\"Spa Light\",\"ACT\":\"65535\",\"USE\":\"REDR\"}}]}\n";
        var response = GSON.fromJson(jsonResponse, ICResponse.class);
        var light = new IntelliBrite(response.getObjectList().get(0));

        assertEquals(light.getColor(), IntelliBrite.Color.RED);
    }

    @Test
    public void testResponseMagenta() {
        String jsonResponse = "{\"command\":\"SendParamList\",\"messageID\":\"ffc6dca0-1d94-454d-84ba-decc5e1ec067\",\"response\":\"200\",\"objectList\":[{\"objnam\":\"C0004\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"INTELLI\",\"STATUS\":\"ON\",\"SNAME\":\"Spa Light\",\"ACT\":\"65535\",\"USE\":\"MAGNTAR\"}}]}\n";
        var response = GSON.fromJson(jsonResponse, ICResponse.class);
        var light = new IntelliBrite(response.getObjectList().get(0));

        assertEquals(light.getColor(), IntelliBrite.Color.MAGENTA);
    }
}

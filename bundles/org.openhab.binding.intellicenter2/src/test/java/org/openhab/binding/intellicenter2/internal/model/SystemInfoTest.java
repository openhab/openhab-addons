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
public class SystemInfoTest {

    @Test
    public void testResponse() {
        String jsonResponse = "{\"command\":\"SendParamList\",\"messageID\":\"0c0a089b-41df-47da-a950-09aa72b264fe\",\"response\":\"200\",\"objectList\":[{\"objnam\":\"_5451\",\"params\":{\"MODE\":\"ENGLISH\",\"VER\":\"IC: 1.064 , ICWEB:2021-10-19 1.007\",\"PROPNAME\":\"my property\",\"SNAME\":\"(TV_+ubDl;}>llXT>pd<*V<+twKK<|$5oEW@b$%E!U3mH6;tKjpBR..fv/.s}&_^\"}}]}\n";

        var response = GSON.fromJson(jsonResponse, ICResponse.class);
        var info = new SystemInfo(response.getObjectList().get(0));

        assertEquals("1.064", info.getIntellicenterVersion());
        assertEquals("my property", info.getPropertyName());
        assertEquals("ENGLISH", info.getMode());
    }
}

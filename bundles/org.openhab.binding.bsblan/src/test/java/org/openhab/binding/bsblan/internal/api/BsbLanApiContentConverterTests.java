/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bsblan.internal.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameterQueryResponse;
import org.openhab.binding.bsblan.internal.api.models.BsbLanApiParameter;

/**
 * The {@link BsbLanApiContentConverterTests} class implements tests 
 * for {@link BsbLanApiContentConverter}.
 *
 * @author Peter Schraffl - Initial contribution
 */
public class BsbLanApiContentConverterTests {

    @Test
    public void parseBsbLanApiParameterQueryResponse() {
        String content = 
            "{\r\n" +
                "\"700\": {\r\n" +
                    "\"name\": \"Betriebsart\",\r\n" +
                    "\"value\": \"0\",\r\n" +
                    "\"unit\": \"\",\r\n" +
                    "\"desc\": \"Schutzbetrieb\",\r\n" +
                    "\"dataType\": 1\r\n" +
                "}\r\n" +
            "}";

        BsbLanApiParameterQueryResponse r = BsbLanApiContentConverter.fromJson(content, BsbLanApiParameterQueryResponse.class);
        assertTrue(r.containsKey(700));
        
        BsbLanApiParameter p = r.get(700);
        assertEquals("Betriebsart", p.name);
        assertEquals("0", p.value);
        assertEquals("", p.unit);
        assertEquals("Schutzbetrieb", p.description);
        assertEquals(BsbLanApiParameter .DataType.DT_ENUM, p.dataType);
    }
}

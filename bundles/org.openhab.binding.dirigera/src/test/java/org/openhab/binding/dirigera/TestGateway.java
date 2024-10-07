/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * {@link TestGateway} some basic tests
 *
 * @author Bernd Weymann - Initial Contribution
 */
class TestGateway {

    @Test
    void testHomeDump() {
        String homeDumpString = FileReader.readFileInString("src/test/resources/test.json");
        // System.out.println(homeDumpString);
        JSONObject homeObject = new JSONObject(homeDumpString);
        // System.out.println(homeObject);
        JSONArray devices = homeObject.getJSONArray("devices");
        Iterator<Object> entries = devices.iterator();
        while (entries.hasNext()) {
            JSONObject entry = (JSONObject) entries.next();
            System.out.println(entry.get("type") + " : " + entry.get("id"));
        }
    }
}

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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;

/**
 * The {@link JsonTest} Test Json conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
class JsonTest {

    @Test
    void testOdoMapper() {
        String content = FileReader.readFileInString("src/test/resources/odo.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }

    @Test
    void testEVMapper() {
        String content = FileReader.readFileInString("src/test/resources/evstatus.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }

    @Test
    void testFuelMapper() {
        String content = FileReader.readFileInString("src/test/resources/fuel.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }

    @Test
    void testLockMapper() {
        String content = FileReader.readFileInString("src/test/resources/lock.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }

    @Test
    void testStatusMapper() {
        String content = FileReader.readFileInString("src/test/resources/status.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }

    @Test
    void testEQALightsMapper() {
        String content = FileReader.readFileInString("src/test/resources/eqa-light-sample.json");
        JSONArray ja = new JSONArray(content);
        ja.forEach(entry -> {
            JSONObject jo = (JSONObject) entry;
            ChannelStateMap csm = Mapper.getChannelStateMap(jo);
            System.out.println(csm);
            assertNotNull(csm);

        });
    }
}

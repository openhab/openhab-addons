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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.core.library.types.DateTimeType;

/**
 * The {@link JsonTest} Test Json conversions
 *
 * @author Bernd Weymann - Initial contribution
 */
class JsonTest {
    public static final String DATE_INPUT_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN_STRING);

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

    @Test
    void testTimeStamp() {
        String content = FileReader.readFileInString("src/test/resources/eqa-light-sample.json");
        JSONArray ja = new JSONArray(content);
        System.out.println(ja.length());
        long lastTimestamp = 0;
        for (Iterator iterator = ja.iterator(); iterator.hasNext();) {
            JSONObject jo = (JSONObject) iterator.next();
            Set<String> s = jo.keySet();
            if (s.size() > 0) {
                String id = s.toArray()[0].toString();
                JSONObject val = jo.getJSONObject(id);
                if (val.has("timestamp")) {
                    lastTimestamp = val.getLong("timestamp");
                    System.out.println("Found timestamp " + lastTimestamp);
                }
            }
        }
        Date d = new Date(lastTimestamp);
        LocalDateTime ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println(ld.format(DATE_INPUT_PATTERN));
        DateTimeType dtt = DateTimeType.valueOf(ld.format(DATE_INPUT_PATTERN));
        System.out.println(dtt);
    }
}

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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.actions.VehicleActions;
import org.openhab.binding.mercedesme.internal.dto.PINRequest;
import org.openhab.binding.mercedesme.internal.utils.Utils;
import org.openhab.core.library.types.OnOffType;

/**
 * The {@link ConfigurationTest} Test configuration settings
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ConfigurationTest {

    @Test
    public void testAction() {
        VehicleActions va = new VehicleActions();
        va.sendPoi("a", 9.4, 3.2);
    }

    @Test
    public void testRunTill() {
        System.out.println(Instant.now());
    }

    @Test
    public void testThingUID() {
        System.out.println(Constants.THING_TYPE_BEV.getId());
    }

    @Test
    void testRound() {
        int socValue = 66;
        double batteryCapacity = 66.5;
        float chargedValue = Math.round(socValue * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(43.89, chargedValue, 0.01);
        float unchargedValue = Math.round((100 - socValue) * 1000 * (float) batteryCapacity / 1000) / (float) 100;
        assertEquals(22.61, unchargedValue, 0.01);
        assertEquals(batteryCapacity, chargedValue + unchargedValue, 0.01);
    }

    @Test
    void testLocale() {
        Locale l = Locale.GERMANY;
        System.out.println(l.getCountry());
        System.out.println(l.toString());
    }

    @Test
    void testRegion() {
        System.out.println(Utils.getLoginAppId(Constants.REGION_EUROPE));
    }

    @Test
    void testGSON() {
        PINRequest pr = new PINRequest("a", "b");
        System.out.println(Utils.GSON.toJson(pr));
    }

    @Test
    void testEncoding() {
        try {
            System.out.println(URLEncoder.encode("bernd.w@ymann.de", StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testCapabilitiesEndpoint() {
        String capabilitiesEndpoint = "/vehicle/%s/capabilities";
        String capabilitiesUrl = Utils.getRestAPIServer("EU") + String.format(capabilitiesEndpoint, "123");
        System.out.println(capabilitiesUrl);
    }

    @Test
    public void testCapabilities() {
        String capas = FileReader.readFileInString("src/test/resources/Capabilities.json");
        JSONObject jo = new JSONObject(capas);
        JSONObject features = jo.getJSONObject("features");
        Map featureMap = new HashMap();
        features.keySet().forEach(key -> {
            Object value = features.get(key);
            String newKey = Character.toUpperCase(key.charAt(0)) + key.substring(1);
            newKey = "feature" + newKey;
            featureMap.put(key, value);
            System.out.println(newKey);
        });
        assertEquals(25, features.toMap().size(), "Size");
        System.out.println(features.toMap().size());
        String commandCapas = FileReader.readFileInString("src/test/resources/CommandCapabilities.json");
        JSONObject commands = new JSONObject(commandCapas);
        JSONArray commandArray = commands.getJSONArray("commands");
        Map cmds = new HashMap();
        commandArray.forEach(object -> {
            String commandName = ((JSONObject) object).get("commandName").toString();
            String[] words = commandName.split("[\\W_]+");
            StringBuilder builder = new StringBuilder();
            builder.append("command");
            for (int i = 0; i < words.length; i++) {
                String word = words[i];
                word = word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
                builder.append(word);
            }
            String value = ((JSONObject) object).get("isAvailable").toString();
            System.out.println(builder.toString() + ":" + value);
            cmds.put(builder.toString(), value);
        });
        System.out.println(cmds);
        System.out.println(Constants.THING_TYPE_HYBRID.getId());
    }

    @Test
    public void testDuration() {
        System.out.println(Utils.getDurationString(16000));
        System.out.println(Utils.getDurationString((24 * 60 + 14)));
        System.out.println(Utils.getDurationString(24 * 60));
        System.out.println(OnOffType.from(false));
    }
}

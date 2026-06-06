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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.APIURL_BASE;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioFlexScheduleRuleResponse;
import org.openhab.binding.rachio.internal.api.json.RachioSmartIrrigationGsonDTO.RachioScheduleRuleResponse;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests schedule service endpoint selection and command payloads.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioScheduleApiTest {
    @Test
    void fixedAndFlexScheduleReadsUseSeparateServices() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        setField(api, "httpApi", http);

        RachioScheduleRuleResponse fixed = api.getScheduleRule("fixed-id");
        RachioFlexScheduleRuleResponse flex = api.getFlexScheduleRule("flex-id");

        assertThat(http.getUrls,
                contains(APIURL_BASE + "schedulerule/fixed-id", APIURL_BASE + "flexschedulerule/flex-id"));
        assertThat(fixed.id, is("fixed-id"));
        assertThat(flex.id, is("flex-id"));
    }

    @Test
    void fixedAndFlexCommandsUseScheduleRuleService() throws Exception {
        RachioApi api = new RachioApi("person-id");
        RecordingRachioHttp http = new RecordingRachioHttp();
        setField(api, "httpApi", http);

        api.startScheduleRule("fixed-id");
        api.skipScheduleRule("flex-id");
        api.skipForwardZoneRun("flex-id");
        api.setScheduleRuleSeasonalAdjustment("flex-id", 0.75);

        assertThat(http.putUrls, contains(APIURL_BASE + "schedulerule/start", APIURL_BASE + "schedulerule/skip",
                APIURL_BASE + "schedulerule/skip_forward_zone_run", APIURL_BASE + "schedulerule/seasonal_adjustment"));
        assertThat(payload(http.putData.get(0)).get("id").getAsString(), is("fixed-id"));
        assertThat(payload(http.putData.get(1)).get("id").getAsString(), is("flex-id"));
        assertThat(payload(http.putData.get(2)).get("id").getAsString(), is("flex-id"));
        assertThat(payload(http.putData.get(3)).get("id").getAsString(), is("flex-id"));
        assertThat(payload(http.putData.get(3)).get("adjustment").getAsDouble(), is(0.75));
    }

    private static JsonObject payload(String value) {
        return JsonParser.parseString(value).getAsJsonObject();
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static class RecordingRachioHttp extends RachioHttp {
        private final List<String> getUrls = new ArrayList<>();
        private final List<String> putUrls = new ArrayList<>();
        private final List<String> putData = new ArrayList<>();

        @Override
        public RachioApiResult httpGet(String url, @Nullable String urlParameters) {
            getUrls.add(url);
            RachioApiResult result = new RachioApiResult();
            result.resultString = url.contains("flexschedulerule") ? "{\"id\":\"flex-id\"}" : "{\"id\":\"fixed-id\"}";
            return result;
        }

        @Override
        public RachioApiResult httpPut(String url, String data) {
            putUrls.add(url);
            putData.add(data);
            return new RachioApiResult();
        }
    }
}

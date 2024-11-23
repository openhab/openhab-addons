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
package org.openhab.binding.windcentrale.internal.dto;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.windcentrale.internal.dto.Project.Participation;

/**
 * Tests deserialization of Windcentrale API responses from JSON.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class WindcentraleGsonTest {

    private static final DataUtil DATA_UTIL = new DataUtil(WindcentraleGson.GSON);

    @Test
    public void deserializeKeyResponse() throws IOException {
        KeyResponse key = DATA_UTIL.fromJson("key-response.json", KeyResponse.class);
        assertThat(key, is(notNullValue()));

        assertThat(key.clientId, is("715j3r0trk7o8dqg3md57il7q0"));
        assertThat(key.region, is("eu-west-1"));
        assertThat(key.userPoolId, is("eu-west-1_U7eYBPrBd"));
    }

    @Test
    public void deserializeProjectsResponse() throws IOException {
        List<Project> projects = DATA_UTIL.fromJson("projects-response.json", WindcentraleGson.PROJECTS_RESPONSE_TYPE);

        assertThat(projects, is(notNullValue()));
        assertThat(projects.size(), is(1));

        Project project = projects.get(0);

        assertThat(project.projectName, is("De Grote Geert"));
        assertThat(project.projectCode, is("WND-GG"));

        List<Participation> participations = Objects.requireNonNull(project.participations);
        assertThat(participations.size(), is(2));

        assertThat(participations.get(0).share, is(20));
        assertThat(participations.get(1).share, is(50));
    }

    @Test
    public void deserializeLiveDataResponseEmpty() throws IOException {
        Map<Windmill, WindmillStatus> map = DATA_UTIL.fromJson("live-data-response-empty.json",
                WindcentraleGson.LIVE_DATA_RESPONSE_TYPE);

        assertThat(map, is(notNullValue()));
        assertThat(map.size(), is(0));
    }

    @Test
    public void deserializeLiveDataResponseSingle() throws IOException {
        Map<Windmill, WindmillStatus> map = DATA_UTIL.fromJson("live-data-response-single.json",
                WindcentraleGson.LIVE_DATA_RESPONSE_TYPE);

        assertThat(map, is(notNullValue()));
        assertThat(map.size(), is(1));

        assertDeJongeHeldStatus(map);
    }

    @Test
    public void deserializeLiveDataResponseMultiple() throws IOException {
        Map<Windmill, WindmillStatus> map = DATA_UTIL.fromJson("live-data-response-multiple.json",
                WindcentraleGson.LIVE_DATA_RESPONSE_TYPE);

        assertThat(map, is(notNullValue()));
        assertThat(map.size(), is(11));

        assertDeBlauweReigerStatus(map);
        assertDeJongeHeldStatus(map);
        assertDeWitteJufferStatus(map);
    }

    private void assertDeBlauweReigerStatus(Map<Windmill, WindmillStatus> map) {
        WindmillStatus status = Objects.requireNonNull(map.get(Windmill.DE_BLAUWE_REIGER));

        assertThat(status.powerPerShare, is(150));
        assertThat(status.timestamp.toEpochSecond(), is(1680425425L));
        assertThat(status.windPower, is(7));
        assertThat(status.power, is(827));
        assertThat(status.windDirection, is("O"));
        assertThat(status.yearProduction, is(872488));
        assertThat(status.totalRuntime, is(29470));
        assertThat(status.yearRuntime, is(-98268833.015556d));
        assertThat(status.powerPercentage, is(98));
    }

    private void assertDeJongeHeldStatus(Map<Windmill, WindmillStatus> map) {
        WindmillStatus status = Objects.requireNonNull(map.get(Windmill.DE_JONGE_HELD));

        assertThat(status.powerPerShare, is(52));
        assertThat(status.timestamp.toEpochSecond(), is(1680425425L));
        assertThat(status.windPower, is(5));
        assertThat(status.power, is(522));
        assertThat(status.windDirection, is("O"));
        assertThat(status.yearProduction, is(1508090));
        assertThat(status.totalRuntime, is(122330));
        assertThat(status.yearRuntime, is(2089d));
        assertThat(status.powerPercentage, is(23));
    }

    private void assertDeWitteJufferStatus(Map<Windmill, WindmillStatus> map) {
        WindmillStatus status = Objects.requireNonNull(map.get(Windmill.DE_WITTE_JUFFER));

        assertThat(status.powerPerShare, is(134));
        assertThat(status.timestamp.toEpochSecond(), is(1680425425L));
        assertThat(status.windPower, is(5));
        assertThat(status.power, is(764));
        assertThat(status.windDirection, is("NO"));
        assertThat(status.yearProduction, is(1233164));
        assertThat(status.totalRuntime, is(111171));
        assertThat(status.yearRuntime, is(2118.266667d));
        assertThat(status.powerPercentage, is(39));
    }
}

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
package org.openhab.binding.hue.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.hue.internal.HttpClient.Result;
import org.openhab.binding.hue.internal.exceptions.ApiException;

/**
 * @author Hengrui Jiang - initial contribution
 */
public class HueBridgeTest {

    @Test
    public void testGetScenesExcludeRecycleScenes() throws IOException, ApiException {
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

        HueBridge hueBridge = new HueBridge("ip", "baseUrl", "username", Executors.newScheduledThreadPool(1),
                mockHttpClient);

        List<Scene> testScenes = Arrays.asList(new Scene("id1", "name1", "group1", Collections.emptyList(), true), //
                new Scene("id2", "name2", "group2", Collections.emptyList(), false));
        when(mockHttpClient.get("baseUrl/username/scenes")).thenReturn(new Result(createMockResponse(testScenes), 200));

        List<Scene> scenes = hueBridge.getScenes();
        assertThat(scenes.size(), is(1));
        assertThat(scenes.get(0).getId(), is("id2"));
    }

    @Test
    public void testGetScenesOrderByGroup() throws IOException, ApiException {
        HttpClient mockHttpClient = Mockito.mock(HttpClient.class);

        HueBridge hueBridge = new HueBridge("ip", "baseUrl", "username", Executors.newScheduledThreadPool(1),
                mockHttpClient);

        List<Scene> testScenes = Arrays.asList(new Scene("id1", "name1", "group1", Collections.emptyList(), false), //
                new Scene("id2", "name2", "group2", Collections.emptyList(), false),
                new Scene("id3", "name3", "group1", Collections.emptyList(), false));
        when(mockHttpClient.get("baseUrl/username/scenes")).thenReturn(new Result(createMockResponse(testScenes), 200));

        List<Scene> scenes = hueBridge.getScenes();
        assertThat(scenes.size(), is(3));
        assertThat(scenes.get(0).getId(), is("id1"));
        assertThat(scenes.get(1).getId(), is("id3"));
        assertThat(scenes.get(2).getId(), is("id2"));
    }

    private static String createMockResponse(List<Scene> scenes) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        stringBuilder.append(scenes.stream().map(HueBridgeTest::createMockJson).collect(Collectors.joining(",")));
        stringBuilder.append("\n}");
        return stringBuilder.toString();
    }

    private static String createMockJson(Scene scene) {
        // Sample response for getting scenes taken from hue API documentation.
        // Extended with the attribute "group"
        String template = "" + //
                "    \"%s\": {\n" + //
                "        \"name\": \"%s\",\n" + //
                "        \"lights\": [%s],\n" + //
                "        \"owner\": \"ffffffffe0341b1b376a2389376a2389\",\n" + //
                "        \"recycle\": %s,\n" + //
                "        \"locked\": false,\n" + //
                "        \"appdata\": {},\n" + //
                "        \"picture\": \"\",\n" + //
                "        \"lastupdated\": \"2015-12-03T08:57:13\",\n" + //
                "        \"version\": 2,\n" + //
                "        \"group\": \"%s\"\n" + //
                "    }";
        String lights = String.join(",",
                scene.getLightIds().stream().map(id -> "\"" + id + "\"").collect(Collectors.toList()));
        return String.format(template, scene.getId(), scene.getName(), lights, scene.isRecycle(), scene.getGroupId());
    }
}

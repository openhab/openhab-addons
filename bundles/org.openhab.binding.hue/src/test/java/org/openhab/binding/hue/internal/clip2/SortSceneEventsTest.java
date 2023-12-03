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
package org.openhab.binding.hue.internal.clip2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.Resources;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;
import org.openhab.binding.hue.internal.api.serialization.InstantDeserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JUnit test for sorting the sequence of scene events in a list.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class SortSceneResourcesTest {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantDeserializer())
            .create();

    private static final String ACTIVE_SCENE_RESOURCE_ID = "ACTIVE00-0000-0000-0000-000000000001";

    private String loadJson(String fileName) {
        try (FileReader file = new FileReader(String.format("src/test/resources/%s.json", fileName));
                BufferedReader reader = new BufferedReader(file)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return "";
    }

    @Test
    void testSceneSorting() {
        Resources resources = GSON.fromJson(loadJson("scene_event"), Resources.class);
        assertNotNull(resources);
        List<Resource> list = resources.getResources();
        assertNotNull(list);
        list.forEach(r -> r.markAsSparse());

        assertEquals(5, list.size());
        assertEquals(ACTIVE_SCENE_RESOURCE_ID, list.get(1).getId());

        Setters.sortSceneResources(list);

        assertEquals(5, list.size());
        assertEquals(ACTIVE_SCENE_RESOURCE_ID, list.get(4).getId());
    }
}

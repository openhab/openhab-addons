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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.hue.internal.api.dto.clip2.Resource;
import org.openhab.binding.hue.internal.api.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.api.dto.clip2.helper.Setters;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * JUnit test for sorting the sequence of scene events in a list.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class SortSceneResourcesTest {

    private static final JsonElement ACTIVE = JsonParser.parseString("{\"active\":\"static\"}");
    private static final JsonElement INACTIVE = JsonParser.parseString("{\"active\":\"inactive\"}");

    @Test
    void testSceneSorting() {
        List<Resource> list = new ArrayList<>();
        list.add(new Resource(ResourceType.LIGHT).setId("0"));
        list.add(new Resource(ResourceType.SCENE).setId("1").setStatus(ACTIVE));
        list.add(new Resource(ResourceType.LIGHT).setId("2"));
        list.add(new Resource(ResourceType.SCENE).setId("3").setStatus(INACTIVE));
        list.add(new Resource(ResourceType.LIGHT).setId("4"));
        list.forEach(r -> r.markAsSparse());

        assertEquals(5, list.size());
        assertEquals("0", list.get(0).getId());
        assertEquals("1", list.get(1).getId());
        assertEquals("2", list.get(2).getId());
        assertEquals("3", list.get(3).getId());
        assertEquals("4", list.get(4).getId());

        assertTrue(list.get(1).getSceneActive().get());
        assertFalse(list.get(3).getSceneActive().get());

        Setters.sortSceneResources(list);

        assertEquals(5, list.size());
        assertEquals("0", list.get(0).getId());
        assertEquals("2", list.get(1).getId());
        assertEquals("3", list.get(2).getId());
        assertEquals("4", list.get(3).getId());
        assertEquals("1", list.get(4).getId());

        assertFalse(list.get(2).getSceneActive().get());
        assertTrue(list.get(4).getSceneActive().get());
    }
}

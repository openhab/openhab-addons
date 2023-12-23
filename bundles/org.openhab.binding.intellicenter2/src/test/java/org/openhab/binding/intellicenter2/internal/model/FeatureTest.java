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
package org.openhab.binding.intellicenter2.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.intellicenter2.internal.protocol.ICProtocol.GSON;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.intellicenter2.internal.protocol.ICResponse;

/**
 * @author Valdis Rigdon - Initial contribution
 */
@NonNullByDefault
public class FeatureTest {

    public FeatureTest() {
    }

    @Test
    public void testFeatureDeserialization() {

        String jsonResponse = "{\"command\":\"SendParamList\",\"messageID\":\"ffc6dca0-1d94-454d-84ba-decc5e1ec067\",\"response\":\"200\",\"objectList\":[{\"objnam\":\"FTR01\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"CIRCGRP\":\"CIRCGRP\",\"FEATR\":\"ON\",\"STATUS\":\"OFF\",\"SNAME\":\"Jets\",\"LISTORD\":\"1\"}},{\"objnam\":\"C0004\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"INTELLI\",\"CIRCGRP\":\"CIRCGRP\",\"FEATR\":\"ON\",\"STATUS\":\"OFF\",\"SNAME\":\"Spa Light\",\"LISTORD\":\"4\"}},{\"objnam\":\"C0002\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"CIRCGRP\":\"CIRCGRP\",\"FEATR\":\"ON\",\"STATUS\":\"OFF\",\"SNAME\":\"Air Blower\",\"LISTORD\":\"2\"}},{\"objnam\":\"C0003\",\"params\":{\"OBJTYP\":\"CIRCUIT\",\"SUBTYP\":\"GENERIC\",\"CIRCGRP\":\"CIRCGRP\",\"FEATR\":\"ON\",\"STATUS\":\"OFF\",\"SNAME\":\"Ultraviolet\",\"LISTORD\":\"3\"}}]}\n";

        var response = GSON.fromJson(jsonResponse, ICResponse.class);
        SortedSet<Circuit> features = response.getObjectList().stream().map(Circuit::new)
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(4, features.size());

        System.err.println(features);

        var array = features.toArray(new Circuit[0]);
        assertEquals("Jets", array[0].getSname());
        assertEquals("Air Blower", array[1].getSname());
        assertEquals("Ultraviolet", array[2].getSname());
        assertEquals("Spa Light", array[3].getSname());
    }
}

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
package org.openhab.binding.shelly.internal.api1;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
public class Shelly1ApiJsonDTOTest {

    @Test
    void fixDimmerJsonReplacesLightsWithDimmers() {
        String input = "{\"lights\":[{\"brightness\":50,\"ison\":true}]}";
        String result = Shelly1ApiJsonDTO.fixDimmerJson(input);
        assertThat(result, containsString("\"dimmers\":["));
        assertThat(result, not(containsString("\"lights\":[")));
    }

    @Test
    void fixDimmerJsonNoOpWhenAlreadyDimmers() {
        String input = "{\"dimmers\":[{\"brightness\":50,\"ison\":true}]}";
        String result = Shelly1ApiJsonDTO.fixDimmerJson(input);
        assertThat(result, is(equalTo(input)));
    }

    @Test
    void fixDimmerJsonNoOpWhenEmpty() {
        String input = "{}";
        String result = Shelly1ApiJsonDTO.fixDimmerJson(input);
        assertThat(result, is(equalTo(input)));
    }

    @Test
    void fixDimmerJsonReplacesFirstOccurrenceOnly() {
        // Embed a second "lights":[ occurrence inside a nested object to ensure only the first is replaced
        String input = "{\"lights\":[{\"brightness\":50}],\"other\":{\"lights\":[{\"brightness\":80}]}}";
        String result = Shelly1ApiJsonDTO.fixDimmerJson(input);
        assertThat(result, containsString("\"dimmers\":["));
        // The second occurrence is inside a nested key so it should remain untouched
        assertThat(result, containsString("\"lights\":[{\"brightness\":80}]"));
    }
}

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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

/**
 * Unit tests for {@link BTHomeDecoder}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BTHomeDecoderTest {

    @Test
    void decodesSingleScalarObject() {
        JsonObject result = BTHomeDecoder.decode("0155");
        assertThat(result.get("Battery").getAsInt(), is(equalTo(85)));
    }

    @Test
    void decodesScaledSignedObject() {
        JsonObject result = BTHomeDecoder.decode("026608");
        assertThat(result.get("Temperature").getAsDouble(), is(equalTo(21.50)));
    }

    @Test
    void decodesNegativeSignedObject() {
        JsonObject result = BTHomeDecoder.decode("57f6");
        assertThat(result.get("Temperature").getAsInt(), is(equalTo(-10)));
    }

    @Test
    void stopsOnUnknownObjectIdAndReportsCode() {
        JsonObject result = BTHomeDecoder.decode("015566");
        assertThat(result.get("Battery").getAsInt(), is(equalTo(85)));
        assertThat(result.get("code").getAsString(), is(equalTo("BTH_UNKNOWN_TYPE")));
    }

    @Test
    void accumulatesRepeatedArrayCapableObjectIdIntoArray() {
        JsonObject result = BTHomeDecoder.decode("3f0a003f1400");
        assertThat(result.get("Rotation").getAsJsonArray().size(), is(equalTo(2)));
        assertThat(result.get("Rotation").getAsJsonArray().get(0).getAsDouble(), is(equalTo(1.0)));
        assertThat(result.get("Rotation").getAsJsonArray().get(1).getAsDouble(), is(equalTo(2.0)));
    }

    @Test
    void repeatedNonArrayCapableObjectIdOverwritesInsteadOfArray() {
        JsonObject result = BTHomeDecoder.decode("0155014b");
        assertThat(result.get("Battery").isJsonArray(), is(false));
        assertThat(result.get("Battery").getAsInt(), is(equalTo(75)));
    }

    @Test
    void decodesDimmerAsDirectionAndSteps() {
        JsonObject result = BTHomeDecoder.decode("3c0103");
        JsonObject dimmer = result.get("Dimmer").getAsJsonObject();
        assertThat(dimmer.get("direction").getAsInt(), is(equalTo(1)));
        assertThat(dimmer.get("steps").getAsInt(), is(equalTo(3)));
    }

    @Test
    void stopsCleanlyOnTruncatedTrailingObject() {
        JsonObject result = BTHomeDecoder.decode("015502");
        assertThat(result.get("Battery").getAsInt(), is(equalTo(85)));
        assertThat(result.has("Temperature"), is(false));
    }

    @Test
    void returnsEmptyResultOnOddLengthHex() {
        JsonObject result = BTHomeDecoder.decode("015");
        assertThat(result.entrySet().isEmpty(), is(true));
    }

    @Test
    void returnsEmptyResultOnInvalidHexCharacter() {
        JsonObject result = BTHomeDecoder.decode("01zz");
        assertThat(result.entrySet().isEmpty(), is(true));
    }

    @Test
    void decodesVariableLengthTextAndRawObjects() {
        JsonObject result = BTHomeDecoder.decode("530241425401ff");
        assertThat(result.get("bthText").getAsString(), is(equalTo("AB")));
        assertThat(result.get("bthRaw").getAsString(), is(equalTo("ff")));
        assertThat(result.has("text"), is(false));
        assertThat(result.has("raw"), is(false));
    }

    @Test
    void textAndRawObjectsPreserveAlignmentOfFollowingObject() {
        JsonObject result = BTHomeDecoder.decode("5301410155");
        assertThat(result.get("bthText").getAsString(), is(equalTo("A")));
        assertThat(result.get("Battery").getAsInt(), is(equalTo(85)));
    }
}

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
package org.openhab.binding.digitalstrom.internal.lib.util;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Test class for {@link DSJsonParser}
 *
 * @author Rouven Schürch - Initial contribution
 *
 */
@NonNullByDefault
class DSJsonParserTest {

    @Test
    void testParseSingleOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList(new OutputChannel(OutputChannelEnum.BRIGHTNESS)));

        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels, contains(OutputChannelEnum.BRIGHTNESS));
    }

    @Test
    void testParseMultipleOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList(new OutputChannel(OutputChannelEnum.BRIGHTNESS),
                new OutputChannel(OutputChannelEnum.AIR_FLAP_POSITION)));

        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels, contains(OutputChannelEnum.BRIGHTNESS, OutputChannelEnum.AIR_FLAP_POSITION));
    }

    @Test
    void testParseNoOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList());
        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels, hasSize(0));
    }

    private static JsonObject createJsonObject(List<OutputChannel> channels) {
        JsonModel model = new JsonModel(channels);

        Gson gson = new Gson();
        String json = gson.toJson(model);

        return JsonParser.parseString(json).getAsJsonObject();
    }
}

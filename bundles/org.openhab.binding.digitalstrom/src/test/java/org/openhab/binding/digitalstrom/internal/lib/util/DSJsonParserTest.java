package org.openhab.binding.digitalstrom.internal.lib.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants.OutputChannelEnum;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class DSJsonParserTest {

    @Test
    void testParseSingleOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList(new OutputChannel(OutputChannelEnum.BRIGHTNESS)));

        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels).containsOnly(OutputChannelEnum.BRIGHTNESS);
    }

    @Test
    void testParseMultipleOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList(new OutputChannel(OutputChannelEnum.BRIGHTNESS),
                new OutputChannel(OutputChannelEnum.AIR_FLAP_POSITION)));

        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels).containsOnly(OutputChannelEnum.BRIGHTNESS, OutputChannelEnum.AIR_FLAP_POSITION);
    }

    @Test
    void testParseNoOutputchannels() {
        JsonObject jsonObject = createJsonObject(Arrays.asList());
        List<OutputChannelEnum> channels = DSJsonParser.getOutputChannels(jsonObject);
        assertThat(channels).isEmpty();
    }

    private static JsonObject createJsonObject(List<OutputChannel> channels) {
        JsonModel model = new JsonModel(channels);

        Gson gson = new Gson();
        String json = gson.toJson(model);

        return JsonParser.parseString(json).getAsJsonObject();
    }
}

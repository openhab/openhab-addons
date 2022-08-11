package org.openhab.binding.liquidcheck.internal.json;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class ResponseTest {

    Response response;

    @BeforeEach
    void setUp() {
        String json = "{\"header\":{\"namespace\":\"Device\",\"name\":\"Response\",\"messageId\":\"499C7D21-F9579A3C\",   \"payloadVersion\":\"1\",\"authorization\":\"1C9DC262BE70-00038BC8-TX0K103HIXCXVLTBMVKVXFF\"},\"payload\":{\"measure\":{\"level\":2.23,\"content\":9265,\"raw\":{\"level\":2.2276,\"content\":9255.3193},\"age\":1981},\"expansion\":{\"boardType\":-1,\"oneWire\":null,\"board\":null},\"device\":{\"firmware\":\"1.60\",\"hardware\":\"B5\",\"name\":\"Liquid-Check\",\"model\":{\"name\":\"\",\"number\":1},\"manufacturer\":\"SI-Elektronik GmbH\",\"uuid\":\"0ba64a0c-7a88b168-0001\",\"security\":{\"code\":\"gkzQ5uGo6ElSdUsDWKQu2A==\"}},\"system\":{\"error\":0,\"uptime\":232392,\"pump\":{\"totalRuns\":351,\"totalRuntime\":1249}},\"wifi\":{\"station\":{\"hostname\":\"Liquid-Check\",\"ip\":\"192.168.2.102\",\"gateway\":\"192.168.2.1\",\"netmask\":\"255.255.255.0\",\"mac\":\"1C:9D:C2:62:BE:70\"},\"accessPoint\":{\"ssid\":\"WLAN-267994\",\"bssid\":\"4C:09:D4:2B:C3:97\",\"rssi\":-45}}}}";
        response = new Gson().fromJson(json, Response.class);
    }

    @Test
    public void jsonTest() {

        assertThat(response.header.namespace, is(equalTo("Device")));
        assertThat(response.header.name, is(equalTo("Response")));
        assertThat(response.header.messageId, is(equalTo("499C7D21-F9579A3C")));
        assertThat(response.header.payloadVersion, is(equalTo("1")));
        assertThat(response.header.authorization, is(equalTo("1C9DC262BE70-00038BC8-TX0K103HIXCXVLTBMVKVXFF")));
        assertThat(response.payload.measure.level, is(equalTo(2.23)));
        assertThat(response.payload.measure.content, is(equalTo(9265)));
        assertThat(response.payload.measure.age, is(equalTo(1981)));
        assertThat(response.payload.measure.raw.level, is(equalTo(2.2276)));
        assertThat(response.payload.measure.raw.content, is(equalTo(9255.3193)));
        assertThat(response.payload.expansion.boardType, is(equalTo(-1)));
        assertThat(response.payload.expansion.oneWire, is(equalTo(null)));
        assertThat(response.payload.expansion.board, is(equalTo(null)));
        assertThat(response.payload.device.firmware, is(equalTo("1.60")));
        assertThat(response.payload.device.hardware, is(equalTo("B5")));
        assertThat(response.payload.device.name, is(equalTo("Liquid-Check")));
        assertThat(response.payload.device.manufacturer, is(equalTo("SI-Elektronik GmbH")));
        assertThat(response.payload.device.uuid, is(equalTo("0ba64a0c-7a88b168-0001")));
        assertThat(response.payload.device.model.name, is(equalTo("")));
        assertThat(response.payload.device.model.number, is(equalTo(1)));
        assertThat(response.payload.device.security.code, is(equalTo("gkzQ5uGo6ElSdUsDWKQu2A==")));
        assertThat(response.payload.system.error, is(equalTo(0)));
        assertThat(response.payload.system.uptime, is(equalTo(232392)));
        assertThat(response.payload.system.pump.totalRuns, is(equalTo(351)));
        assertThat(response.payload.system.pump.totalRuntime, is(equalTo(1249)));
        assertThat(response.payload.wifi.station.hostname, is(equalTo("Liquid-Check")));
        assertThat(response.payload.wifi.station.ip, is(equalTo("192.168.2.102")));
        assertThat(response.payload.wifi.station.gateway, is(equalTo("192.168.2.1")));
        assertThat(response.payload.wifi.station.netmask, is(equalTo("255.255.255.0")));
        assertThat(response.payload.wifi.station.mac, is(equalTo("1C:9D:C2:62:BE:70")));
        assertThat(response.payload.wifi.accessPoint.ssid, is(equalTo("WLAN-267994")));
        assertThat(response.payload.wifi.accessPoint.bssid, is(equalTo("4C:09:D4:2B:C3:97")));
        assertThat(response.payload.wifi.accessPoint.rssi, is(equalTo(-45)));
    }
}

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
package org.openhab.binding.ipcamera.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAbilityResponse;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAbilityResponse.Value.Ability;
import org.openhab.binding.ipcamera.internal.ReolinkState.GetAbilityResponse.Value.Ability.AbilityChn;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Tests for {@link ReolinkStatus} and the per channel ability lookup, using replies of a Reolink Home Hub.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
public class ReolinkStatusTest {

    private static JsonObject value(String reply) {
        JsonObject value = ReolinkStatus.findValue(reply);
        assertNotNull(value);
        return value;
    }

    @Test
    public void parseChannelStatus() {
        JsonObject value = value("[{\"cmd\":\"GetChannelstatus\",\"code\":0,\"value\":{\"count\":8,\"status\":["
                + "{\"channel\":0,\"name\":\"cam1\",\"online\":1,\"sleep\":1,\"uid\":\"x\"},"
                + "{\"channel\":2,\"name\":\"bell\",\"online\":1,\"sleep\":0,\"uid\":\"y\"},"
                + "{\"channel\":3,\"name\":\"\",\"online\":0,\"sleep\":0,\"uid\":\"\"}]}}]");
        int[] cam1 = ReolinkStatus.parseChannelStatus(value, 0);
        assertNotNull(cam1);
        assertEquals(1, cam1[0]);
        assertEquals(1, cam1[1]);
        int[] bell = ReolinkStatus.parseChannelStatus(value, 2);
        assertNotNull(bell);
        assertEquals(0, bell[1]);
        assertNull(ReolinkStatus.parseChannelStatus(value, 5));
    }

    @Test
    public void parseBattery() {
        ReolinkStatus.BatteryInfo battery = ReolinkStatus.parseBattery(value(
                "[{\"cmd\":\"GetBatteryInfo\",\"code\":0,\"value\":{\"Battery\":{\"adapterStatus\":2,\"batteryPercent\":87,"
                        + "\"batteryVersion\":2,\"chargeStatus\":1,\"current\":-112,\"lowPowerFlag\":1,"
                        + "\"temperature\":27,\"voltage\":4054}}}]"));
        assertNotNull(battery);
        assertEquals(87, battery.percent());
        assertTrue(battery.low());
        assertTrue(battery.charging());
    }

    @Test
    public void parseBatteryFullyChargedIsNotCharging() {
        ReolinkStatus.BatteryInfo battery = ReolinkStatus.parseBattery(
                value("[{\"cmd\":\"GetBatteryInfo\",\"code\":0,\"value\":{\"Battery\":{\"batteryPercent\":100,"
                        + "\"chargeStatus\":2,\"lowPowerFlag\":0}}}]"));
        assertNotNull(battery);
        assertFalse(battery.charging());
        assertFalse(battery.low());
    }

    @Test
    public void parseStorage() {
        ReolinkStatus.StorageInfo storage = ReolinkStatus
                .parseStorage(value("[{\"cmd\":\"GetHddInfo\",\"code\":0,\"value\":{\"HddInfo\":["
                        + "{\"capacity\":119833,\"format\":1,\"mount\":1,\"number\":0,\"size\":8150,\"storageType\":2},"
                        + "{\"capacity\":119833,\"format\":1,\"mount\":1,\"number\":1,\"size\":5971,\"storageType\":2}]}}]"));
        assertNotNull(storage);
        assertEquals(94, storage.usedPercent());
        assertFalse(storage.error());
    }

    @Test
    public void parseStorageReportsUnmountedDevice() {
        ReolinkStatus.StorageInfo storage = ReolinkStatus
                .parseStorage(value("[{\"cmd\":\"GetHddInfo\",\"code\":0,\"value\":{\"HddInfo\":["
                        + "{\"capacity\":0,\"format\":0,\"mount\":0,\"number\":0,\"size\":0}]}}]"));
        assertNotNull(storage);
        assertTrue(storage.error());
        assertNull(
                ReolinkStatus.parseStorage(value("[{\"cmd\":\"GetHddInfo\",\"code\":0,\"value\":{\"HddInfo\":[]}}]")));
    }

    @Test
    public void parseVolume() {
        assertEquals(Integer.valueOf(55),
                ReolinkStatus.parseVolume(value("[{\"cmd\":\"GetAudioCfg\",\"code\":0,\"value\":"
                        + "{\"AudioCfg\":{\"visitorLoudspeaker\":1,\"volume\":55}}}]")));
    }

    @Test
    public void parseAudioAlarmEnable() {
        String prefix = "[{\"cmd\":\"GetAudioAlarmV20\",\"code\":0,";
        String table = "{\"AI_PEOPLE\":\"1111\",\"AI_VEHICLE\":\"0000\"}";
        assertEquals(Boolean.TRUE, ReolinkStatus.parseAudioAlarmEnable(value(prefix
                + "\"value\":{\"Audio\":{\"enable\":1,\"schedule\":{\"channel\":0,\"table\":" + table + "}}}}]")));
        // action 1 adds the defaults in "initial", only "value" must be used
        assertEquals(Boolean.FALSE, ReolinkStatus.parseAudioAlarmEnable(
                value(prefix + "\"initial\":{\"Audio\":{\"enable\":1}},\"value\":{\"Audio\":{\"enable\":0}}}]")));
        assertNull(ReolinkStatus.parseAudioAlarmEnable(value(prefix + "\"value\":{}}]")));
    }

    @Test
    public void errorsAreDistinguished() {
        String notSupported = "[{\"cmd\":\"GetAutoReply\",\"code\":1,\"error\":{\"detail\":\"not support\",\"rspCode\":-9}}]";
        String loginExpired = "[{\"cmd\":\"GetHddInfo\",\"code\":1,\"error\":{\"detail\":\"please login first\",\"rspCode\":-6}}]";
        assertNull(ReolinkStatus.findValue(notSupported));
        assertTrue(ReolinkStatus.isNotSupported(notSupported));
        assertNull(ReolinkStatus.findValue(loginExpired));
        assertFalse(ReolinkStatus.isNotSupported(loginExpired));
        assertFalse(ReolinkStatus.isNotSupported("garbage"));
    }

    @Test
    public void loginRequiredIsDetected() {
        String pretty = "[\n{\n\"cmd\" : \"GetAbility\",\n\"code\" : 1,\n\"error\" :\n{\n"
                + "\"detail\" : \"please login first\",\n\"rspCode\" : -6\n}\n}\n]";
        assertTrue(ReolinkStatus.isLoginRequired(pretty));
        assertFalse(ReolinkStatus.isLoginRequired(
                "[{\"cmd\":\"GetAutoReply\",\"code\":1,\"error\":{\"detail\":\"not support\",\"rspCode\":-9}}]"));
        assertFalse(ReolinkStatus.isLoginRequired("[{\"cmd\":\"GetAudioCfg\",\"code\":0,\"value\":{}}]"));
        assertFalse(ReolinkStatus.isLoginRequired("\u00ff\u00d8 binary"));
    }

    @Test
    public void statusPollCycles() {
        assertEquals(0, ReolinkStatus.statusPollCycles(0));
        assertEquals(0, ReolinkStatus.statusPollCycles(-5));
        assertEquals(1, ReolinkStatus.statusPollCycles(1));
        assertEquals(1, ReolinkStatus.statusPollCycles(8));
        assertEquals(8, ReolinkStatus.statusPollCycles(60));
        assertEquals(38, ReolinkStatus.statusPollCycles(300));
    }

    @Test
    public void sirenAbilityIsReadPerChannel() {
        // Home Hub reply (shortened): the siren is only reported per channel, with a version but without permit
        String reply = "[{\"cmd\":\"GetAbility\",\"code\":0,\"value\":{\"Ability\":{"
                + "\"supportAudioAlarmEnable\":{\"permit\":0,\"ver\":0},\"abilityChn\":["
                + "{\"supportAudioAlarm\":{\"permit\":0,\"ver\":1}},"
                + "{\"supportAudioAlarm\":{\"permit\":0,\"ver\":0}}]}}}]";
        GetAbilityResponse[] response = new Gson().fromJson(reply, GetAbilityResponse[].class);
        assertNotNull(response);
        Ability ability = response[0].value.ability;
        assertTrue(ReolinkHandler.isSupported(ReolinkHandler.channelAbility(ability, 0).supportAudioAlarm));
        assertFalse(ReolinkHandler.isSupported(ReolinkHandler.channelAbility(ability, 1).supportAudioAlarm));
        assertFalse(ReolinkHandler.isSupported(null));
    }

    @Test
    public void channelAbilityUsesEntryOfConfiguredChannel() {
        String reply = "[{\"cmd\":\"GetAbility\",\"code\":0,\"value\":{\"Ability\":{\"abilityChn\":["
                + "{\"ptzType\":{\"permit\":6,\"ver\":3},\"battery\":{\"permit\":6,\"ver\":2}},"
                + "{\"ptzType\":{\"permit\":0,\"ver\":0},\"battery\":{\"permit\":6,\"ver\":2}},"
                + "{\"ptzType\":{\"permit\":0,\"ver\":0},\"supportAudioAlarm\":{\"permit\":6,\"ver\":3}}]}}}]";
        GetAbilityResponse[] response = new Gson().fromJson(reply, GetAbilityResponse[].class);
        assertNotNull(response);
        AbilityChn ch0 = ReolinkHandler.channelAbility(response[0].value.ability, 0);
        AbilityChn ch2 = ReolinkHandler.channelAbility(response[0].value.ability, 2);
        AbilityChn outOfRange = ReolinkHandler.channelAbility(response[0].value.ability, 9);
        assertEquals(3, ch0.ptzType.ver);
        assertEquals(0, ch2.ptzType.ver);
        assertEquals(3, ch2.supportAudioAlarm.ver);
        assertEquals(3, outOfRange.ptzType.ver);
    }
}

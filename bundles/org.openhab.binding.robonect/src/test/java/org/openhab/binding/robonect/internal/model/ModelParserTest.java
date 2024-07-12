/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The goal of this class is to test the model parser to make sure the structures
 * returned from the module can be handled.
 *
 * @author Marco Meyer - Initial contribution
 */
public class ModelParserTest {

    private ModelParser subject;

    @BeforeEach
    public void setUp() {
        subject = new ModelParser();
    }

    @Test
    public void shouldParseSimpleSuccessModel() {
        String correctModel = "{\"successful\": true}";
        RobonectAnswer answer = subject.parse(correctModel, RobonectAnswer.class);
        assertTrue(answer.isSuccessful());
        assertNull(answer.getErrorMessage());
        assertNull(answer.getErrorCode());
    }

    @Test
    public void shouldParseErrorResponseOnAllResponseTypes() {
        String correctModel = "{\"successful\": false, \"error_code\": 7, \"error_message\": \"Automower already stopped\"}";
        RobonectAnswer answer = subject.parse(correctModel, RobonectAnswer.class);
        assertFalse(answer.isSuccessful());
        assertEquals(Integer.valueOf(7), answer.getErrorCode());
        assertEquals("Automower already stopped", answer.getErrorMessage());

        MowerInfo info = subject.parse(correctModel, MowerInfo.class);
        assertFalse(info.isSuccessful());
        assertEquals(Integer.valueOf(7), info.getErrorCode());
        assertEquals("Automower already stopped", info.getErrorMessage());
    }

    @Test
    public void shouldParseCorrectStatusModel() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 17, \"stopped\": false, \"duration\": 4359, \"mode\": 0, \"battery\": 100, \"hours\": 29}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"01.05.2017\", \"time\": \"19:00:00\", \"unix\": 1493665200}}, \"wlan\": {\"signal\": -76}}";
        MowerInfo mowerInfo = subject.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Mein Automower", mowerInfo.getName());
        assertEquals(MowerStatus.SLEEPING, mowerInfo.getStatus().getStatus());
        assertFalse(mowerInfo.getStatus().isStopped());
        assertEquals(4359, mowerInfo.getStatus().getDuration());
        assertEquals(MowerMode.AUTO, mowerInfo.getStatus().getMode());
        assertEquals(100, mowerInfo.getStatus().getBattery());
        assertEquals(29, mowerInfo.getStatus().getHours());
        assertEquals(Timer.TimerMode.STANDBY, mowerInfo.getTimer().getStatus());
        assertEquals("01.05.2017", mowerInfo.getTimer().getNext().getDate());
        assertEquals("19:00:00", mowerInfo.getTimer().getNext().getTime());
        assertEquals("1493665200", mowerInfo.getTimer().getNext().getUnix());
        assertEquals(-76, mowerInfo.getWlan().getSignal());
        assertNull(mowerInfo.getError());
    }

    @Test
    public void shouldParseCorrectStatusModelWithHealth() {
        String correctModel = "{ \"successful\": true, \"name\": \"Rosenlund Automower\", \"status\": { \"status\": 4, \"stopped\": false, \"duration\": 47493, \"mode\": 0, \"battery\": 20, \"hours\": 991 }, \"timer\": { \"status\": 2, \"next\": { \"date\": \"30.07.2017\", \"time\": \"13:00:00\", \"unix\": 1501419600 } }, \"blades\": {\"quality\": 9, \"hours\": 183, \"days\": 76}, \"wlan\": { \"signal\": -66 }, \"health\": { \"temperature\": 28, \"humidity\": 32 } }";
        MowerInfo mowerInfo = subject.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Rosenlund Automower", mowerInfo.getName());
        assertEquals(MowerStatus.CHARGING, mowerInfo.getStatus().getStatus());
        assertFalse(mowerInfo.getStatus().isStopped());
        assertEquals(47493, mowerInfo.getStatus().getDuration());
        assertEquals(MowerMode.AUTO, mowerInfo.getStatus().getMode());
        assertEquals(20, mowerInfo.getStatus().getBattery());
        assertEquals(991, mowerInfo.getStatus().getHours());
        assertEquals(Timer.TimerMode.STANDBY, mowerInfo.getTimer().getStatus());
        assertEquals("30.07.2017", mowerInfo.getTimer().getNext().getDate());
        assertEquals("13:00:00", mowerInfo.getTimer().getNext().getTime());
        assertEquals("1501419600", mowerInfo.getTimer().getNext().getUnix());
        assertEquals(-66, mowerInfo.getWlan().getSignal());
        assertNull(mowerInfo.getError());
        assertNotNull(mowerInfo.getHealth());
        assertEquals(28, mowerInfo.getHealth().getTemperature());
        assertEquals(32, mowerInfo.getHealth().getHumidity());
        assertEquals(9, mowerInfo.getBlades().getQuality());
        assertEquals(76, mowerInfo.getBlades().getDays());
        assertEquals(183, mowerInfo.getBlades().getHours());
        // "health": { "temperature": 28, "humidity": 32 }
    }

    @Test
    public void shouldParseISOEncodedStatusModel() {
        byte[] responseBytesISO88591 = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"error\" : {\"error_code\": 15, \"error_message\": \"Utanför arbetsområdet\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, \"wlan\": {\"signal\": -75}}"
                .getBytes(StandardCharsets.ISO_8859_1);
        MowerInfo mowerInfo = subject.parse(new String(responseBytesISO88591, StandardCharsets.ISO_8859_1),
                MowerInfo.class);
        assertEquals("Utanför arbetsområdet", mowerInfo.getError().getErrorMessage());
    }

    @Test
    public void shouldParseCorrectStatusModelWithErrorCode() {
        String correctModel = "{\"successful\": true, \"name\": \"Grasi\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 423, \"mode\": 0, \"battery\": 83, \"hours\": 55}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"15.05.2017\", \"time\": \"19:00:00\", \"unix\": 1494874800}}, \"wlan\": {\"signal\": -76}, \"error\": {\"error_code\": 9, \"error_message\": \"Grasi ist eingeklemmt\", \"date\": \"13.05.2017\", \"time\": \"23:00:22\", \"unix\": 1494716422}}";
        MowerInfo mowerInfo = subject.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Grasi", mowerInfo.getName());
        assertEquals(MowerStatus.ERROR_STATUS, mowerInfo.getStatus().getStatus());
        assertTrue(mowerInfo.getStatus().isStopped());
        assertEquals(9, mowerInfo.getError().getErrorCode().intValue());
        assertEquals("Grasi ist eingeklemmt", mowerInfo.getError().getErrorMessage());
    }

    @Test
    public void shouldParseCorrectStatusModelMowing() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 2, \"stopped\": false, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"wlan\": {\"signal\": -75}}";
        MowerInfo mowerInfo = subject.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Mein Automower", mowerInfo.getName());
        assertEquals(MowerStatus.MOWING, mowerInfo.getStatus().getStatus());
        assertFalse(mowerInfo.getStatus().isStopped());
        assertEquals(MowerMode.MANUAL, mowerInfo.getStatus().getMode());
    }

    @Test
    public void shouldParseCorrectErrorModelInErrorState() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"error\" : {\"error_code\": 15, \"error_message\": \"Mein Automower ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, \"wlan\": {\"signal\": -75}}";
        MowerInfo mowerInfo = subject.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Mein Automower", mowerInfo.getName());
        assertEquals(MowerStatus.ERROR_STATUS, mowerInfo.getStatus().getStatus());
        assertTrue(mowerInfo.getStatus().isStopped());
        assertNotNull(mowerInfo.getError());
        assertEquals("Mein Automower ist angehoben", mowerInfo.getError().getErrorMessage());
        assertEquals(Integer.valueOf(15), mowerInfo.getError().getErrorCode());
        assertEquals("02.05.2017", mowerInfo.getError().getDate());
        assertEquals("20:36:43", mowerInfo.getError().getTime());
        assertEquals("1493757403", mowerInfo.getError().getUnix());
    }

    @Test
    public void shouldParseErrorsList() {
        String errorsListResponse = "{\"errors\": [{\"error_code\": 15, \"error_message\": \"Grasi ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"26.04.2017\", \"time\": \"21:31:18\", \"unix\": 1493242278}, {\"error_code\": 13, \"error_message\": \"Kein Antrieb\", \"date\": \"21.04.2017\", \"time\": \"20:17:22\", \"unix\": 1492805842}, {\"error_code\": 10, \"error_message\": \"Grasi ist umgedreht\", \"date\": \"20.04.2017\", \"time\": \"20:14:37\", \"unix\": 1492719277}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"12.04.2017\", \"time\": \"19:10:09\", \"unix\": 1492024209}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"22:59:35\", \"unix\": 1491865175}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"10.04.2017\", \"time\": \"21:21:55\", \"unix\": 1491859315}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"20:26:13\", \"unix\": 1491855973}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"09.04.2017\", \"time\": \"14:50:36\", \"unix\": 1491749436}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"09.04.2017\", \"time\": \"14:23:27\", \"unix\": 1491747807}], \"successful\": true}";
        ErrorList errorList = subject.parse(errorsListResponse, ErrorList.class);
        assertTrue(errorList.isSuccessful());
        assertEquals(10, errorList.getErrors().size());
        assertEquals(Integer.valueOf(15), errorList.getErrors().get(0).getErrorCode());
        assertEquals("Grasi ist angehoben", errorList.getErrors().get(0).getErrorMessage());
        assertEquals("02.05.2017", errorList.getErrors().get(0).getDate());
        assertEquals("20:36:43", errorList.getErrors().get(0).getTime());
        assertEquals("1493757403", errorList.getErrors().get(0).getUnix());
    }

    @Test
    public void shouldParseName() {
        String nameResponse = "{\"name\": \"Grasi\", \"successful\": true}";
        Name name = subject.parse(nameResponse, Name.class);
        assertTrue(name.isSuccessful());
        assertEquals("Grasi", name.getName());
    }

    @Test
    public void shouldParseVersionInfo() {
        String versionResponse = "{\"robonect\": {\"serial\": \"05D92D32-38355048-43203030\", \"version\": \"V0.9\", \"compiled\": \"2017-03-25 20:10:00\", \"comment\": \"V0.9c\"}, \"successful\": true}";
        VersionInfo versionInfo = subject.parse(versionResponse, VersionInfo.class);
        assertTrue(versionInfo.isSuccessful());
        assertEquals("05D92D32-38355048-43203030", versionInfo.getRobonect().getSerial());
        assertEquals("V0.9", versionInfo.getRobonect().getVersion());
        assertEquals("2017-03-25 20:10:00", versionInfo.getRobonect().getCompiled());
        assertEquals("V0.9c", versionInfo.getRobonect().getComment());
    }

    @Test
    public void shouldParseVersionInfoV1betaToNA() {
        String versionResponse = """
                {
                mower: {
                hardware: {
                serial: 170602001,
                production: "2017-02-07 15:12:00"
                },
                msw: {
                title: "420",
                version: "7.10.00",
                compiled: "2016-11-29 08:44:06"
                },
                sub: {
                version: "6.01.00"
                }
                },
                serial: "05D80037-39355548-43163930",
                bootloader: {
                version: "V0.4",
                compiled: "2016-10-22 01:12:00",
                comment: ""
                },
                wlan: {
                at-version: "V1.4.0",
                sdk-version: "V2.1.0"
                },
                application: {
                version: "V1.0",
                compiled: "2018-03-12 21:01:00",
                comment: "Release V1.0 Beta2"
                },
                successful: true
                }\
                """;
        VersionInfo versionInfo = subject.parse(versionResponse, VersionInfo.class);
        assertTrue(versionInfo.isSuccessful());
        assertEquals("n/a", versionInfo.getRobonect().getSerial());
        assertEquals("n/a", versionInfo.getRobonect().getVersion());
        assertEquals("n/a", versionInfo.getRobonect().getCompiled());
        assertEquals("n/a", versionInfo.getRobonect().getComment());
    }
}

package org.openhab.binding.robonect.model.org.openhab.binding.robonect.model;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.robonect.model.ErrorList;
import org.openhab.binding.robonect.model.ModelParser;
import org.openhab.binding.robonect.model.MowerInfo;
import org.openhab.binding.robonect.model.MowerMode;
import org.openhab.binding.robonect.model.MowerStatus;
import org.openhab.binding.robonect.model.Name;
import org.openhab.binding.robonect.model.RobonectAnswer;
import org.openhab.binding.robonect.model.Timer;
import org.openhab.binding.robonect.model.VersionInfo;

import static org.junit.Assert.*;

public class ModelParserTest {

    private ModelParser parser;

    @Before
    public void setUp() {
        parser = new ModelParser();
    }

    @Test
    public void shouldParseSimpleSuccessModel() {
        String correctModel = "{\"successful\": true}";
        RobonectAnswer answer = parser.parse(correctModel, RobonectAnswer.class);
        assertTrue(answer.isSuccessful());
        assertNull(answer.getErrorMessage());
        assertNull(answer.getErrorCode());
    }

    @Test
    public void shouldParseErrorResponseOnAllResponseTypes() {
        String correctModel = "{\"successful\": false, \"error_code\": 7, \"error_message\": \"Automower already stopped\"}";
        RobonectAnswer answer = parser.parse(correctModel, RobonectAnswer.class);
        assertFalse(answer.isSuccessful());
        assertEquals(new Integer(7), answer.getErrorCode());
        assertEquals("Automower already stopped", answer.getErrorMessage());

        MowerInfo info = parser.parse(correctModel, MowerInfo.class);
        assertFalse(info.isSuccessful());
        assertEquals(new Integer(7), info.getErrorCode());
        assertEquals("Automower already stopped", info.getErrorMessage());
    }

    @Test
    public void shouldParseCorrectStatusModel() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 17, \"stopped\": false, \"duration\": 4359, \"mode\": 0, \"battery\": 100, \"hours\": 29}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"01.05.2017\", \"time\": \"19:00:00\", \"unix\": 1493665200}}, \"wlan\": {\"signal\": -76}}";
        MowerInfo mowerInfo = parser.parse(correctModel, MowerInfo.class);
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
    public void shouldParseCorrectStatusModelMowing() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 2, \"stopped\": false, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"wlan\": {\"signal\": -75}}";
        MowerInfo mowerInfo = parser.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Mein Automower", mowerInfo.getName());
        assertEquals(MowerStatus.MOWING, mowerInfo.getStatus().getStatus());
        assertFalse(mowerInfo.getStatus().isStopped());
        assertEquals(MowerMode.MANUAL, mowerInfo.getStatus().getMode());

    }

    @Test
    public void shouldParseCorrectErrorModelInErrorState() {
        String correctModel = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"error\" : {\"error_code\": 15, \"error_message\": \"Mein Automower ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, \"wlan\": {\"signal\": -75}}";
        MowerInfo mowerInfo = parser.parse(correctModel, MowerInfo.class);
        assertTrue(mowerInfo.isSuccessful());
        assertEquals("Mein Automower", mowerInfo.getName());
        assertEquals(MowerStatus.ERROR_STATUS, mowerInfo.getStatus().getStatus());
        assertTrue(mowerInfo.getStatus().isStopped());
        assertNotNull(mowerInfo.getError());
        assertEquals("Mein Automower ist angehoben", mowerInfo.getError().getErrorMessage());
        assertEquals(new Integer(15), mowerInfo.getError().getErrorCode());
        assertEquals("02.05.2017", mowerInfo.getError().getDate());
        assertEquals("20:36:43", mowerInfo.getError().getTime());
        assertEquals("1493757403",mowerInfo.getError().getUnix());
    }

    @Test
    public void shouldParseErrorsList() {
        String errorsListResponse = "{\"errors\": [{\"error_code\": 15, \"error_message\": \"Grasi ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"26.04.2017\", \"time\": \"21:31:18\", \"unix\": 1493242278}, {\"error_code\": 13, \"error_message\": \"Kein Antrieb\", \"date\": \"21.04.2017\", \"time\": \"20:17:22\", \"unix\": 1492805842}, {\"error_code\": 10, \"error_message\": \"Grasi ist umgedreht\", \"date\": \"20.04.2017\", \"time\": \"20:14:37\", \"unix\": 1492719277}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"12.04.2017\", \"time\": \"19:10:09\", \"unix\": 1492024209}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"22:59:35\", \"unix\": 1491865175}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"10.04.2017\", \"time\": \"21:21:55\", \"unix\": 1491859315}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"20:26:13\", \"unix\": 1491855973}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"09.04.2017\", \"time\": \"14:50:36\", \"unix\": 1491749436}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"09.04.2017\", \"time\": \"14:23:27\", \"unix\": 1491747807}], \"successful\": true}";
        ErrorList errorList = parser.parse(errorsListResponse, ErrorList.class);
        assertTrue(errorList.isSuccessful());
        assertEquals(10, errorList.getErrors().size());
        assertEquals(new Integer(15), errorList.getErrors().get(0).getErrorCode());
        assertEquals("Grasi ist angehoben", errorList.getErrors().get(0).getErrorMessage());
        assertEquals("02.05.2017", errorList.getErrors().get(0).getDate());
        assertEquals("20:36:43", errorList.getErrors().get(0).getTime());
        assertEquals("1493757403", errorList.getErrors().get(0).getUnix());
    }

    @Test
    public void shouldParseName() {
        String nameResponse = "{\"name\": \"Grasi\", \"successful\": true}";
        Name name = parser.parse(nameResponse, Name.class);
        assertTrue(name.isSuccessful());
        assertEquals("Grasi", name.getName());
    }

    @Test
    public void shouldParseVersionInfo() {
        String versionResponse = "{\"robonect\": {\"serial\": \"05D92D32-38355048-43203030\", \"version\": \"V0.9\", \"compiled\": \"2017-03-25 20:10:00\", \"comment\": \"V0.9c\"}, \"successful\": true}";
        VersionInfo versionInfo = parser.parse(versionResponse, VersionInfo.class);
        assertTrue(versionInfo.isSuccessful());
        assertEquals("05D92D32-38355048-43203030", versionInfo.getRobonect().getSerial());
        assertEquals("V0.9", versionInfo.getRobonect().getVersion());
        assertEquals("2017-03-25 20:10:00", versionInfo.getRobonect().getCompiled());
        assertEquals("V0.9c", versionInfo.getRobonect().getComment());
    }
}

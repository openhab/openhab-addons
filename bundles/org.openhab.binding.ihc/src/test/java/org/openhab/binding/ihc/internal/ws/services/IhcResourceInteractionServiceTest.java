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
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ihc.internal.ws.ResourceFileUtils;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimeValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSWeekdayValue;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcResourceInteractionServiceTest {

    private IhcResourceInteractionService ihcResourceInteractionService;
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/ResourceInteractionService";

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcResourceInteractionService = spy(new IhcResourceInteractionService(host, 0, new IhcConnectionPool()));

        final String query = ResourceFileUtils.getFileContent("ResourceValueQueryTemplate.xml");
        final String response11111 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse11111.xml");
        final String response22222 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse22222.xml");
        final String response33333 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse33333.xml");
        final String response44444 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse44444.xml");
        final String response55555 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse55555.xml");
        final String response66666 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse66666.xml");
        final String response77777 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse77777.xml");
        final String response88888 = ResourceFileUtils.getFileContent("ResourceValueQueryResponse88888.xml");

        doReturn(response11111).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 11111)), anyInt());
        doReturn(response22222).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 22222)), anyInt());
        doReturn(response33333).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 33333)), anyInt());
        doReturn(response44444).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 44444)), anyInt());
        doReturn(response55555).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 55555)), anyInt());
        doReturn(response66666).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 66666)), anyInt());
        doReturn(response77777).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 77777)), anyInt());
        doReturn(response88888).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(String.format(query, 88888)), anyInt());

        final String updateOkResult = ResourceFileUtils.getFileContent("ResourceValueUpdateOkResult.xml");
        final String update100001 = ResourceFileUtils.getFileContent("ResourceValueUpdate100001.xml");
        final String update200002 = ResourceFileUtils.getFileContent("ResourceValueUpdate200002.xml");
        final String update300003 = ResourceFileUtils.getFileContent("ResourceValueUpdate300003.xml");
        final String update400004 = ResourceFileUtils.getFileContent("ResourceValueUpdate400004.xml");
        final String update500005 = ResourceFileUtils.getFileContent("ResourceValueUpdate500005.xml");
        final String update600006 = ResourceFileUtils.getFileContent("ResourceValueUpdate600006.xml");
        final String update700007 = ResourceFileUtils.getFileContent("ResourceValueUpdate700007.xml");
        final String update800008 = ResourceFileUtils.getFileContent("ResourceValueUpdate800008.xml");

        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update100001),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update200002),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update300003),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update400004),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update500005),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update600006),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update700007),
                anyInt());
        doReturn(updateOkResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update800008),
                anyInt());

        final String updateFailureResult = ResourceFileUtils.getFileContent("ResourceValueUpdateFailureResult.xml");
        final String update100011 = ResourceFileUtils.getFileContent("ResourceValueUpdate100011.xml");

        doReturn(updateFailureResult).when(ihcResourceInteractionService).sendQuery(eq(url), any(), eq(update100011),
                anyInt());

        final String resourceValueNotificationsQuery = ResourceFileUtils
                .getFileContent("ResourceValueNotificationsQuery.xml");
        final String resourceValueNotificationsResponse = ResourceFileUtils
                .getFileContent("ResourceValueNotificationsResponse.xml");

        doReturn(resourceValueNotificationsResponse).when(ihcResourceInteractionService).sendQuery(eq(url), any(),
                eq(resourceValueNotificationsQuery), anyInt());
    }

    @Test
    public void testWSBooleanValueQuery() throws IhcExecption {
        final WSBooleanValue val = (WSBooleanValue) ihcResourceInteractionService.resourceQuery(11111);
        assertEquals(11111, val.resourceID);
        assertEquals(true, val.value);
    }

    @Test
    public void testWSFloatingPointValueQuery() throws IhcExecption {
        final WSFloatingPointValue val = (WSFloatingPointValue) ihcResourceInteractionService.resourceQuery(22222);
        assertEquals(22222, val.resourceID);
        assertEquals(24.399999618530273, val.value, 0.000001);
        assertEquals(-1000.0, val.minimumValue, 0.01);
        assertEquals(1000.0, val.maximumValue, 0.01);
    }

    @Test
    public void testWSEnumValueQuery() throws IhcExecption {
        final WSEnumValue val = (WSEnumValue) ihcResourceInteractionService.resourceQuery(33333);
        assertEquals(33333, val.resourceID);
        assertEquals(4236359, val.definitionTypeID);
        assertEquals(4236872, val.enumValueID);
        assertEquals("test value", val.enumName);
    }

    @Test
    public void testWSIntegerValueQuery() throws IhcExecption {
        final WSIntegerValue val = (WSIntegerValue) ihcResourceInteractionService.resourceQuery(44444);
        assertEquals(44444, val.resourceID);
        assertEquals(424561, val.value);
        assertEquals(-2147483648, val.minimumValue);
        assertEquals(2147483647, val.maximumValue);
    }

    @Test
    public void testWSTimerValueQuery() throws IhcExecption {
        final WSTimerValue val = (WSTimerValue) ihcResourceInteractionService.resourceQuery(55555);
        assertEquals(55555, val.resourceID);
        assertEquals(13851, val.milliseconds);
    }

    @Test
    public void testWSWeekdayValueQuery() throws IhcExecption {
        final WSWeekdayValue val = (WSWeekdayValue) ihcResourceInteractionService.resourceQuery(66666);
        assertEquals(66666, val.resourceID);
        assertEquals(2, val.weekdayNumber);
    }

    @Test
    public void testWSDateValueQuery() throws IhcExecption {
        final WSDateValue val = (WSDateValue) ihcResourceInteractionService.resourceQuery(77777);
        assertEquals(77777, val.resourceID);
        assertEquals(2018, val.year);
        assertEquals(10, val.month);
        assertEquals(22, val.day);
    }

    @Test
    public void testWSTimeValueQuery() throws IhcExecption {
        final WSTimeValue val = (WSTimeValue) ihcResourceInteractionService.resourceQuery(88888);
        assertEquals(88888, val.resourceID);
        assertEquals(16, val.hours);
        assertEquals(32, val.minutes);
        assertEquals(45, val.seconds);
    }

    @Test
    public void testWSBooleanValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSBooleanValue(100001, true));
        assertTrue(result);
    }

    @Test
    public void testWSBooleanValueUpdateFailure() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSBooleanValue(100011, true));
        assertFalse(result);
    }

    @Test
    public void testWSFloatingPointValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService
                .resourceUpdate(new WSFloatingPointValue(200002, 24.1, -1000.0, 1000.0));
        assertTrue(result);
    }

    @Test
    public void testWSEnumValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSEnumValue(300003, 11111, 22222, "test123"));
        assertTrue(result);
    }

    @Test
    public void testWSIntegerValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSIntegerValue(400004, 201, -1000, 1000));
        assertTrue(result);
    }

    @Test
    public void testWSTimerValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSTimerValue(500005, 2134));
        assertTrue(result);
    }

    @Test
    public void testWSWeekdayValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSWeekdayValue(600006, 4));
        assertTrue(result);
    }

    @Test
    public void testWSDateValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService
                .resourceUpdate(new WSDateValue(700007, (short) 2018, (byte) 3, (byte) 24));
        assertTrue(result);
    }

    @Test
    public void testWSTimeValueUpdate() throws IhcExecption {
        boolean result = ihcResourceInteractionService.resourceUpdate(new WSTimeValue(800008, 15, 34, 45));
        assertTrue(result);
    }

    @Test
    public void testResourceValueNotifications() throws IhcExecption, SocketTimeoutException {
        final List<WSResourceValue> list = ihcResourceInteractionService.waitResourceValueNotifications(1);
        assertEquals(8, list.size());

        List<WSResourceValue> found = new ArrayList<>();

        for (WSResourceValue val : list) {
            switch (val.resourceID) {
                case 10454030:
                    assertEquals(2018, ((WSDateValue) val).year);
                    assertEquals(9, ((WSDateValue) val).month);
                    assertEquals(28, ((WSDateValue) val).day);
                    found.add(val);
                    break;
                case 10454541:
                    assertEquals(10, ((WSTimeValue) val).hours);
                    assertEquals(20, ((WSTimeValue) val).minutes);
                    assertEquals(30, ((WSTimeValue) val).seconds);
                    found.add(val);
                    break;
                case 10447883:
                    assertEquals(456789, ((WSIntegerValue) val).value);
                    assertEquals(-2147483648, ((WSIntegerValue) val).minimumValue);
                    assertEquals(2147483647, ((WSIntegerValue) val).maximumValue);
                    found.add(val);
                    break;
                case 4133210:
                    assertEquals(false, ((WSBooleanValue) val).value);
                    found.add(val);
                    break;
                case 3988827:
                    assertEquals(true, ((WSBooleanValue) val).value);
                    found.add(val);
                    break;
                case 4159252:
                    assertEquals(24.50, ((WSFloatingPointValue) val).value, 0.01);
                    assertEquals(-1000.00, ((WSFloatingPointValue) val).minimumValue, 0.01);
                    assertEquals(1000.00, ((WSFloatingPointValue) val).maximumValue, 0.01);
                    found.add(val);
                    break;
                case 3515401:
                    assertEquals(2, ((WSWeekdayValue) val).weekdayNumber);
                    found.add(val);
                    break;
                case 7419663:
                    assertEquals(4236359, ((WSEnumValue) val).definitionTypeID);
                    assertEquals(4236872, ((WSEnumValue) val).enumValueID);
                    assertEquals("testVal", ((WSEnumValue) val).enumName);
                    found.add(val);
                    break;
            }
        }
        assertEquals(8, found.size());
    }
}

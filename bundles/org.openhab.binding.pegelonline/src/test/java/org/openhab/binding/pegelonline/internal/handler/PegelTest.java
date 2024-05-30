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
package org.openhab.binding.pegelonline.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.GSON;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.jupiter.api.Test;
import org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants;
import org.openhab.binding.pegelonline.internal.dto.Measure;
import org.openhab.binding.pegelonline.internal.util.FileReader;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PegelTest} Test helper utils
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class PegelTest {
    private final Logger logger = LoggerFactory.getLogger(PegelTest.class);

    @Test
    void test() {
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        Measure measure = GSON.fromJson(content, Measure.class);
        if (measure != null) {
            DateTimeType dtt = DateTimeType.valueOf(measure.timestamp);
        } else {
            fail();
        }
    }

    @Test
    public void testWarningLevels() {
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        ContentResponse crMock = mock(ContentResponse.class);
        when(crMock.getStatus()).thenReturn(200);
        when(crMock.getContentAsString()).thenReturn(content);
        HttpClient httpClientMock = mock(HttpClient.class);
        try {
            when(httpClientMock.GET(anyString())).thenReturn(crMock);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail();
        }

        CallbackMock thc = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, httpClientMock);
        handler.setCallback(thc);
        handler.initialize();
        System.out.println("After init");
        Map<String, Object> config = new HashMap<>();
        handler.updateConfiguration(new Configuration(config));
        System.out.println("After config update");
        Measure m = new Measure();
        m.value = 500;
        assertEquals(PegelOnlineBindingConstants.NO_WARNING, handler.getWarnLevel(m), "No Warn Level");
        config.put("warningLevel1", 1000);
        config.put("warningLevel2", 2000);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();
        assertEquals(PegelOnlineBindingConstants.NO_WARNING, handler.getWarnLevel(m), "No Warn Level");
        m.value = 1200;
        assertEquals(PegelOnlineBindingConstants.WARN_LEVEL_1, handler.getWarnLevel(m), "Warn Level 1");
        m.value = 2100;
        assertEquals(PegelOnlineBindingConstants.WARN_LEVEL_2, handler.getWarnLevel(m), "Warn Level 2");
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

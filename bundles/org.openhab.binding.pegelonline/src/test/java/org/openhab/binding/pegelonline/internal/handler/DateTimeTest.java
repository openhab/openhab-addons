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
import static org.mockito.Mockito.mock;
import static org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants.GSON;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
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
 * The {@link DateTimeTest} Test helper utils
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class DateTimeTest {
    private final Logger logger = LoggerFactory.getLogger(DateTimeTest.class);

    @Test
    void test() {
        String content = FileReader.readFileInString("src/test/resources/measure.json");
        logger.info("Content {}", content);
        Measure measure = GSON.fromJson(content, Measure.class);
        if (measure != null) {
            DateTimeType dtt = DateTimeType.valueOf(measure.timestamp);
            logger.warn("DateTimeType {}", dtt.toFullString());
        } else {
            fail();
        }
    }

    @Test
    public void testWarningLevels() {
        CallbackMock thc = new CallbackMock();
        ThingImpl ti = new ThingImpl(new ThingTypeUID("pegelonline:station"), "test");
        PegelOnlineHandler handler = new PegelOnlineHandler(ti, mock(HttpClient.class));
        handler.setCallback(thc);
        handler.initialize();
        Map<String, Object> config = new HashedMap<>();
        handler.updateConfiguration(new Configuration(config));
        Measure m = new Measure();
        m.value = 500;
        assertEquals(PegelOnlineBindingConstants.NO_WARNING, handler.getWarnLevel(m), "No Warn Level");
        config.put("warningLevel1", 1000);
        config.put("warningLevel2", 2000);
        handler.updateConfiguration(new Configuration(config));
        handler.initialize();
        System.out.println("Warnlevel " + handler.getWarnLevel(m));
        m.value = 1200;
        System.out.println("Warnlevel " + handler.getWarnLevel(m));
        m.value = 2100;
        System.out.println("Warnlevel " + handler.getWarnLevel(m));
    }
}

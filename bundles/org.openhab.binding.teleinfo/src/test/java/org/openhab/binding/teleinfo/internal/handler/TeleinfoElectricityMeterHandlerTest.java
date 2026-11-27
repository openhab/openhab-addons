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
package org.openhab.binding.teleinfo.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.teleinfo.internal.TeleinfoBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.teleinfo.internal.data.Frame;
import org.openhab.binding.teleinfo.internal.reader.io.serialport.Label;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class TeleinfoElectricityMeterHandlerTest {

    @Test
    public void historicalFrameWithoutVoltageDoesNotWarn() {
        Frame frame = new Frame();
        frame.put(Label.ADCO, "meter");
        frame.put(Label.OPTARIF, "BASE");
        frame.put(Label.IINST, "3");
        frame.put(Label.PAPP, "600");
        assertVoltageWarning(frame, false);
    }

    @Test
    public void explicitZeroVoltageStillWarns() {
        Frame frame = new Frame();
        frame.put(Label.ADSC, "meter");
        frame.put(Label.URMS1, "0");
        frame.put(Label.SINSTS, "600");
        assertVoltageWarning(frame, true);
    }

    private void assertVoltageWarning(Frame frame, boolean expected) {
        Logger logger = (Logger) LoggerFactory.getLogger(TeleinfoElectricityMeterHandler.class);
        ListAppender<@NonNull ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            newHandler().onFrameReceived(frame);
            assertEquals(expected,
                    appender.list.stream().anyMatch(event -> event.getFormattedMessage().contains("urms is zero")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    private TeleinfoElectricityMeterHandler newHandler() {
        Thing thing = requireNonNull(mock(Thing.class));
        when(thing.getUID()).thenReturn(new ThingUID(THING_LSMM_ELECTRICITY_METER_TYPE_UID, "meter"));
        when(thing.getProperties()).thenReturn(Map.of());

        TeleinfoElectricityMeterHandler handler = new TeleinfoElectricityMeterHandler(thing);
        handler.configuration.setAdco("meter");
        handler.setCallback(mock(ThingHandlerCallback.class));
        return handler;
    }
}

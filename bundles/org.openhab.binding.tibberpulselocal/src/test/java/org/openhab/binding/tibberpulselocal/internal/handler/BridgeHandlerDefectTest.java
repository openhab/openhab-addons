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
package org.openhab.binding.tibberpulselocal.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.openhab.binding.tibberpulselocal.internal.handler.HandlerSupport.*;
import static org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibberpulselocal.internal.handler.HandlerSupport.Recorder;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlFrameBuilder;
import org.openhab.binding.tibberpulselocal.internal.sml.SmlReading;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * A defect in the processing of a message must not end the stream attempt and must not leak the message or the
 * credential into the log ({@code connection} spec, "Credential and identifier protection").
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BridgeHandlerDefectTest {

    private static final String LEAK = PASSWORD + SYNTHETIC_METER_ID;

    /** A meter that fails the way a defect would, with message content in the exception text. */
    private static final class FailingMeter extends MeterHandler {
        FailingMeter(Thing thing) {
            super(thing);
        }

        @Override
        void onReading(SmlReading reading) {
            throw new IllegalStateException(LEAK);
        }
    }

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger logger = (Logger) LoggerFactory.getLogger("ROOT");
    private Level previous = Level.INFO;
    private final List<StreamListener> listeners = new ArrayList<>();

    @BeforeEach
    void attach() {
        assumeTrue(LoggerFactory.getILoggerFactory() instanceof LoggerContext, "needs logback");
        logger = (Logger) LoggerFactory.getLogger("org.openhab.binding.tibberpulselocal");
        previous = logger.getLevel() != null ? logger.getLevel() : Level.INFO;
        logger.setLevel(Level.TRACE);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void detach() {
        logger.detachAppender(appender);
        logger.setLevel(previous);
        appender.stop();
    }

    @Test
    void whenAMeterFailsWhileApplyingAReadingThenTheMessageIsReportedAsInvalidAndTheLogHasNoMessageContent() {
        Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
        Recorder recorder = new Recorder(bridge, null);
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
            listeners.add(listener);
            return new StreamConnection() {
                @Override
                public void start() {
                }

                @Override
                public void stop() {
                }
            };
        });
        handler.setCallback(recorder.callback);
        bridge.setHandler(handler);
        handler.initialize();
        Thing thing = meter(bridge, "m1", DEVICE_A, null);
        MeterHandler failing = new FailingMeter(thing);
        thing.setHandler(failing);
        ((org.openhab.core.thing.internal.BridgeImpl) bridge).addThing(thing);
        failing.setCallback(new Recorder(thing, bridge).callback);
        failing.initialize();
        byte[] frame = new SmlFrameBuilder().signed(POWER, UNIT_W, 0, 5, 8)
                .unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.ONE, 8).build();

        boolean valid = listeners.get(0).onMessage(message(DEVICE_A, frame));

        assertFalse(valid);
        List<ILoggingEvent> warnings = appender.list.stream().filter(e -> e.getLevel() == Level.WARN).toList();
        assertEquals(1, warnings.size());
        for (ILoggingEvent event : appender.list) {
            String text = event.getFormattedMessage();
            assertFalse(text.contains(LEAK) || text.contains(PASSWORD) || text.contains(SYNTHETIC_METER_ID), text);
            assertNull(event.getThrowableProxy(), "no stack trace with the exception text");
        }
        failing.dispose();
        handler.dispose();
    }
}

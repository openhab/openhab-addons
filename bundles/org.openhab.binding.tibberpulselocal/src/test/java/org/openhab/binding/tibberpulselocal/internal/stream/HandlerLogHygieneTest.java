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
package org.openhab.binding.tibberpulselocal.internal.stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.openhab.binding.tibberpulselocal.internal.TibberPulseLocalBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibberpulselocal.internal.Fixtures;
import org.openhab.binding.tibberpulselocal.internal.stream.StreamClient.BackoffWaiter;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.read.ListAppender;

/**
 * Tests the scenarios of the {@code connection} spec, "Credential and identifier protection", for the handler layer:
 * the logs of the whole binding at TRACE while the handlers run through success, a rejected password, a refused
 * connection, and disposal, and every status description and thing property they publish.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(60)
class HandlerLogHygieneTest extends StreamTestSupport {

    private static final String SECRET = "s3cret-Pw";
    private static final BackoffWaiter IMMEDIATE = (delay, stopped) -> stopped.getCount() == 0;

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private Logger bindingLogger = (Logger) LoggerFactory.getLogger("ROOT");
    private Level previousLevel = Level.INFO;
    private final List<HandlerHarness> harnesses = new ArrayList<>();

    @BeforeEach
    void attachAppender() {
        assumeTrue(LoggerFactory.getILoggerFactory() instanceof LoggerContext, "needs logback");
        bindingLogger = (Logger) LoggerFactory.getLogger("org.openhab.binding.tibberpulselocal");
        previousLevel = bindingLogger.getLevel() != null ? bindingLogger.getLevel() : Level.INFO;
        bindingLogger.setLevel(Level.TRACE);
        appender.start();
        bindingLogger.addAppender(appender);
    }

    @AfterEach
    void detachAppender() {
        harnesses.forEach(HandlerHarness::dispose);
        bindingLogger.detachAppender(appender);
        bindingLogger.setLevel(previousLevel);
        appender.stop();
    }

    private static String render(ILoggingEvent event) {
        StringBuilder text = new StringBuilder(event.getFormattedMessage());
        for (IThrowableProxy proxy = event.getThrowableProxy(); proxy != null; proxy = proxy.getCause()) {
            text.append(' ').append(proxy.getClassName()).append(' ').append(proxy.getMessage());
        }
        return text.toString();
    }

    private static void assertNoSecrets(String where, String text) {
        String basic = Base64.getEncoder().encodeToString(("admin:" + SECRET).getBytes(StandardCharsets.UTF_8));
        String bare = Base64.getEncoder().encodeToString(SECRET.getBytes(StandardCharsets.UTF_8));
        assertFalse(text.contains(SECRET), where + " contains the password");
        assertFalse(text.contains(basic), where + " contains the Authorization credential");
        assertFalse(text.contains(bare), where + " contains the Base64 of the password");
        assertFalse(text.toLowerCase(Locale.ROOT).contains("authorization"),
                where + " contains the Authorization header");
        assertFalse(text.contains(Fixtures.METER_ID), where + " contains the meter id");
        assertFalse(text.contains(Fixtures.DEVICE_ID), where + " contains the device id");
    }

    private HandlerHarness start(int port) {
        HandlerHarness harness = new HandlerHarness("bridge.example", SECRET, Fixtures.DEVICE_ID,
                HandlerHarness.factory(this, () -> port, LONG_TIMING, IMMEDIATE));
        harnesses.add(harness);
        harness.initialize();
        return harness;
    }

    @Test
    void whenTheHandlersRunThroughSuccessRejectionRefusalAndDisposalThenNoSecretReachesLogsStatusesOrProperties()
            throws Exception {
        List<String> published = new ArrayList<>();

        // success: readings for the configured meter, then disposal
        FakeBridge good = bridge(FakeBridge.sending(FrameBytes.concat(validWire(), validWire())));
        HandlerHarness running = start(good.port());
        HandlerHarness.await(running.meterStatuses, i -> i.getStatus() == ThingStatus.ONLINE, WAIT_MILLIS);
        running.awaitMeterState(CHANNEL_POWER, s -> true, WAIT_MILLIS);
        running.meter.getProperties().forEach((k, v) -> {
            published.add("property " + k + " of the meter");
            assertFalse(v.contains(SECRET), "property " + k + " contains the password");
        });
        running.bridge.getProperties().forEach((k, v) -> assertFalse(v.contains(SECRET)));
        running.dispose();

        // rejection
        FakeBridge rejecting = bridge(FakeBridge.answeringThenClosing("HTTP/1.1 401 Unauthorized\r\n\r\n"));
        HandlerHarness rejected = start(rejecting.port());
        ThingStatusInfo rejection = HandlerHarness.await(rejected.bridgeStatuses,
                i -> i.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR, WAIT_MILLIS);
        rejected.dispose();

        // refusal
        HandlerHarness refused = start(closedPort());
        ThingStatusInfo refusal = HandlerHarness.await(refused.bridgeStatuses,
                i -> i.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR, WAIT_MILLIS);
        refused.dispose();

        // The status texts are keys, so the check is on the final text parts too.
        for (ThingStatusInfo info : List.of(rejection, refusal)) {
            assertNoSecrets("status " + info, String.valueOf(info.getDescription()));
        }
        for (HandlerHarness harness : harnesses) {
            for (ThingStatusInfo info : harness.bridgeStatuses) {
                assertNoSecrets("bridge status " + info, String.valueOf(info.getDescription()));
            }
            for (ThingStatusInfo info : harness.meterStatuses) {
                assertNoSecrets("meter status " + info, String.valueOf(info.getDescription()));
            }
        }
        List<ILoggingEvent> events = new ArrayList<>(appender.list);
        assertFalse(events.isEmpty(), "the capture works: the stream client and the handlers log at debug");
        for (ILoggingEvent event : events) {
            assertNoSecrets("log entry '" + event.getFormattedMessage() + "'", render(event));
        }
        assertFalse(published.isEmpty());
    }

    @Test
    void whenTheHandlersRunNormallyThenNothingIsLoggedAtWarnOrAbove() throws Exception {
        FakeBridge good = bridge(FakeBridge.sending(validWire()));
        HandlerHarness harness = start(good.port());
        HandlerHarness.await(harness.meterStatuses, i -> i.getStatus() == ThingStatus.ONLINE, WAIT_MILLIS);
        harness.dispose();

        for (ILoggingEvent event : new ArrayList<>(appender.list)) {
            assertTrue(event.getLevel().toInt() < Level.WARN.toInt(),
                    "unexpected warning on the happy path: " + event.getFormattedMessage());
        }
    }
}

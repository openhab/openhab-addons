/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.threema.tests.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.threema.internal.ThreemaBindingConstants.CHID_CREDITS;
import static org.openhab.binding.threema.tests.internal.Mocks.createApiConnector;
import static org.openhab.binding.threema.tests.internal.Mocks.createThing;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.threema.internal.ThreemaBasicConfiguration;
import org.openhab.binding.threema.internal.ThreemaBasicHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import ch.threema.apitool.APIConnector;

/**
 * @author Kai K. - Initial contribution
 */
@SuppressWarnings({ "unchecked", "null" })
class ThreemaHandlerTest {

    private final class StatusCollector extends DefaultThingHandlerCallback {
        private final BlockingQueue<ThingStatusInfo> stati = new LinkedBlockingQueue<ThingStatusInfo>();
        private final ConcurrentHashMap<ChannelUID, BlockingQueue<State>> channelUpdates = new ConcurrentHashMap<>();

        @Override
        public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
            stati.add(thingStatus);
        }

        @Override
        public void stateUpdated(ChannelUID channelUID, State state) {
            getChannelUpdates(channelUID).add(state);
        }

        private BlockingQueue<State> getChannelUpdates(ChannelUID channelUID) {
            return channelUpdates.computeIfAbsent(channelUID, c -> new LinkedBlockingQueue<State>());
        }

        public ThingStatusInfo nextStatusInfo() throws InterruptedException {
            return stati.poll(5, TimeUnit.SECONDS);
        }

        public boolean noMoreStatusInfo() {
            return stati.isEmpty();
        }

        public State getNextChannelUpdates(ChannelUID channelUID) throws InterruptedException {
            return getChannelUpdates(channelUID).poll(5, TimeUnit.SECONDS);
        }

        public void reset() {
            stati.clear();
            channelUpdates.clear();
        }
    }

    private static final String THIRD = "THIRD";
    private static final String SECOND = "SECOND";
    private static final String FIRST = "FIRST";
    private static final String GATEWAY_ID = "*TESTID1";
    private static final String SECRET = "secret";
    private static final String DONTCARE = "notimportant";

    private APIConnector apiConnector;
    private StatusCollector statusCollector;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        apiConnector = createApiConnector();
        statusCollector = new StatusCollector();
    }

    @Test
    void expectInitializeSucceeds() throws IOException, InterruptedException {
        // GIVEN

        Thing thing = createThing(GATEWAY_ID, SECRET, null);

        // WHEN
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.setCallback(statusCollector);
        threemaHandler.initialize();

        // THEN
        ThreemaBasicConfiguration threemaConfig = thing.getConfiguration().as(ThreemaBasicConfiguration.class);
        verify(threemaConfig, atLeastOnce()).getGatewayId();
        verify(threemaConfig, atLeastOnce()).getSecret();
        assertThat(statusCollector.nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);
        assertThat(statusCollector.nextStatusInfo().getStatus()).isEqualTo(ThingStatus.ONLINE);
        assertThat(statusCollector.noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectInitializeFails() throws IOException, InterruptedException {
        List<String> recipients = List.of(FIRST, SECOND, THIRD);
        for (Thing thing : List.of(createThing(null, SECRET, recipients), createThing(GATEWAY_ID, null, recipients))) {
            // GIVEN
            statusCollector.reset();

            // WHEN
            ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
            threemaHandler.setCallback(statusCollector);
            threemaHandler.initialize();

            // THEN
            ThingStatusInfo status = statusCollector.nextStatusInfo();
            assertThat(status.getStatus()).isEqualTo(ThingStatus.UNINITIALIZED);
            assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.CONFIGURATION_ERROR);
            assertThat(statusCollector.noMoreStatusInfo()).isTrue();
        }
    }

    @Test
    void expectInitializeFailsIfWithWrongCredentials() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(GATEWAY_ID, SECRET, null);

        // WHEN
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing);
        threemaHandler.setCallback(statusCollector);
        threemaHandler.initialize();

        // THEN
        ThreemaBasicConfiguration threemaConfig = thing.getConfiguration().as(ThreemaBasicConfiguration.class);
        verify(threemaConfig, atLeastOnce()).getGatewayId();
        verify(threemaConfig, atLeastOnce()).getSecret();

        assertThat(statusCollector.nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);

        ThingStatusInfo lastStatus = statusCollector.nextStatusInfo();
        assertThat(lastStatus.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(lastStatus.getStatusDetail()).isEqualTo(ThingStatusDetail.CONFIGURATION_ERROR);
        assertThat(lastStatus.getDescription()).isEqualTo(HttpStatus.Code.UNAUTHORIZED.getMessage());

        assertThat(statusCollector.noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectSendTextMessageSimpleStringStringSucceeds() throws IOException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        when(apiConnector.lookupKey("*THREEMA")).thenReturn(new byte[1]);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isTrue();
        verify(apiConnector, atMost(1)).sendTextMessageSimple("*THREEMA", "Hello World");
    }

    @Test
    void expectSendTextMessageSimpleStringStringFails() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.setCallback(statusCollector);

        when(apiConnector.lookupKey("*THREEMA")).thenReturn(new byte[1]);
        when(apiConnector.sendTextMessageSimple("*THREEMA", "Hello World")).thenThrow(IOException.class);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isFalse();
        ThingStatusInfo status = statusCollector.nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(statusCollector.noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectSendTextMessageSimpleStringStringFailsIfThreemaIDUnknown() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.initialize();
        threemaHandler.setCallback(statusCollector);

        when(apiConnector.lookupKey("*THREEMA")).thenReturn(null);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isFalse();
        ThingStatusInfo status = statusCollector.nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.ONLINE);
        assertThat(statusCollector.noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectSendTextMessageSimpleStringSucceeds() throws IOException {
        // GIVEN
        List<String> recipients = List.of(FIRST, SECOND, THIRD);
        Thing thing = createThing(GATEWAY_ID, SECRET, recipients);
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.initialize();
        when(apiConnector.lookupKey(FIRST)).thenReturn(new byte[1]);
        when(apiConnector.lookupKey(SECOND)).thenReturn(new byte[2]);
        when(apiConnector.lookupKey(THIRD)).thenReturn(new byte[3]);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("Hello World");

        // THEN
        assertThat(suceeded).isTrue();
        for (String id : List.of(FIRST, SECOND, THIRD)) {
            verify(apiConnector).sendTextMessageSimple(id, "Hello World");
        }
    }

    @Test
    void expectSendTextMessageSimpleStringFails() throws IOException, InterruptedException {
        // GIVEN
        List<String> recipients = List.of(FIRST, SECOND, THIRD);
        Thing thing = createThing(GATEWAY_ID, SECRET, recipients);
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.setCallback(statusCollector);

        when(apiConnector.lookupKey(FIRST)).thenReturn(new byte[1]);
        when(apiConnector.lookupKey(SECOND)).thenReturn(new byte[2]);
        when(apiConnector.lookupKey(THIRD)).thenReturn(new byte[3]);

        when(apiConnector.sendTextMessageSimple(FIRST, "Hello World")).thenThrow(IOException.class);
        when(apiConnector.sendTextMessageSimple(SECOND, "Hello World")).thenReturn("super");
        when(apiConnector.sendTextMessageSimple(THIRD, "Hello World")).thenReturn("super");

        // WHEN
        threemaHandler.initialize();
        boolean suceeded = threemaHandler.sendTextMessageSimple("Hello World");

        // THEN
        assertThat(suceeded).isFalse();
        verify(apiConnector).sendTextMessageSimple(FIRST, "Hello World");
        verify(apiConnector).sendTextMessageSimple(SECOND, "Hello World");
        verify(apiConnector).sendTextMessageSimple(THIRD, "Hello World");

        assertThat(statusCollector.nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);
        assertThat(statusCollector.nextStatusInfo().getStatus()).isEqualTo(ThingStatus.ONLINE);
        ThingStatusInfo status = statusCollector.nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(statusCollector.noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectLookupCreditsToSucceed() throws InterruptedException {
        // GIVEN
        Thing thing = createThing(null, null, null);
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        ChannelUID creditsChannel = new ChannelUID(thing.getUID(), CHID_CREDITS);
        threemaHandler.setCallback(statusCollector);

        // WHEN
        int credits = threemaHandler.lookupCredits();

        // THEN
        assertThat(credits).isEqualTo(1);

        State state = statusCollector.getNextChannelUpdates(creditsChannel);
        assertThat(state).isEqualTo(new DecimalType(1));
    }

    @Test
    void expectLookupCreditsToFail() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        when(apiConnector.lookupCredits()).thenThrow(IOException.class);
        threemaHandler.setCallback(statusCollector);
        ChannelUID creditsChannel = new ChannelUID(thing.getUID(), CHID_CREDITS);

        // WHEN
        int credits = threemaHandler.lookupCredits();

        // THEN
        assertThat(credits).isEqualTo(-1);
        ThingStatusInfo status = statusCollector.nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(statusCollector.noMoreStatusInfo()).isTrue();

        State state = statusCollector.getNextChannelUpdates(creditsChannel);
        assertThat(state).isEqualTo(UnDefType.UNDEF);
    }

    @Test
    void expectCreditsChannelIsRefreshedIfLinked() throws InterruptedException, IOException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaBasicHandler threemaHandler = new ThreemaBasicHandler(thing, apiConnector);
        threemaHandler.setCallback(statusCollector);
        ChannelUID creditsChannel = new ChannelUID(thing.getUID(), CHID_CREDITS);
        when(apiConnector.lookupCredits()).thenReturn(99);

        // WHEN
        threemaHandler.channelLinked(creditsChannel);

        // THEN
        State state = statusCollector.getNextChannelUpdates(creditsChannel);
        assertThat(state).isEqualTo(new DecimalType(99));
    }
}

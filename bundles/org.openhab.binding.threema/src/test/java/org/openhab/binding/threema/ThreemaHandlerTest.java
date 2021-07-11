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
package org.openhab.binding.threema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.threema.Mocks.createApiConnector;
import static org.openhab.binding.threema.Mocks.createThing;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.threema.internal.ThreemaConfiguration;
import org.openhab.binding.threema.internal.ThreemaHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import ch.threema.apitool.APIConnector;

/**
 * @author Kai K. - Initial contribution
 */
@SuppressWarnings("unchecked")
@NonNullByDefault
class ThreemaHandlerTest {

    private static final String THIRD = "THIRD";
    private static final String SECOND = "SECOND";
    private static final String FIRST = "FIRST";
    private static final String GATEWAY_ID = "*TESTID1";
    private static final String SECRET = "secret";
    private static final String DONTCARE = "notimportant";
    private static final List<String> RECEPIENTS = List.of(FIRST, SECOND, THIRD);

    private APIConnector apiConnector;
    private ArrayBlockingQueue<ThingStatusInfo> stati;
    private ThingHandlerCallback thingHandlerCallback;

    @BeforeEach
    void setup() throws IOException, InterruptedException {
        apiConnector = createApiConnector();
        stati = new ArrayBlockingQueue<ThingStatusInfo>(10);
        thingHandlerCallback = new DefaultThingHandlerCallback() {

            @Override
            public void statusUpdated(Thing thing, ThingStatusInfo thingStatus) {
                assertThat(thing).isEqualTo(thing);
                stati.add(thingStatus);
            }
        };
    }

    private ThingStatusInfo nextStatusInfo() throws InterruptedException {
        return stati.poll(1, TimeUnit.SECONDS);
    }

    private boolean noMoreStatusInfo() {
        return stati.isEmpty();
    }

    @Test
    void expectInitializeSucceeds() throws IOException, InterruptedException {
        // GIVEN

        Thing thing = createThing(GATEWAY_ID, SECRET, null);

        // WHEN
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.setCallback(thingHandlerCallback);
        threemaHandler.initialize();

        // THEN
        ThreemaConfiguration threemaConfig = thing.getConfiguration().as(ThreemaConfiguration.class);
        verify(threemaConfig, atLeastOnce()).getGatewayId();
        verify(threemaConfig, atLeastOnce()).getSecret();
        assertThat(nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);
        assertThat(nextStatusInfo().getStatus()).isEqualTo(ThingStatus.ONLINE);
        assertThat(noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectInitializeFails() throws IOException, InterruptedException {
        for (Thing thing : List.of(createThing(null, SECRET, RECEPIENTS), createThing(GATEWAY_ID, null, RECEPIENTS))) {
            // GIVEN
            stati.clear();

            // WHEN
            ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
            threemaHandler.setCallback(thingHandlerCallback);
            threemaHandler.initialize();

            // THEN
            ThingStatusInfo status = nextStatusInfo();
            assertThat(status.getStatus()).isEqualTo(ThingStatus.UNINITIALIZED);
            assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.CONFIGURATION_ERROR);
            assertThat(noMoreStatusInfo()).isTrue();
        }
    }

    @Test
    void expectInitializeFailsIfWithWrongCredentials() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(GATEWAY_ID, SECRET, null);

        // WHEN
        ThreemaHandler threemaHandler = new ThreemaHandler(thing);
        threemaHandler.setCallback(thingHandlerCallback);
        threemaHandler.initialize();

        // THEN
        ThreemaConfiguration threemaConfig = thing.getConfiguration().as(ThreemaConfiguration.class);
        verify(threemaConfig, atLeastOnce()).getGatewayId();
        verify(threemaConfig, atLeastOnce()).getSecret();

        assertThat(nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);

        ThingStatusInfo lastStatus = nextStatusInfo();
        assertThat(lastStatus.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(lastStatus.getStatusDetail()).isEqualTo(ThingStatusDetail.CONFIGURATION_ERROR);
        assertThat(lastStatus.getDescription()).isEqualTo(HttpStatus.Code.UNAUTHORIZED.getMessage());

        assertThat(noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectSendTextMessageSimpleStringStringSucceeds() throws IOException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isTrue();
        verify(apiConnector, atMost(1)).sendTextMessageSimple("*THREEMA", "Hello World");
    }

    @Test
    void expectSendTextMessageSimpleStringStringFails() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.setCallback(thingHandlerCallback);

        when(apiConnector.sendTextMessageSimple("*THREEMA", "Hello World")).thenThrow(IOException.class);

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("*THREEMA", "Hello World");

        assertThat(suceeded).isFalse();
        ThingStatusInfo status = nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectSendTextMessageSimpleStringSucceeds() throws IOException {
        // GIVEN
        Thing thing = createThing(GATEWAY_ID, SECRET, RECEPIENTS);
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.initialize();

        // WHEN
        boolean suceeded = threemaHandler.sendTextMessageSimple("Hello World");

        // THEN
        assertThat(suceeded).isTrue();
        for (String id : RECEPIENTS) {
            verify(apiConnector).sendTextMessageSimple(id, "Hello World");
        }
    }

    @Test
    void expectSendTextMessageSimpleStringFails() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(GATEWAY_ID, SECRET, RECEPIENTS);
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
        threemaHandler.setCallback(thingHandlerCallback);

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

        assertThat(nextStatusInfo().getStatus()).isEqualTo(ThingStatus.UNKNOWN);
        assertThat(nextStatusInfo().getStatus()).isEqualTo(ThingStatus.ONLINE);
        ThingStatusInfo status = nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(noMoreStatusInfo()).isTrue();
    }

    @Test
    void expectLookupCreditsToSucceed() {
        // GIVEN
        Thing thing = createThing(null, null, null);
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);

        // WHEN
        int credits = threemaHandler.lookupCredits();

        // THEN
        assertThat(credits).isEqualTo(1);
    }

    @Test
    void expectLookupCreditsToFail() throws IOException, InterruptedException {
        // GIVEN
        Thing thing = createThing(DONTCARE, DONTCARE, Collections.emptyList());
        ThreemaHandler threemaHandler = new ThreemaHandler(thing, apiConnector);
        when(apiConnector.lookupCredits()).thenThrow(IOException.class);
        threemaHandler.setCallback(thingHandlerCallback);

        // WHEN
        int credits = threemaHandler.lookupCredits();

        // THEN
        assertThat(credits).isEqualTo(-1);
        ThingStatusInfo status = nextStatusInfo();
        assertThat(status.getStatus()).isEqualTo(ThingStatus.OFFLINE);
        assertThat(status.getStatusDetail()).isEqualTo(ThingStatusDetail.COMMUNICATION_ERROR);
        assertThat(noMoreStatusInfo()).isTrue();
    }
}

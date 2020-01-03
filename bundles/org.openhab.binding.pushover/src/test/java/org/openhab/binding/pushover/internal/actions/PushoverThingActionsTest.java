/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.pushover.internal.actions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.pushover.internal.connection.PushoverMessageBuilder;
import org.openhab.binding.pushover.internal.handler.PushoverAccountHandler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link PushoverThingActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class PushoverThingActionsTest {

    private static final String MESSAGE = "My message";
    private static final String TITLE = "My title";
    private static final String RECEIPT = "12345";
    private static final String SUCCESSFULL_RESPONSE = "{\"status\":1,\"request\":\"647d2300-702c-4b38-8b2f-d56326ae460b\"}";
    private static final String FAILED_RESPONSE = "{\"user\":\"invalid\",\"errors\":[\"user identifier is invalid\"],\"status\":0,\"request\":\"5042853c-402d-4a18-abcb-168734a801de\"}";

    @NonNullByDefault
    private final ThingActions thingActionsStub = new ThingActions() {
        @Override
        public void setThingHandler(ThingHandler handler) {
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    };

    private @Mock Thing mockThing;
    private @Mock HttpClient mockHttpClient;
    private @Mock PushoverAccountHandler mockPushoverAccountHandler;

    private final PushoverThingActions pushoverThingActions = new PushoverThingActions();

    @Before
    public void setUp() {
        initMocks(this);

        when(mockPushoverAccountHandler.getDefaultPushoverMessageBuilder(any()))
                .thenReturn(PushoverMessageBuilder.getInstance("key", "user"));
        when(mockPushoverAccountHandler.sendMessage(any())).thenReturn(Boolean.TRUE);
        when(mockPushoverAccountHandler.sendPriorityMessage(any())).thenReturn(RECEIPT);
        when(mockPushoverAccountHandler.cancelPriorityMessage(RECEIPT)).thenReturn(Boolean.TRUE);
    }

    // sendMessage
    @Test(expected = IllegalArgumentException.class)
    public void testSendMessageThingActionsIsNull() {
        PushoverThingActions.sendMessage(null, MESSAGE, TITLE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendMessageThingActionsIsNotPushoverThingActions() {
        PushoverThingActions.sendMessage(thingActionsStub, MESSAGE, TITLE);
    }

    @Test(expected = RuntimeException.class)
    public void testSendMessageThingHandlerIsNull() {
        PushoverThingActions.sendMessage(pushoverThingActions, MESSAGE, TITLE);
    }

    @Test
    public void testSendMessageWithoutTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverThingActions.sendMessage(pushoverThingActions, MESSAGE, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendMessage() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverThingActions.sendMessage(pushoverThingActions, MESSAGE, TITLE);
        assertThat(sent, is(true));
    }

    // sendPriorityMessage
    @Test(expected = IllegalArgumentException.class)
    public void sendPriorityMessageThingActionsIsNull() {
        PushoverThingActions.sendPriorityMessage(null, MESSAGE, TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendPriorityMessageThingActionsIsNotPushoverThingActions() {
        PushoverThingActions.sendPriorityMessage(thingActionsStub, MESSAGE, TITLE,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
    }

    @Test(expected = RuntimeException.class)
    public void sendPriorityMessageMessageThingHandlerIsNull() {
        PushoverThingActions.sendPriorityMessage(pushoverThingActions, MESSAGE, TITLE,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
    }

    @Test
    public void sendPriorityMessageWithoutTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        String receipt = PushoverThingActions.sendPriorityMessage(pushoverThingActions, MESSAGE, null,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    @Test
    public void sendPriorityMessage() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        String receipt = PushoverThingActions.sendPriorityMessage(pushoverThingActions, MESSAGE, TITLE,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    // cancelPriorityMessage
    @Test(expected = IllegalArgumentException.class)
    public void cancelPriorityMessageThingActionsIsNull() {
        PushoverThingActions.cancelPriorityMessage(null, RECEIPT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cancelPriorityMessageThingActionsIsNotPushoverThingActions() {
        PushoverThingActions.cancelPriorityMessage(thingActionsStub, RECEIPT);
    }

    @Test(expected = RuntimeException.class)
    public void cancelPriorityMessageThingHandlerIsNull() {
        PushoverThingActions.cancelPriorityMessage(pushoverThingActions, RECEIPT);
    }

    @Test
    public void cancelPriorityMessageWithValidReceipt() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean cancelled = PushoverThingActions.cancelPriorityMessage(pushoverThingActions, RECEIPT);
        assertThat(cancelled, is(true));
    }

    @Test
    public void cancelPriorityMessageWithInvalidReceipt() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean cancelled = PushoverThingActions.cancelPriorityMessage(pushoverThingActions, "invalid");
        assertThat(cancelled, is(false));
    }
}

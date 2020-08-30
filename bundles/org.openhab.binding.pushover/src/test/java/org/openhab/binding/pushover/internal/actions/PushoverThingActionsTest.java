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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.pushover.actions.PushoverThingActions;
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
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
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

    @BeforeEach
    public void setUp() {
        when(mockPushoverAccountHandler.getDefaultPushoverMessageBuilder(any()))
                .thenReturn(PushoverMessageBuilder.getInstance("key", "user"));
        when(mockPushoverAccountHandler.sendMessage(any())).thenReturn(Boolean.TRUE);
        when(mockPushoverAccountHandler.sendPriorityMessage(any())).thenReturn(RECEIPT);
        when(mockPushoverAccountHandler.cancelPriorityMessage(RECEIPT)).thenReturn(Boolean.TRUE);
    }

    // sendMessage
    @Test
    public void testSendMessageThingActionsIsNull() {
        assertThrows(IllegalArgumentException.class, () -> PushoverThingActions.sendMessage(null, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(IllegalArgumentException.class,
                () -> PushoverThingActions.sendMessage(thingActionsStub, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushoverThingActions.sendMessage(pushoverThingActions, MESSAGE, TITLE));
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
    @Test
    public void sendPriorityMessageThingActionsIsNull() {
        assertThrows(IllegalArgumentException.class, () -> PushoverThingActions.sendPriorityMessage(null, MESSAGE,
                TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void sendPriorityMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(IllegalArgumentException.class, () -> PushoverThingActions.sendPriorityMessage(thingActionsStub,
                MESSAGE, TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void sendPriorityMessageMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushoverThingActions.sendPriorityMessage(pushoverThingActions,
                MESSAGE, TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY));
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
    @Test
    public void cancelPriorityMessageThingActionsIsNull() {
        assertThrows(IllegalArgumentException.class, () -> PushoverThingActions.cancelPriorityMessage(null, RECEIPT));
    }

    @Test
    public void cancelPriorityMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(IllegalArgumentException.class,
                () -> PushoverThingActions.cancelPriorityMessage(thingActionsStub, RECEIPT));
    }

    @Test
    public void cancelPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushoverThingActions.cancelPriorityMessage(pushoverThingActions, RECEIPT));
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

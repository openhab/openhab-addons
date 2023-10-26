/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.pushover.internal.connection.PushoverMessageBuilder;
import org.openhab.binding.pushover.internal.handler.PushoverAccountHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link PushoverActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PushoverActionsTest {

    private static final String MESSAGE = "My Message";
    private static final String TITLE = "My Title";
    private static final String URL = "https://www.test.com";
    private static final String URL_TITLE = "Some Link";
    private static final String RECEIPT = "12345";
    private static final Duration TTL = Duration.ofSeconds(15);

    private final ThingActions thingActionsStub = new ThingActions() {
        @Override
        public void setThingHandler(ThingHandler handler) {
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    };

    private @NonNullByDefault({}) @Mock PushoverAccountHandler mockPushoverAccountHandler;
    private @NonNullByDefault({}) PushoverActions pushoverThingActions;

    @BeforeEach
    public void setUp() {
        pushoverThingActions = new PushoverActions();

        when(mockPushoverAccountHandler.getDefaultPushoverMessageBuilder(any()))
                .thenReturn(PushoverMessageBuilder.getInstance("key", "user"));
        when(mockPushoverAccountHandler.sendMessage(any())).thenReturn(Boolean.TRUE);
        when(mockPushoverAccountHandler.sendPriorityMessage(any())).thenReturn(RECEIPT);
        when(mockPushoverAccountHandler.cancelPriorityMessage(RECEIPT)).thenReturn(Boolean.TRUE);
    }

    // sendMessage
    @Test
    public void testSendMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(ClassCastException.class, () -> PushoverActions.sendMessage(thingActionsStub, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushoverActions.sendMessage(pushoverThingActions, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessage() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendMessage(pushoverThingActions, MESSAGE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendMessageWithTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendMessage(pushoverThingActions, MESSAGE, TITLE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendMessageWithTitleAndTTL() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendMessage(pushoverThingActions, MESSAGE, TITLE, TTL);
        assertThat(sent, is(true));
    }

    // sendURLMessage
    @Test
    public void testSendURLMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(ClassCastException.class,
                () -> PushoverActions.sendURLMessage(thingActionsStub, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushoverActions.sendURLMessage(pushoverThingActions, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageWithoutTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendURLMessage(pushoverThingActions, MESSAGE, null, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessageWithoutURLTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendURLMessage(pushoverThingActions, MESSAGE, TITLE, URL, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessage() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendURLMessage(pushoverThingActions, MESSAGE, TITLE, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessageWithTTL() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean sent = PushoverActions.sendURLMessage(pushoverThingActions, MESSAGE, TITLE, URL, URL_TITLE, TTL);
        assertThat(sent, is(true));
    }

    // sendPriorityMessage
    @Test
    public void testSendPriorityMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(ClassCastException.class, () -> PushoverActions.sendPriorityMessage(thingActionsStub, MESSAGE,
                TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushoverActions.sendPriorityMessage(pushoverThingActions, MESSAGE,
                TITLE, PushoverMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageWithoutTitle() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        String receipt = PushoverActions.sendPriorityMessage(pushoverThingActions, MESSAGE, null,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    @Test
    public void testSendPriorityMessage() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        String receipt = PushoverActions.sendPriorityMessage(pushoverThingActions, MESSAGE, TITLE,
                PushoverMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    // cancelPriorityMessage
    @Test
    public void testCancelPriorityMessageThingActionsIsNotPushoverThingActions() {
        assertThrows(ClassCastException.class, () -> PushoverActions.cancelPriorityMessage(thingActionsStub, RECEIPT));
    }

    @Test
    public void testCancelPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushoverActions.cancelPriorityMessage(pushoverThingActions, RECEIPT));
    }

    @Test
    public void testCancelPriorityMessageWithValidReceipt() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean cancelled = PushoverActions.cancelPriorityMessage(pushoverThingActions, RECEIPT);
        assertThat(cancelled, is(true));
    }

    @Test
    public void testCancelPriorityMessageWithInvalidReceipt() {
        pushoverThingActions.setThingHandler(mockPushoverAccountHandler);
        boolean cancelled = PushoverActions.cancelPriorityMessage(pushoverThingActions, "invalid");
        assertThat(cancelled, is(false));
    }
}

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
package org.openhab.binding.pushsafer.internal.actions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.pushsafer.internal.connection.PushsaferMessageBuilder;
import org.openhab.binding.pushsafer.internal.handler.PushsaferAccountHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link PushsaferActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class PushsaferActionsTest {

    private static final String MESSAGE = "My Message";
    private static final String TITLE = "My Title";
    private static final String URL = "https://www.test.com";
    private static final String URL_TITLE = "Some Link";
    private static final String RECEIPT = "12345";

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

    private @Mock PushsaferAccountHandler mockPushsaferAccountHandler;

    private PushsaferActions pushsaferThingActions;

    @BeforeEach
    public void setUp() {
        pushsaferThingActions = new PushsaferActions();

        when(mockPushsaferAccountHandler.getDefaultPushsaferMessageBuilder(any()))
                .thenReturn(PushsaferMessageBuilder.getInstance("key", "user"));
        when(mockPushsaferAccountHandler.sendMessage(any())).thenReturn(Boolean.TRUE);
        when(mockPushsaferAccountHandler.sendPriorityMessage(any())).thenReturn(RECEIPT);
        when(mockPushsaferAccountHandler.cancelPriorityMessage(RECEIPT)).thenReturn(Boolean.TRUE);
    }

    // sendMessage
    @Test
    public void testSendMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class, () -> PushsaferActions.sendMessage(thingActionsStub, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushsaferActions.sendMessage(pushsaferThingActions, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendMessage(pushsaferThingActions, MESSAGE, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendMessage(pushsaferThingActions, MESSAGE, TITLE);
        assertThat(sent, is(true));
    }

    // sendURLMessage
    @Test
    public void testSendURLMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class,
                () -> PushsaferActions.sendURLMessage(thingActionsStub, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushsaferActions.sendURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendURLMessage(pushsaferThingActions, MESSAGE, null, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessageWithoutURLTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    // sendPriorityMessage
    @Test
    public void testSendPriorityMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class, () -> PushsaferActions.sendPriorityMessage(thingActionsStub, MESSAGE,
                TITLE, PushsaferMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushsaferActions.sendPriorityMessage(pushsaferThingActions, MESSAGE,
                TITLE, PushsaferMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        String receipt = PushsaferActions.sendPriorityMessage(pushsaferThingActions, MESSAGE, null,
                PushsaferMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    @Test
    public void testSendPriorityMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        String receipt = PushsaferActions.sendPriorityMessage(pushsaferThingActions, MESSAGE, TITLE,
                PushsaferMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    // cancelPriorityMessage
    @Test
    public void testCancelPriorityMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class, () -> PushsaferActions.cancelPriorityMessage(thingActionsStub, RECEIPT));
    }

    @Test
    public void testCancelPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushsaferActions.cancelPriorityMessage(pushsaferThingActions, RECEIPT));
    }

    @Test
    public void testCancelPriorityMessageWithValidReceipt() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean cancelled = PushsaferActions.cancelPriorityMessage(pushsaferThingActions, RECEIPT);
        assertThat(cancelled, is(true));
    }

    @Test
    public void testCancelPriorityMessageWithInvalidReceipt() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean cancelled = PushsaferActions.cancelPriorityMessage(pushsaferThingActions, "invalid");
        assertThat(cancelled, is(false));
    }
}

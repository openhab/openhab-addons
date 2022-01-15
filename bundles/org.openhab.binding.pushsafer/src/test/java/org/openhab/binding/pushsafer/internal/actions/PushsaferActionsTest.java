/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.pushsafer.internal.connection.PushsaferConfigurationException;
import org.openhab.binding.pushsafer.internal.connection.PushsaferMessageBuilder;
import org.openhab.binding.pushsafer.internal.handler.PushsaferAccountHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link PushsaferActions}.
 *
 * @author Kevin Siml - Initial contribution, forked from Christoph Weitkamp
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
    public void setUp() throws PushsaferConfigurationException {
        pushsaferThingActions = new PushsaferActions();

        when(mockPushsaferAccountHandler.getDefaultPushsaferMessageBuilder(any()))
                .thenReturn(PushsaferMessageBuilder.getInstance("key", "user"));
        when(mockPushsaferAccountHandler.sendPushsaferMessage(any())).thenReturn(Boolean.TRUE);
        when(mockPushsaferAccountHandler.sendPushsaferPriorityMessage(any())).thenReturn(RECEIPT);
    }

    // sendPushsaferMessage
    @Test
    public void testSendMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class,
                () -> PushsaferActions.sendPushsaferMessage(thingActionsStub, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushsaferActions.sendPushsaferMessage(pushsaferThingActions, MESSAGE, TITLE));
    }

    @Test
    public void testSendMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendPushsaferMessage(pushsaferThingActions, MESSAGE, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendPushsaferMessage(pushsaferThingActions, MESSAGE, TITLE);
        assertThat(sent, is(true));
    }

    // sendPushsaferURLMessage
    @Test
    public void testSendURLMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class,
                () -> PushsaferActions.sendPushsaferURLMessage(thingActionsStub, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class,
                () -> PushsaferActions.sendPushsaferURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, URL_TITLE));
    }

    @Test
    public void testSendURLMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendPushsaferURLMessage(pushsaferThingActions, MESSAGE, null, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessageWithoutURLTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendPushsaferURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, null);
        assertThat(sent, is(true));
    }

    @Test
    public void testSendURLMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        boolean sent = PushsaferActions.sendPushsaferURLMessage(pushsaferThingActions, MESSAGE, TITLE, URL, URL_TITLE);
        assertThat(sent, is(true));
    }

    // sendPushsaferPriorityMessage
    @Test
    public void testSendPriorityMessageThingActionsIsNotPushsaferThingActions() {
        assertThrows(ClassCastException.class, () -> PushsaferActions.sendPushsaferPriorityMessage(thingActionsStub,
                MESSAGE, TITLE, PushsaferMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageThingHandlerIsNull() {
        assertThrows(RuntimeException.class, () -> PushsaferActions.sendPushsaferPriorityMessage(pushsaferThingActions,
                MESSAGE, TITLE, PushsaferMessageBuilder.EMERGENCY_PRIORITY));
    }

    @Test
    public void testSendPriorityMessageWithoutTitle() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        String receipt = PushsaferActions.sendPushsaferPriorityMessage(pushsaferThingActions, MESSAGE, null,
                PushsaferMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }

    @Test
    public void testSendPriorityMessage() {
        pushsaferThingActions.setThingHandler(mockPushsaferAccountHandler);
        String receipt = PushsaferActions.sendPushsaferPriorityMessage(pushsaferThingActions, MESSAGE, TITLE,
                PushsaferMessageBuilder.EMERGENCY_PRIORITY);
        assertThat(receipt, is(RECEIPT));
    }
}

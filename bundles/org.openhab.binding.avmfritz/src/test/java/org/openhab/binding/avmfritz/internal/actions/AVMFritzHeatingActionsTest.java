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
package org.openhab.binding.avmfritz.internal.actions;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzHeatingActionsHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link AVMFritzHeatingActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class AVMFritzHeatingActionsTest {

    private final ThingActions thingActionsStub = new ThingActions() {
        @Override
        public void setThingHandler(ThingHandler handler) {
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    };

    private @Mock @NonNullByDefault({}) AVMFritzHeatingActionsHandler heatingActionsHandlerMock;

    private @NonNullByDefault({}) AVMFritzHeatingActions heatingActions;

    @BeforeEach
    public void setUp() {
        heatingActions = new AVMFritzHeatingActions();
    }

    @Test
    public void testSetBoostModeThingActionsIsNotAVMFritzHeatingActions() {
        assertThrows(ClassCastException.class,
                () -> AVMFritzHeatingActions.setBoostMode(thingActionsStub, Long.valueOf(5L)));
    }

    @Test
    public void testSetBoostModeThingHandlerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzHeatingActions.setBoostMode(heatingActions, Long.valueOf(5L)));
    }

    @Test
    public void testSetBoostModeDurationNull() {
        heatingActions.setThingHandler(heatingActionsHandlerMock);
        assertThrows(IllegalArgumentException.class, () -> AVMFritzHeatingActions.setBoostMode(heatingActions, null));
    }

    @Test
    public void testSetBoostMode() {
        heatingActions.setThingHandler(heatingActionsHandlerMock);
        AVMFritzHeatingActions.setBoostMode(heatingActions, Long.valueOf(5L));
    }

    @Test
    public void testSetWindowOpenModeThingActionsIsNotAVMFritzHeatingActions() {
        assertThrows(ClassCastException.class,
                () -> AVMFritzHeatingActions.setWindowOpenMode(thingActionsStub, Long.valueOf(5L)));
    }

    @Test
    public void testSetWindowOpenModeThingHandlerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzHeatingActions.setWindowOpenMode(heatingActions, Long.valueOf(5L)));
    }

    @Test
    public void testSetWindowOpenModeDurationNull() {
        heatingActions.setThingHandler(heatingActionsHandlerMock);
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzHeatingActions.setWindowOpenMode(heatingActions, null));
    }

    @Test
    public void testSetWindowOpenMode() {
        heatingActions.setThingHandler(heatingActionsHandlerMock);
        AVMFritzHeatingActions.setWindowOpenMode(heatingActions, Long.valueOf(5L));
    }
}

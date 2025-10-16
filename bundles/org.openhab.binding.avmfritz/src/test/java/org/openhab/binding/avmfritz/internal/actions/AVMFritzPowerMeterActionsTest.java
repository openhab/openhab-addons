/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.avmfritz.internal.handler.AVMFritzPowerMeterActionsHandler;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingHandler;

/**
 * Unit tests for {@link AVMFritzPowerMeterActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class AVMFritzPowerMeterActionsTest {

    private final ThingActions thingActionsStub = new ThingActions() {
        @Override
        public void setThingHandler(ThingHandler handler) {
        }

        @Override
        public @Nullable ThingHandler getThingHandler() {
            return null;
        }
    };

    private @Mock @NonNullByDefault({}) AVMFritzPowerMeterActionsHandler powerMeterActionsHandlerMock;

    private @NonNullByDefault({}) AVMFritzPowerMeterActions powerMeterActions;

    @BeforeEach
    public void setUp() {
        powerMeterActions = new AVMFritzPowerMeterActions();
    }

    @Test
    public void testEnablePowerMeterHighRefreshThingActionsIsNotAVMFritzPowerMeterActions() {
        assertThrows(ClassCastException.class,
                () -> AVMFritzPowerMeterActions.enablePowerMeterHighRefresh(thingActionsStub, Long.valueOf(5L)));
    }

    @Test
    public void testEnablePowerMeterHighRefreshThingHandlerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzPowerMeterActions.enablePowerMeterHighRefresh(powerMeterActions, Long.valueOf(5L)));
    }

    @Test
    public void testEnablePowerMeterHighRefreshDurationNull() {
        powerMeterActions.setThingHandler(powerMeterActionsHandlerMock);
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzPowerMeterActions.enablePowerMeterHighRefresh(powerMeterActions, null));
    }

    @Test
    public void testEnablePowerMeterHighRefresh() {
        powerMeterActions.setThingHandler(powerMeterActionsHandlerMock);
        AVMFritzPowerMeterActions.enablePowerMeterHighRefresh(powerMeterActions, Long.valueOf(5L));
    }

    @Test
    public void testDisablePowerMeterHighRefreshThingActionsIsNotAVMFritzPowerMeterActions() {
        assertThrows(ClassCastException.class,
                () -> AVMFritzPowerMeterActions.disablePowerMeterHighRefresh(thingActionsStub));
    }

    @Test
    public void testDisablePowerMeterHighRefreshThingHandlerIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> AVMFritzPowerMeterActions.disablePowerMeterHighRefresh(powerMeterActions));
    }

    @Test
    public void testDisablePowerMeterHighRefresh() {
        powerMeterActions.setThingHandler(powerMeterActionsHandlerMock);
        AVMFritzPowerMeterActions.disablePowerMeterHighRefresh(powerMeterActions);
    }
}

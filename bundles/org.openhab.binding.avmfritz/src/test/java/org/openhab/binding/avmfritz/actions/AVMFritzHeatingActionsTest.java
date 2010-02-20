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
package org.openhab.binding.avmfritz.actions;

import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.core.thing.binding.ThingActions;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzHeatingActionsHandler;

/**
 * Unit tests for {@link AVMFritzHeatingActions}.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
public class AVMFritzHeatingActionsTest {

    private final ThingActions thingActionsStub = new ThingActions() {
        @Override
        public void setThingHandler(ThingHandler handler) {
        }

        @Override
        public ThingHandler getThingHandler() {
            return null;
        }
    };

    private @Mock AVMFritzHeatingActionsHandler heatingActionsHandler;

    private AVMFritzHeatingActions heatingActions;

    @Before
    public void setUp() {
        initMocks(this);

        heatingActions = new AVMFritzHeatingActions();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBoostModeThingActionsIsNull() {
        AVMFritzHeatingActions.setBoostMode(null, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBoostModeThingActionsIsNotPushoverThingActions() {
        AVMFritzHeatingActions.setBoostMode(thingActionsStub, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBoostModeThingHandlerIsNull() {
        AVMFritzHeatingActions.setBoostMode(heatingActions, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetBoostModeDurationNull() {
        heatingActions.setThingHandler(heatingActionsHandler);
        AVMFritzHeatingActions.setBoostMode(heatingActions, null);
    }

    @Test
    public void testSetBoostMode() {
        heatingActions.setThingHandler(heatingActionsHandler);
        AVMFritzHeatingActions.setBoostMode(heatingActions, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWindowOpenModeThingActionsIsNull() {
        AVMFritzHeatingActions.setWindowOpenMode(null, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWindowOpenModeThingActionsIsNotPushoverThingActions() {
        AVMFritzHeatingActions.setWindowOpenMode(thingActionsStub, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWindowOpenModeThingHandlerIsNull() {
        AVMFritzHeatingActions.setWindowOpenMode(heatingActions, Long.valueOf(5L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetWindowOpenModeDurationNull() {
        heatingActions.setThingHandler(heatingActionsHandler);
        AVMFritzHeatingActions.setWindowOpenMode(heatingActions, null);
    }

    @Test
    public void testSetWindowOpenMode() {
        heatingActions.setThingHandler(heatingActionsHandler);
        AVMFritzHeatingActions.setWindowOpenMode(heatingActions, Long.valueOf(5L));
    }
}

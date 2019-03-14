/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.type.ChannelKind;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.StateDescription;
import org.openhab.binding.loxone.internal.types.LxCategory;
import org.openhab.binding.loxone.internal.types.LxContainer;
import org.openhab.binding.loxone.internal.types.LxUuid;

class LxControlTest {
    LxServerHandlerDummy handler;
    LxUuid controlUuid;
    LxUuid roomUuid;
    LxUuid categoryUuid;
    String controlName;

    void setupControl(String controlUuid, String roomUuid, String categoryUuid, String controlName) {
        this.controlUuid = new LxUuid(controlUuid);
        this.roomUuid = new LxUuid(roomUuid);
        this.categoryUuid = new LxUuid(categoryUuid);
        this.controlName = controlName;
        handler = new LxServerHandlerDummy();
        handler.loadConfiguration();
    }

    <T> void testControlCreation(Class<T> testClass, int numberOfSubcontrols, int numberOfChannels,
            int numberOfChannelsWithSubs, int numberOfStates) {
        assertEquals(1, numberOfControls(testClass));
        LxControl c = getControl(controlUuid);
        assertNotNull(c);
        Map<LxUuid, LxControl> subC = c.getSubControls();
        assertNotNull(subC);
        assertEquals(numberOfSubcontrols, subC.size());
        assertEquals(controlUuid, c.getUuid());
        assertEquals(controlName, c.getName());
        assertEquals(controlName, c.getLabel());
        LxContainer room = c.getRoom();
        assertNotNull(room);
        assertEquals(roomUuid, room.getUuid());
        LxCategory cat = c.getCategory();
        assertNotNull(cat);
        assertEquals(categoryUuid, cat.getUuid());
        assertEquals(numberOfChannels, c.getChannels().size());
        assertEquals(numberOfChannelsWithSubs, c.getChannelsWithSubcontrols().size());
        assertEquals(numberOfStates, c.getStates().size());
    }

    void testChannel(String itemType, String namePostFix, BigDecimal min, BigDecimal max, BigDecimal step,
            String format, boolean readOnly) {
        LxControl ctrl = getControl(controlUuid);
        assertNotNull(ctrl);
        Channel c = getChannel(getExpectedName(controlName, ctrl.getRoom().getName(), namePostFix), ctrl);
        assertNotNull(c);
        assertNotNull(c.getUID());
        assertNotNull(c.getDescription());
        assertEquals(itemType, c.getAcceptedItemType());
        assertEquals(ChannelKind.STATE, c.getKind());
        if (readOnly || min != null || max != null || step != null || format != null) {
            StateDescription d = handler.stateDescriptions.get(c.getUID());
            assertNotNull(d);
            assertEquals(min, d.getMinimum());
            assertEquals(max, d.getMaximum());
            assertEquals(step, d.getStep());
            assertEquals(format, d.getPattern());
            assertTrue(d.isReadOnly());
        }
    }

    void testChannel(String itemType) {
        testChannel(itemType, null, null, null, null, null, false);
    }

    void testChannelState(String namePostFix, State expectedValue) {
        LxControl ctrl = getControl(controlUuid);
        assertNotNull(ctrl);
        Channel c = getChannel(getExpectedName(controlName, ctrl.getRoom().getName(), namePostFix), ctrl);
        assertNotNull(c);
        State current = ctrl.getChannelState(c.getUID());
        if (expectedValue != null) {
            assertNotNull(current);
        }
        assertEquals(expectedValue, current);
    }

    void testChannelState(State expectedValue) {
        testChannelState(null, expectedValue);
    }

    void changeLoxoneState(String stateName, Double value) {
        LxControl ctrl = getControl(controlUuid);
        assertNotNull(ctrl);
        LxControlState state = ctrl.getStates().get(stateName);
        assertNotNull(state);
        state.setStateValue(value);
    }

    void executeCommand(String namePostFix, Command command) {
        LxControl ctrl = getControl(controlUuid);
        assertNotNull(ctrl);
        Channel c = getChannel(getExpectedName(controlName, ctrl.getRoom().getName(), namePostFix), ctrl);
        assertNotNull(c);
        try {
            ctrl.handleCommand(c.getUID(), command);
        } catch (IOException e) {
            fail("This exception should never happen in test environment.");
        }
    }

    void executeCommand(Command command) {
        executeCommand(null, command);
    }

    void testAction(String expectedAction, int numberOfActions) {
        assertEquals(numberOfActions, handler.actionQueue.size());
        if (numberOfActions > 0) {
            String action = handler.actionQueue.poll();
            assertNotNull(action);
            assertEquals(controlUuid + "/" + expectedAction, action);
        }
    }

    void testAction(String expectedAction) {
        if (expectedAction == null) {
            testAction(null, 0);
        } else {
            testAction(expectedAction, 1);
        }
    }

    private Channel getChannel(String name, LxControl c) {
        List<Channel> channels = c.getChannels();
        List<Channel> filtered = channels.stream().filter(a -> name.equals(a.getLabel())).collect(Collectors.toList());
        assertEquals(1, filtered.size());
        return filtered.get(0);
    }

    private <T> long numberOfControls(Class<T> c) {
        Collection<LxControl> v = handler.controls.values();
        return v.stream().filter(o -> c.isInstance(o)).collect(Collectors.counting());
    }

    private LxControl getControl(LxUuid uuid) {
        return handler.controls.get(uuid);
    }

    private String getExpectedName(String controlName, String roomName, String postFix) {
        return roomName + " / " + controlName + (postFix != null ? postFix : "");
    }
}

/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tesla.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tesla.internal.TeslaChannelSelectorProxy.TeslaChannelSelector;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests for {@link TeslaChannelSelectorProxy}.
 *
 * @author Ronny Glöckner - Initial contribution
 */
@NonNullByDefault
public class TeslaChannelSelectorProxyTest {

    @Test
    public void steeringWheelHeaterUpdatesItsChannel() {
        TeslaChannelSelector selector = TeslaChannelSelector.getValueSelectorFromRESTID("steering_wheel_heater");

        assertFalse(selector.isProperty());
        assertEquals("steeringwheelheater", selector.getChannelID());
        assertEquals(OnOffType.ON, new TeslaChannelSelectorProxy().getState("true", selector, new HashMap<>()));
        assertEquals(OnOffType.OFF, new TeslaChannelSelectorProxy().getState("false", selector, new HashMap<>()));
    }
}

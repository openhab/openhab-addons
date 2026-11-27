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
package org.openhab.binding.myvaillant.internal.action;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.binding.ThingActions;

/**
 * Tests the static methods Rules DSL calls with the actions as first argument, e.g.
 * {@code getActions("myvaillant", "myvaillant:zone:...").cancelQuickVeto()}.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
class ThingActionsTest {

    @Test
    void staticMethodsForwardToTheActions() {
        // Without a handler the actions fail gracefully, which shows that the call reached the instance
        ThingActions systemActions = new SystemActions();
        assertFalse(SystemActions.cancelHoliday(systemActions));
        assertNull(SystemActions.exportEnergyData(systemActions, "DAY", "2026-11-01", "2026-11-02", null));

        ThingActions zoneActions = new ZoneActions();
        assertFalse(ZoneActions.setQuickVeto(zoneActions, 21.0, null));
        assertNull(ZoneActions.getTimeProgram(zoneActions, "heating"));

        ThingActions dhwActions = new DhwActions();
        assertFalse(DhwActions.setTimeProgram(dhwActions, "{}"));
        assertNull(DhwActions.getCirculationTimeProgram(dhwActions));
    }

    @Test
    void staticMethodsRejectForeignActions() {
        assertThrows(ClassCastException.class, () -> ZoneActions.cancelQuickVeto(new DhwActions()));
    }
}
